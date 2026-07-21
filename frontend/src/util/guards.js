import { useAuthStore } from '@/stores/auth';

// 개별 라우트에 beforeEnter 로 걸고 싶을 때 사용.
// (기본은 router/index.js 의 전역 가드가 meta.requiresAuth 로 처리합니다)
export const isAuthenticated = (to) => {
  const auth = useAuthStore();
  if (!auth.isLogin) {
    return { name: 'Login', query: { redirect: to.fullPath } };
  }
};
