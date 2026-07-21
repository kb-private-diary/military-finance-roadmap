// member 라우트 (담당: 호빈)
// 화면 파일 위치: @/pages/member/
export default [
  {
    path: '/terms',
    name: 'Terms',   // 약관 안내
    component: () => import('@/pages/member/TermsPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/login',
    name: 'Login',   // 로그인
    component: () => import('@/pages/member/LoginPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/signup/info',
    name: 'SignupInfo',   // 회원가입 - 개인정보
    component: () => import('@/pages/member/SignupInfoPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/signup/military',
    name: 'SignupMilitary',   // 회원가입 - 군정보
    component: () => import('@/pages/member/SignupMilitaryPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/find/id',
    name: 'FindId',   // 아이디 찾기
    component: () => import('@/pages/member/FindIdPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/find/password',
    name: 'FindPassword',   // 비밀번호 찾기
    component: () => import('@/pages/member/FindPasswordPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/mypage',
    name: 'MyPage',   // 회원정보
    component: () => import('@/pages/member/MyPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/mypage/edit',
    name: 'MyPageEdit',   // 회원정보 수정
    component: () => import('@/pages/member/MyPageEditPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/mypage/password',
    name: 'MyPagePassword',   // 비밀번호 변경
    component: () => import('@/pages/member/MyPagePasswordPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/mypage/withdraw',
    name: 'MyPageWithdraw',   // 회원 탈퇴
    component: () => import('@/pages/member/MyPageWithdrawPage.vue'),
    meta: { requiresAuth: true },
  },
];
