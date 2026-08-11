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

  // 휴가 목록(요약 + 카드 목록) 조회 (DASH-API-04: GET /api/dashboard/vacations, userId는 JWT에서 식별)
  async findVacations() {
    const { data } = await instance.get(`${BASE_URL}/vacations`);
    return data.data; // ApiResponse<DashboardVacationListResponseDTO> 래핑 해제
  },

  // 휴가 상세 조회 (DASH-API-05: GET /api/dashboard/vacations/{vacationId})
  async findVacationDetail(vacationId) {
    const { data } = await instance.get(`${BASE_URL}/vacations/${vacationId}`);
    return data.data; // ApiResponse<DashboardVacationDetailResponseDTO> 래핑 해제
  },

  // 휴가 등록 (DASH-API-06: POST /api/dashboard/vacations, REGULAR 제외 - 가입 시 자동 부여)
  async createVacation(payload) {
    const { data } = await instance.post(`${BASE_URL}/vacations`, payload);
    return data.data; // ApiResponse<Long> 래핑 해제 (생성된 vacationId)
  },

  // 휴가 수정 (DASH-API-07: PUT /api/dashboard/vacations/{vacationId}, REGULAR 제외)
  async updateVacation(vacationId, payload) {
    await instance.put(`${BASE_URL}/vacations/${vacationId}`, payload);
  },

  // 휴가 삭제 (DASH-API-08: DELETE /api/dashboard/vacations/{vacationId}, REGULAR 제외)
  async deleteVacation(vacationId) {
    await instance.delete(`${BASE_URL}/vacations/${vacationId}`);
  },

  // 휴가 사용내역 등록 (DASH-API-09: POST /api/dashboard/vacations/{vacationId}/usages)
  async createVacationUsage(vacationId, payload) {
    const { data } = await instance.post(
      `${BASE_URL}/vacations/${vacationId}/usages`,
      payload,
    );
    return data.data; // ApiResponse<Long> 래핑 해제 (생성된 historyId)
  },

  // 휴가 사용내역 삭제 (DASH-API-10: DELETE /api/dashboard/vacations/{vacationId}/usages/{historyId})
  async deleteVacationUsage(vacationId, historyId) {
    await instance.delete(`${BASE_URL}/vacations/${vacationId}/usages/${historyId}`);
  },
};
