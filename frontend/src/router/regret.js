// regret 라우트 (담당: 수연)
// 화면 파일 위치: @/pages/regret/
export default [
  {
    path: '/regret',
    name: 'RegretDashboard',   // 후회소비 대시보드
    component: () => import('@/pages/regret/RegretDashboardPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/regret/spendings/:date',
    name: 'RegretDailySpending',   // 일자별 지출
    component: () => import('@/pages/regret/RegretDailySpendingPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/regret/review',
    name: 'RegretReview',   // 소비 점호 태깅
    component: () => import('@/pages/regret/RegretReviewPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/regret/report',
    name: 'RegretReport',   // 월별 분석
    component: () => import('@/pages/regret/RegretReportPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/regret/challenges',
    name: 'RegretChallenge',   // 절감 목표 + 적금 추천
    component: () => import('@/pages/regret/RegretChallengePage.vue'),
    meta: { requiresAuth: true },
  },
];
