// main 라우트 (담당: 수연, 지원)
// 화면 파일 위치: @/pages/main/
export default [
  {
    path: '/',
    name: 'Welcome',   // 미니앱 진입 (로그인 전 진입 페이지 - 공통 헤더 대신 전용 최소 헤더)
    component: () => import('@/pages/main/WelcomePage.vue'),
    meta: { requiresAuth: false, hideHeader: true },
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
