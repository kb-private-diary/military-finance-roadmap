// dashboard 라우트 (담당: 석윤)
// 화면 파일 위치: @/pages/dashboard/
export default [
  {
    path: '/dashboard',
    name: 'Dashboard',   // 대시보드
    component: () => import('@/pages/dashboard/DashboardPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/dashboard/vacation',
    name: 'VacationEdit',   // 휴가 추가/수정
    component: () => import('@/pages/dashboard/VacationEditPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/dashboard/vacation/regular',
    name: 'VacationRegular',   // 정기 휴가 수정
    component: () => import('@/pages/dashboard/VacationRegularPage.vue'),
    meta: { requiresAuth: true },
  },
];
