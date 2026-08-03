// simulator 도메인 axios 호출 — 군적금 상세내역 조회 + 모의 계산(동일/구간별 금액)
import instance from '@/api'; // api/index.js

const BASE_URL = '/api/simulator';

export default {
  // 군적금 예상 만기 수령액 상세 조회 (SIM-API-01: GET /api/simulator/saving-details)
  async findSavingDetails(userId) {
    const { data } = await instance.get(`${BASE_URL}/saving-details`, {
      params: { userId },
    });
    return data.data; // ApiResponse<SimulatorSavingDetailsResponseDTO> 래핑 해제
  },

  // 만기수령액 모의 계산 - 동일 금액 (SIM-API-02: POST /api/simulator/calculate/constant)
  async calculateConstant({ monthlySave, saveMonths }) {
    const { data } = await instance.post(`${BASE_URL}/calculate/constant`, {
      monthlySave,
      saveMonths,
    });
    return data.data; // ApiResponse<SimulatorCalculateResponseDTO> 래핑 해제
  },

  // 만기수령액 모의 계산 - 구간별 금액 (SIM-API-02: POST /api/simulator/calculate/variable)
  async calculateVariable(periods) {
    const { data } = await instance.post(`${BASE_URL}/calculate/variable`, {
      periods,
    });
    return data.data; // ApiResponse<SimulatorCalculateResponseDTO> 래핑 해제
  },

  // 현재 납입금 기준 중도해지 수령액 및 손실금 조회 (SIM-API-04: GET /api/simulator/saving-loss)
  async findSavingLoss(userId) {
    const { data } = await instance.get(`${BASE_URL}/saving-loss`, {
      params: { userId },
    });
    return data.data; // ApiResponse<SimulatorSavingLossResponseDTO> 래핑 해제
  },
};
