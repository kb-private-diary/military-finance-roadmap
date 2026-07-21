// 소셜 라우트 (태석)
export default [
  {
    path: '/social',
    name: 'social',
    component: () => import('@/pages/social/SocialPage.vue'),
    // beforeEnter: isAuthenticated, // 로그인 필요 시 guards.js 의 isAuthenticated 연결
  },
];
