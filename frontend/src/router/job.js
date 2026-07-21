// job 라우트 (담당: 지원)
// 화면 파일 위치: @/pages/job/
export default [
  {
    path: '/job/goals/new',
    name: 'JobGoalCreate',   // step1) 진로 목표 등록
    component: () => import('@/pages/job/JobGoalCreatePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/job/goals/:goalId/recommend',
    name: 'JobRecommend',   // step2) 진로 로드맵 추천
    component: () => import('@/pages/job/JobRecommendPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/job/goals/:goalId/cost',
    name: 'JobCost',   // step3) 진로 비용 계산
    component: () => import('@/pages/job/JobCostPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/job/goals/:goalId/products',
    name: 'JobProducts',   // step4) 진로 금융상품 추천
    component: () => import('@/pages/job/JobProductsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/job/goals/:goalId',
    name: 'JobGoalDetail',   // 진로 목표 상세
    component: () => import('@/pages/job/JobGoalDetailPage.vue'),
    meta: { requiresAuth: true },
  },
];
