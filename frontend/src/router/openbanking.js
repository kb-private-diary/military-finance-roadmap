// openbanking 라우트 (담당: 수연)
// 화면 파일 위치: @/pages/openbanking/
// 온보딩 3단계(웰컴 → 이용동의 → 연동완료)는 정해진 플로우라 탭바 숨김(showTabNav: false)
export default [
  {
    path: '/onboarding',
    name: 'Onboarding',   // 웰컴 온보딩 (오픈뱅킹 필수 연동)
    component: () => import('@/pages/openbanking/OnboardingPage.vue'),
    meta: { requiresAuth: true, showTabNav: false },
  },
  {
    path: '/onboarding/terms',
    name: 'OnboardingTerms',   // 오픈뱅킹 이용 동의
    component: () => import('@/pages/openbanking/OnboardingTermsPage.vue'),
    meta: { requiresAuth: true, showTabNav: false },
  },
  {
    path: '/onboarding/done',
    name: 'OnboardingDone',   // 연동 완료
    component: () => import('@/pages/openbanking/OnboardingDonePage.vue'),
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
