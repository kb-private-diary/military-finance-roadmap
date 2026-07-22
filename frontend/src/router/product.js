// product 라우트 (담당: 석윤)
// 화면 파일 위치: @/pages/product/
export default [
  {
    path: '/products/:productId',
    name: 'SavingProductDetail',   // 적금 상품 상세
    component: () => import('@/pages/product/SavingProductDetailPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/policies/:policyId',
    name: 'PolicyProductDetail',   // 정책 상품 상세
    component: () => import('@/pages/product/PolicyProductDetailPage.vue'),
    meta: { requiresAuth: true },
  },
];
