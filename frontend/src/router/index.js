import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

import carRoutes from './car';
import chatRoutes from './chat';
import dashboardRoutes from './dashboard';
import jobRoutes from './job';
import mainRoutes from './main';
import memberRoutes from './member';
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
    ...regretRoutes,
    ...rentRoutes,
    ...simulatorRoutes,
    ...socialRoutes,
    ...travelRoutes,
  ],
});

// 인증이 필요한 화면(meta.requiresAuth) 접근 시 로그인 페이지로 이동
router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.meta.requiresAuth && !auth.isLogin) {
    return { name: 'Login', query: { redirect: to.fullPath } };
  }
});

export default router;
