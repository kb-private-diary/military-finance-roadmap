export default [
  {
    path: '/social',
    name: 'Social',   // 저축 비교
    component: () => import('@/pages/social/SocialPage.vue'),
    meta: { requiresAuth: true },
  },
];
