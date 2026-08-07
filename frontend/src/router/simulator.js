// simulator 라우트 (담당: 석윤)
// 화면 파일 위치: @/pages/simulator/
export default [
  {
    path: '/simulator',
    name: 'Simulator', // 군적금 시뮬레이터 (모의 계산은 이 화면의 바텀시트로 통합)
    component: () => import('@/pages/simulator/SimulatorPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/simulator/calc',
    name: 'SimulatorCalc', // 군적금 계산 — SimulatorPage 위 바텀시트를 열어서 보여준다
    component: () => import('@/pages/simulator/SimulatorPage.vue'),
    meta: { requiresAuth: true, openCalc: true },
  },
  {
    path: '/simulator/products',
    name: 'SimulatorProductList', // 예적금/정책 상품 전체 목록 (시뮬레이터 "더보기")
    component: () => import('@/pages/simulator/SimulatorProductListPage.vue'),
    meta: { requiresAuth: true },
  },
];
