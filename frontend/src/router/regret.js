// 후회 소비 회고 라우트 (수연)
export default [
  {
    path: '/regret',
    name: 'regret',
    component: () => import('@/pages/regret/RegretPage.vue'),
    // beforeEnter: isAuthenticated, // 로그인 필요 시 guards.js 의 isAuthenticated 연결
  },
];
