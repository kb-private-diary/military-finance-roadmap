import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '@/pages/HomePage.vue';

// 인증(로그인/회원)
import authRoutes from './auth';

// 도메인별 라우트 (담당자가 각 파일에서 확장)
import dashboardRoutes from './dashboard';
import simulatorRoutes from './simulator';
import travelRoutes from './travel';
import rentRoutes from './rent';
import carRoutes from './car';
import jobRoutes from './job';
import socialRoutes from './social';
import regretRoutes from './regret';
import chatRoutes from './chat';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomePage },
    ...authRoutes,
    ...dashboardRoutes,
    ...simulatorRoutes,
    ...travelRoutes,
    ...rentRoutes,
    ...carRoutes,
    ...jobRoutes,
    ...socialRoutes,
    ...regretRoutes,
    ...chatRoutes,
  ],
});

export default router;
