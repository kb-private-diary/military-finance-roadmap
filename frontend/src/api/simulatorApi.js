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
};
