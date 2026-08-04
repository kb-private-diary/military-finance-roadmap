import instance from '@/api'; // api/index.js

const BASE_URL = '/api/dashboard';

export default {
  // 복무 기본 정보 조회 (DASH-API-01: GET /api/dashboard/basic, userId는 JWT에서 식별)
  async findBasicInfo() {
    const { data } = await instance.get(`${BASE_URL}/basic`);
    return data.data; // ApiResponse<DashboardBasicResponseDTO> 래핑 해제
  },

  // 군적금 납입 현황 조회 (DASH-API-03: GET /api/dashboard/savings, userId는 JWT에서 식별)
  async findSavingsStatus() {
    const { data } = await instance.get(`${BASE_URL}/savings`);
    return data.data; // ApiResponse<DashboardSavingsResponseDTO> 래핑 해제
  },
};
