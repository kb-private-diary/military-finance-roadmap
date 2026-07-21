// 로드맵 · 자취(월세) 라우트 (수연)
export default [
  {
    path: '/roadmap/rent',
    name: 'rent',
    component: () => import('@/pages/rent/RentPage.vue'),
    // beforeEnter: isAuthenticated, // 로그인 필요 시 guards.js 의 isAuthenticated 연결
  },
];
