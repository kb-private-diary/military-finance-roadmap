import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import { useToast } from '@/composables/useToast';
import router from '@/router';

const instance = axios.create({
  timeout: 15000,
});

//axios요청 보낼 때 http header에 jwt token넣어서 보내야함.
//axios응답 받을 때 꺼내기 전 응답내용을 확인해서 401, 403, 500 등
//응답코드에 따라 동일하게 처리 가능!!!
//==> 인터셉터(가로채다, 중간에 가로채서 멈추다.)

instance.interceptors.request.use((config) => {
  const { getToken } = useAuthStore();
  const token = getToken();
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});
// 액세스 토큰 만료(401)로 동시에 여러 요청이 실패할 수 있어서, 리프레시는 한 번만 실행하고
// 그동안 들어온 다른 401 요청들은 대기시켰다가 새 토큰으로 같이 재시도한다.
let isRefreshing = false;
let waiters = [];
const flushWaiters = (error, token) => {
  waiters.forEach(({ resolve, reject }) => (error ? reject(error) : resolve(token)));
  waiters = [];
};

instance.interceptors.response.use(
  (response) => {
    if (response.status === 200) {
      return response;
    }
    if (response.status === 404) {
      return Promise.reject('404: 페이지 없음 ' + response.request);
    }
    return response;
  },
  async (error) => {
    const status = error.response?.status;
    const originalRequest = error.config;

    // 401: 액세스 토큰(30분) 만료 - 로그아웃시키기 전에 리프레시 토큰(14일)으로 먼저 재발급 시도.
    // 리프레시 자체가 401이거나(리프레시 토큰도 만료) 이미 한 번 재시도한 요청이면 더 시도 안 하고 로그아웃.
    if (status === 401 && !originalRequest?._retry && !originalRequest?.url?.includes('/api/users/refresh')) {
      const authStore = useAuthStore();

      if (isRefreshing) {
        try {
          const newToken = await new Promise((resolve, reject) => waiters.push({ resolve, reject }));
          originalRequest._retry = true;
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
          return instance(originalRequest);
        } catch {
          return Promise.reject({ error: '로그인이 필요한 서비스입니다.' });
        }
      }

      originalRequest._retry = true;
      isRefreshing = true;
      try {
        await authStore.refresh();
        const newToken = authStore.getToken();
        flushWaiters(null, newToken);
        originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
        return instance(originalRequest);
      } catch (refreshError) {
        flushWaiters(refreshError, null);
        authStore.logout();
        router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } });
        return Promise.reject({ error: '로그인이 필요한 서비스입니다.' });
      } finally {
        isRefreshing = false;
      }
    }

    // 리프레시까지 실패했거나(위에서 못 걸러진 401) 재발급 자체가 불가능한 상황 → 로그아웃
    if (status === 401) {
      const { logout } = useAuthStore();
      logout();
      router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } });
      return Promise.reject({ error: '로그인이 필요한 서비스입니다.' });
    }

    // 전역 공통 처리: 어느 화면이든 똑같이 반응할 것만.
    // (4xx 비즈니스 에러는 화면 try/catch 에서 처리하도록 그대로 넘김)
    const { show } = useToast();
    if (!error.response) {
      show('네트워크 연결을 확인해주세요', 'error'); // 끊김·타임아웃
    } else if (status >= 500) {
      show('잠시 후 다시 시도해주세요', 'error'); // 서버 오류
    }

    return Promise.reject(error);
  },
);
export default instance;
