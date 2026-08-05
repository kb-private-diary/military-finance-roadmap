import instance from '@/api'; // 공통 axios 인스턴스

// 자취(rent) 도메인 API (FINAL 스펙 v2.1 §7)
// 응답은 ApiResponse 래핑 → data.data 로 벗겨서 반환함
// ⚠️ userId 는 JWT 연동 전 임시 @RequestParam임 (TODO: JWT 연동 후 제거)
const BASE_URL = '/api/rent';

export default {
  // 학교 검색 — 백엔드 실계약: GET /rent/schools?keyword= → [{ schoolId, schoolName, address }]
  async searchSchools(keyword) {
    const { data } = await instance.get(`${BASE_URL}/schools`, {
      params: { keyword },
    });
    return data.data;
  },

  // 지역 계층 조회 (Step1 지역 모드) — level: SIDO | SIGUNGU | DONG
  //   findRegions({ level: 'SIDO' })
  //   findRegions({ level: 'SIGUNGU', sidoCode: '26' })
  //   findRegions({ level: 'DONG', sigunguCode: '26260' })
  async findRegions(params) {
    const { data } = await instance.get(`${BASE_URL}/regions`, { params });
    return data.data; // [{ regionCode, regionName, sigunguCode }]
  },

  // 목표 생성 (Step1 → 매물 보기) — { goalId, maturityAmount }
  async createGoal(payload, userId) {
    const { data } = await instance.post(`${BASE_URL}/goals`, payload, {
      params: { userId },
    });
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

  // 거주기간 슬라이더 재계산 (Step3) — { costBreakdown, affordability }
  async simulate(listingId, goalId, months) {
    const { data } = await instance.get(
      `${BASE_URL}/listings/${listingId}/simulate`,
      { params: { goalId, months } },
    );
    return data.data;
  },

  // 금융상품 조회 (Step4) — { months, gap, products[] }
  async findProducts(goalId, listingId, months) {
    const { data } = await instance.get(`${BASE_URL}/goals/${goalId}/products`, {
      params: { listingId, months },
    });
    return data.data;
  },

  // 로드맵 저장 (Step4 → 5) — body: { listingId, months, selectedProductIds }
  async confirmGoal(goalId, payload, userId) {
    const { data } = await instance.post(
      `${BASE_URL}/goals/${goalId}/confirm`,
      payload,
      { params: { userId } },
    );
    return data.data; // { goalId, status, months, redirectUrl }
  },

  // 저장된 목표 상세 (Step5) — { goal, nearbyFacilities, precisionSimulation }
  async findGoal(goalId) {
    const { data } = await instance.get(`${BASE_URL}/goals/${goalId}`);
    return data.data;
  },
};
