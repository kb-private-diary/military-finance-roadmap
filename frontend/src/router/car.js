// 로드맵 · 자동차 라우트 (호빈)
export default [
  {
    path: '/roadmap/car',
    name: 'car',
    component: () => import('@/pages/car/CarPage.vue'),
    // beforeEnter: isAuthenticated, // 로그인 필요 시 guards.js 의 isAuthenticated 연결
  },
];
