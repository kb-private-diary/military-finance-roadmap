import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

const instance = axios.create({
  timeout: 1000,
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
    console.log(config.headers.Authrization);
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
    if (error.response?.status === 401) {
      const { logout } = useAuthStore();
      logout();
      router.push('/auth/login?error=login_required');
      return Promise.reject({ error: '로그인이 필요한 서비스입니다.' });
    }
    return Promise.reject(error);
  },
);
export default instance;
