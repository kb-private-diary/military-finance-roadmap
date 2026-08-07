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
  const getRefreshToken = () => state.value.refreshToken;

  // 액세스 토큰(30분)이 만료되면 리프레시 토큰(14일)으로 조용히 재발급받는다.
  // axios 인터셉터가 401을 받았을 때 이걸 먼저 시도하고, 그래도 실패해야 로그아웃한다.
  // (예전엔 리프레시 토큰을 저장만 해두고 실제로 쓰는 곳이 없어서, 30분마다 강제 로그아웃됐었음 - 2026-08-06)
  const refresh = async () => {
    const refreshToken = state.value.refreshToken;
    if (!refreshToken) throw new Error('리프레시 토큰이 없습니다');
    // /api/users/refresh는(로그인과 달리) ApiResponse로 감싸서 응답하므로 data.data로 한 번 더 풀어야 한다.
    const { data } = await axios.post('/api/users/refresh', { refreshToken });
    state.value = { ...data.data };
    localStorage.setItem('auth', JSON.stringify(state.value));
  };

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
    getRefreshToken,
    refresh,
  };
});
