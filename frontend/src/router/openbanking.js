// openbanking 라우트 (담당: 수연)
// 화면 파일 위치: @/pages/openbanking/
export default [
  {
    path: '/onboarding',
    name: 'Onboarding',   // 웰컴 온보딩 (오픈뱅킹 필수 연동) - 헤더(텅장일병구하기)는 유지, 탭 메뉴만 숨김
    component: () => import('@/pages/openbanking/OnboardingPage.vue'),
    meta: { requiresAuth: true, showTabNav: false },
  },
  {
    path: '/regret/link',
    name: 'RegretLink',   // 오픈뱅킹 재연동 유도
    component: () => import('@/pages/openbanking/RegretLinkPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/regret/link/auth',
    name: 'RegretLinkAuth',   // 오픈뱅킹 인증
    component: () => import('@/pages/openbanking/RegretLinkAuthPage.vue'),
    meta: { requiresAuth: true },
  },
];
