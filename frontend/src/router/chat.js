// chat 라우트 (담당: 에스더)
// 화면 파일 위치: @/pages/chat/
export default [
  {
    path: '/chat',
    name: 'Chat',   // 챗봇
    component: () => import('@/pages/chat/ChatPage.vue'),
    meta: { requiresAuth: true },
  },
];
