// 로드맵 · 진로 라우트 (지원)
export default [
  {
    path: '/roadmap/job',
    name: 'job',
    component: () => import('@/pages/job/JobPage.vue'),
    // beforeEnter: isAuthenticated, // 로그인 필요 시 guards.js 의 isAuthenticated 연결
  },
];
