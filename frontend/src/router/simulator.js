// 군적금 만기 시뮬레이터 라우트 (석윤)
export default [
  {
    path: '/simulator',
    name: 'simulator',
    component: () => import('@/pages/simulator/SimulatorPage.vue'),
    // beforeEnter: isAuthenticated, // 로그인 필요 시 guards.js 의 isAuthenticated 연결
  },
];
