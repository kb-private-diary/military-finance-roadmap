// social 라우트 (담당: 태석)
// 화면 파일 위치: @/pages/social/
export default [
  {
    path: '/social',
    name: 'Social',   // 소셜
    component: () => import('@/pages/social/SocialPage.vue'),
    meta: { requiresAuth: true },
  },
];
