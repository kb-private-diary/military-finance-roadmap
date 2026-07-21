// main 라우트 (담당: 지원, 호빈)
// 화면 파일 위치: @/pages/main/
export default [
  {
    path: '/',
    name: 'Welcome',   // 미니앱 진입
    component: () => import('@/pages/main/WelcomePage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/home',
    name: 'Home',   // 홈 메인
    component: () => import('@/pages/main/HomePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/roadmap',
    name: 'RoadmapMain',   // 로드맵 메인
    component: () => import('@/pages/main/RoadmapMainPage.vue'),
    meta: { requiresAuth: true },
  },
];
