// car 라우트 (담당: 호빈)
// 화면 파일 위치: @/pages/car/
export default [
  {
    path: '/car/goals/:goalId/cost',
    name: 'CarCost',   // step3) 자동차 비용 계산
    component: () => import('@/pages/car/CarCostPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/car/goals/:goalId/products',
    name: 'CarProducts',   // step4) 자동차 금융상품 추천
    component: () => import('@/pages/car/CarProductsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/car/goals/:goalId',
    name: 'CarGoalDetail',   // 자동차 목표 상세
    component: () => import('@/pages/car/CarGoalDetailPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/car/goals/new',
    name: 'CarGoalCreate',   // step1) 자동차 목표 등록
    component: () => import('@/pages/car/CarGoalCreatePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/car/goals/:goalId/recommend',
    name: 'CarRecommend',   // step2) 자동차 로드맵 추천
    component: () => import('@/pages/car/CarRecommendPage.vue'),
    meta: { requiresAuth: true },
  },
];
