// simulator 라우트 (담당: 석윤)
// 화면 파일 위치: @/pages/simulator/
export default [
  {
    path: '/simulator',
    name: 'Simulator',   // 군적금 시뮬레이터
    component: () => import('@/pages/simulator/SimulatorPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/simulator/calc',
    name: 'SimulatorCalc',   // 군적금 계산
    component: () => import('@/pages/simulator/SimulatorCalcPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/simulator/products/:productId',
    name: 'SavingProductDetail',   // 적금 상품 상세
    component: () => import('@/pages/simulator/SavingProductDetailPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/simulator/policies/:policyId',
    name: 'PolicyProductDetail',   // 정책 상품 상세
    component: () => import('@/pages/simulator/PolicyProductDetailPage.vue'),
    meta: { requiresAuth: true },
  },
];
