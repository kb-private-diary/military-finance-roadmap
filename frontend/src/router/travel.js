// travel 라우트 (담당: 태석)
// 화면 파일 위치: @/pages/travel/
export default [
  {
    path: '/travel/goals/new',
    name: 'TravelGoalCreate',   // step1) 여행 목표 등록
    component: () => import('@/pages/travel/TravelGoalCreatePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/travel/goals/:goalId/cost',
    name: 'TravelCost',   // step2) 여행 비용 계산
    component: () => import('@/pages/travel/TravelCostPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/travel/goals/:goalId/places',
    name: 'TravelPlaces',   // step3) 여행지 정보 추천
    component: () => import('@/pages/travel/TravelPlacesPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/travel/goals/:goalId/packages',
    name: 'TravelPackages',   // step3) 여행 패키지 추천
    component: () => import('@/pages/travel/TravelPackagesPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/travel/goals/:goalId/products',
    name: 'TravelProducts',   // step4) 여행 금융상품 혜택 추천
    component: () => import('@/pages/travel/TravelProductsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/travel/goals/:goalId',
    name: 'TravelGoalDetail',   // 여행 목표 상세
    component: () => import('@/pages/travel/TravelGoalDetailPage.vue'),
    meta: { requiresAuth: true },
  },
];
