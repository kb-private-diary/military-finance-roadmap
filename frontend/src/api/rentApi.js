import instance from '@/api'; // 공통 axios 인스턴스

// 자취(rent) 도메인 API (FINAL 스펙 v2.1 §7)
// 응답은 ApiResponse 래핑 → data.data 로 벗겨서 반환함
// userId 는 JWT(Authorization 헤더)에서 식별 - axios 인터셉터가 토큰을 자동으로 붙임
const BASE_URL = '/api/rent';

export default {
  // 학교 검색 — 백엔드 실계약: GET /rent/schools?keyword= → [{ schoolId, schoolName, address }]
  async searchSchools(keyword) {
    const { data } = await instance.get(`${BASE_URL}/schools`, {
      params: { keyword },
    });
    return data.data;
  },

  // 지역 계층 조회 (Step1 지역 모드) — 백엔드 실계약: GET /rent/regions (sido·sigunguCode)
  //   findRegions({})                    → 시/도 목록   (code=name=시도명)
  //   findRegions({ sido: '서울특별시' })  → 시/군/구 목록 (code=시군구코드, name=시군구명)
  //   findRegions({ sigunguCode: '11680' }) → 읍/면/동 목록 (code=법정동코드, name=읍면동명)
  async findRegions(params = {}) {
    const { data } = await instance.get(`${BASE_URL}/regions`, { params });
    return data.data; // [{ code, name }]
  },

  // 목표 생성 (Step1 → 매물 보기) — { goalId, maturityAmount }
  async createGoal(payload) {
    const { data } = await instance.post(`${BASE_URL}/goals`, payload);
    return data.data;
  },

  // 매물 리스트 (Step2) — { totalCount, listings[] }
  async findListings(goalId) {
    const { data } = await instance.get(`${BASE_URL}/goals/${goalId}/listings`);
    return data.data;
  },

  // 매물 상세 (Step3) — { listing, propertyBadges, costBreakdown, affordability }
  async findListingDetail(listingId, goalId, months) {
    const { data } = await instance.get(`${BASE_URL}/listings/${listingId}`, {
      params: { goalId, months },
    });
    return data.data;
  },

  // 감당도 조회 (Step3 재정 체크) — depositMode: INCLUDE(보증금 포함, 기본) | EXCLUDE(보증금은 전세대출로 제외)
  //   GET /listings/{listingId}/affordability?months=&depositMode= → affordability
  async findAffordability(listingId, months, depositMode = 'INCLUDE') {
    const { data } = await instance.get(
      `${BASE_URL}/listings/${listingId}/affordability`,
      { params: { months, depositMode } },
    );
    return data.data;
  },

  // 동네 시세 상세 비교 (Step3 바텀시트) — market-comparison
  //   GET /listings/{listingId}/market-comparison
  //   → { umdName, estateTypeLabel, sampleCount, enough, betterCount, totalItems,
  //       rows[{key,label,mine,avg,better}], rentMin, rentMax, rentAvg,
  //       rentPercentile, verdict, verdictTitle, verdictText }
  async findMarketComparison(listingId) {
    const { data } = await instance.get(
      `${BASE_URL}/listings/${listingId}/market-comparison`,
    );
    return data.data;
  },

  // 금융상품 조회 (Step4) — { months, gap, products[] }
  async findProducts(listingId, months) {
    const { data } = await instance.get(`${BASE_URL}/listings/${listingId}/products`, {
      params: { months },
    });
    return data.data;
  },

  // 로드맵 저장 (Step4 → 5) — body: { listingId, months, selectedProductIds }
  async confirmGoal(goalId, payload) {
    // 백엔드는 query 파라미터(?months=&listingId=)로 받는다 (body 아님)
    const { data } = await instance.post(
      `${BASE_URL}/goals/${goalId}/confirm`,
      null,
      { params: { months: payload.months, listingId: payload.listingId } },
    );
    return data.data; // { goalId, status, months, redirectUrl }
  },

  // 저장된 목표 상세 (Step5) — { goal, nearbyFacilities, precisionSimulation }
  async findGoal(goalId) {
    const { data } = await instance.get(`${BASE_URL}/goals/${goalId}`);
    return data.data;
  },
};
