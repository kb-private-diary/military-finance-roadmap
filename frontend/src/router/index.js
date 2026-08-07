import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

import carRoutes from './car';
import chatRoutes from './chat';
import dashboardRoutes from './dashboard';
import jobRoutes from './job';
import mainRoutes from './main';
import memberRoutes from './member';
import openbankingRoutes from './openbanking';
import productRoutes from './product';
import pushRoutes from './push';
import regretRoutes from './regret';
import rentRoutes from './rent';
import simulatorRoutes from './simulator';
import socialRoutes from './social';
import travelRoutes from './travel';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...carRoutes,
    ...chatRoutes,
    ...dashboardRoutes,
    ...jobRoutes,
    ...mainRoutes,
    ...memberRoutes,
    ...openbankingRoutes,
    ...productRoutes,
    ...pushRoutes,
    ...regretRoutes,
    ...rentRoutes,
    ...simulatorRoutes,
    ...socialRoutes,
    ...travelRoutes,
  ],
});

// 라우팅 가드
router.beforeEach((to) => {
  const auth = useAuthStore();
  // 로그인 상태여도 Welcome(진입 페이지)은 그대로 보여준다(버튼 눌러야 Home 이동).
  // Login 화면만 이미 로그인 상태면 건너뛰고 홈으로.
  if (to.name === 'Login' && auth.isLogin) {
    return { name: 'Home' };
  }
  // 인증 필요한 화면인데 로그인 안 됐으면 로그인 페이지로
  if (to.meta.requiresAuth && !auth.isLogin) {
    return { name: 'Login', query: { redirect: to.fullPath } };
  }
});

export default router;
