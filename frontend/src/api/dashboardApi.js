import instance from '@/api'; // api/index.js

const BASE_URL = '/api/dashboard';

export default {
  // 복무 기본 정보 조회 (DASH-API-01: GET /api/dashboard/basic)
  async findBasicInfo(userId) {
    const { data } = await instance.get(`${BASE_URL}/basic`, {
      params: { userId },
    });
    return data.data; // ApiResponse<DashboardBasicResponseDTO> 래핑 해제
  },

  // 군적금 납입 현황 조회 (DASH-API-03: GET /api/dashboard/savings)
  async findSavingsStatus(userId) {
    const { data } = await instance.get(`${BASE_URL}/savings`, {
      params: { userId },
    });
    return data.data; // ApiResponse<DashboardSavingsResponseDTO> 래핑 해제
  },
};
