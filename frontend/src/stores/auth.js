import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import axios from 'axios';

const initState = {
  token: '', // 접근 토큰(JWT)
  refreshToken: '',
  user: {
    id: null,
    userId: '', // 로그인 아이디
    name: '',
    phone: '',
    status: '',
  },
};

export const useAuthStore = defineStore('auth', () => {
  const state = ref({ ...initState });

  const isLogin = computed(() => !!state.value.token);
  const userId = computed(() => state.value.user.userId);
  const name = computed(() => state.value.user.name);

  const login = async ({ username, password }) => {
    // 로그인은 Spring Security 필터(JwtUsernamePasswordAuthenticationFilter)가 처리해서
    // 다른 API와 달리 ApiResponse로 감싸지 않은 AuthResultDTO({token, refreshToken, user})를 그대로 응답한다.
    const { data } = await axios.post('/api/users/login', { username, password });
    state.value = { ...data };
    localStorage.setItem('auth', JSON.stringify(state.value));
  };

  const logout = () => {
    localStorage.removeItem('auth');
    state.value = { ...initState };
  };

  const getToken = () => state.value.token;

  const load = () => {
    const saved = localStorage.getItem('auth');
    if (saved != null) {
      state.value = JSON.parse(saved);
    }
  };

  load();

  return {
    state,
    isLogin,
    userId,
    name,
    login,
    logout,
    getToken,
  };
});
