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

    // 401: 인증 만료 → 자동 로그아웃 + 로그인 이동
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
