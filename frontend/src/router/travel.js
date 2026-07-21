// 로드맵 · 여행 라우트 (태석)
export default [
  {
    path: '/roadmap/travel',
    name: 'travel',
    component: () => import('@/pages/travel/TravelPage.vue'),
    // beforeEnter: isAuthenticated, // 로그인 필요 시 guards.js 의 isAuthenticated 연결
  },
];
