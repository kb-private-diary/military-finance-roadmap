// rent 라우트 (담당: 수연)
// 화면 파일 위치: @/pages/rent/
export default [
  {
    path: '/rent/goals/new',
    name: 'RentGoalCreate',   // step1) 자취 목표 등록
    component: () => import('@/pages/rent/RentGoalCreatePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/rent/goals/:goalId/listings',
    name: 'RentListingList',   // step2) 자취 매물 리스트
    component: () => import('@/pages/rent/RentListingListPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/rent/listings/:listingId',
    name: 'RentListingDetail',   // step2) 자취 매물 상세
    component: () => import('@/pages/rent/RentListingDetailPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/rent/goals/:goalId/products',
    name: 'RentProducts',   // step4) 자취 금융상품 추천
    component: () => import('@/pages/rent/RentProductsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/rent/goals/:goalId',
    name: 'RentGoalDetail',   // 자취 목표 상세
    component: () => import('@/pages/rent/RentGoalDetailPage.vue'),
    meta: { requiresAuth: true },
  },
];
