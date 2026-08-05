import instance from '@/api'; // 공통 axios 인스턴스

// 오픈뱅킹(openbanking) 도메인 API
// 응답은 ApiResponse 래핑 → data.data 로 벗겨서 반환함
// 흐름: (로그인 후) getStatus → 미연동이면 온보딩 → getAuthUrl → link
// ⚠️ userId 는 JWT 연동 전 임시 @RequestParam임 (TODO: JWT 연동 후 제거)
const BASE_URL = '/api/openbanking';

export default {
  // 연동 여부 조회 (로그인 직후 가드) — { linked: boolean, accountCount }
  async getStatus(userId) {
    const { data } = await instance.get(`${BASE_URL}/status`, {
      params: { userId },
    });
    return data.data;
  },

  // 오픈뱅킹 인증 URL 발급 — { authUrl }
  async getAuthUrl(userId) {
    const { data } = await instance.get(`${BASE_URL}/auth-url`, {
      params: { userId },
    });
    return data.data;
  },

  // 계좌 연동 (인증 콜백 code/state + 선택 계좌) — [{ fintechUseNum, bankName, productName, accountType, balance }]
  async link(userId, payload) {
    const { data } = await instance.post(`${BASE_URL}/link`, payload, {
      params: { userId },
    });
    return data.data;
  },

  // 연동 해제
  async unlink(userId) {
    const { data } = await instance.delete(`${BASE_URL}/link`, {
      params: { userId },
    });
    return data.data;
  },

  // 잔액·거래내역 동기화 — { syncedAt }
  async sync(userId) {
    const { data } = await instance.post(`${BASE_URL}/sync`, null, {
      params: { userId },
    });
    return data.data;
  },
};
