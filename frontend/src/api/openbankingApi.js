import instance from '@/api'; // 공통 axios 인스턴스

// 오픈뱅킹(openbanking) 도메인 API
// 응답은 ApiResponse 래핑 → data.data 로 벗겨서 반환함
// 흐름: (로그인 후) getStatus → 미연동이면 온보딩 → getAuthUrl → link
// userId 는 JWT(Authorization 헤더)에서 식별 - axios 인터셉터가 토큰을 자동으로 붙임
const BASE_URL = '/api/openbanking';

export default {
  // 연동 여부 조회 (로그인 직후 가드) — { linked: boolean, accountCount }
  async getStatus() {
    const { data } = await instance.get(`${BASE_URL}/status`);
    return data.data;
  },

  // 오픈뱅킹 인증 URL 발급 — { authUrl }
  async getAuthUrl() {
    const { data } = await instance.get(`${BASE_URL}/auth-url`);
    return data.data;
  },

  // 연동 가능한 계좌 목록 조회 (인증 후 "계좌 선택" 단계) —
  //   [{ fintechUseNum, bankCodeStd, bankName, accountType, productName, accountNumMasked,
  //      balance, openDate, maturityDate, interestRate, govMatchRate }]
  async getAccounts() {
    const { data } = await instance.get(`${BASE_URL}/accounts`);
    return data.data;
  },

  // 계좌 연동 (인증 콜백 code/state + 선택 계좌) — [{ fintechUseNum, bankName, productName, accountType, balance }]
  async link(payload) {
    const { data } = await instance.post(`${BASE_URL}/link`, payload);
    return data.data;
  },

  // 연동 해제
  async unlink() {
    const { data } = await instance.delete(`${BASE_URL}/link`);
    return data.data;
  },

  // 잔액·거래내역 동기화 — { syncedAt }
  async sync() {
    const { data } = await instance.post(`${BASE_URL}/sync`, null);
    return data.data;
  },
};
