import instance from '@/api'; // 공통 axios 인스턴스

// 후회소비(regret) 도메인 API
// 응답은 ApiResponse 래핑 → data.data 로 벗겨서 반환함
// userId 는 JWT(Authorization 헤더)에서 식별 - axios 인터셉터가 토큰을 자동으로 붙임
const BASE_URL = '/api/regret';

export default {
  // 지출 목록 (회고 태깅 상태 포함) — [{ spendingId, merchantName, category, amount, spentAt, reviewType }]
  async findSpendings() {
    const { data } = await instance.get(`${BASE_URL}/spendings`);
    return data.data;
  },

  // 지출 만족/후회 태깅 — reviewType: 'SATISFIED' | 'REGRET' (재태깅 시 갱신)
  async tagReview(spendingId, reviewType) {
    const { data } = await instance.post(
      `${BASE_URL}/reviews`,
      { spendingId, reviewType },
    );
    return data.data;
  },

  // 월별 후회소비 통계 — yearMonth='yyyyMM'
  //   { totalSpending, regretAmount, satisfiedAmount, regretCount, satisfiedCount, untaggedCount, categoryRegrets[] }
  async getMonthlyStats(yearMonth) {
    const { data } = await instance.get(`${BASE_URL}/stats`, {
      params: { yearMonth },
    });
    return data.data;
  },

  // 최근 N개월 월평균 지출·후회 요약 (자취 Step5 연동) — { avgMonthlySpending, avgRegretSpending }
  async getSpendingSummary(months = 3) {
    const { data } = await instance.get(`${BASE_URL}/spending/summary`, {
      params: { months },
    });
    return data.data;
  },
};
