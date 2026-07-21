// AI 재무 상담 챗봇 (FastAPI 연동) 라우트 (에스더)
export default [
  {
    path: '/chat',
    name: 'chat',
    component: () => import('@/pages/chat/ChatPage.vue'),
    // beforeEnter: isAuthenticated, // 로그인 필요 시 guards.js 의 isAuthenticated 연결
  },
];
