// push(웹푸시 알림함) 라우트 (담당: 석윤)
// 화면 파일 위치: @/pages/push/
export default [
  {
    path: '/push',
    name: 'WebPush', // 알림함
    component: () => import('@/pages/push/WebPushPage.vue'),
    meta: { requiresAuth: true },
  },
];
