import instance from '@/api'; // api/index.js

const BASE_URL = '/api/job';

export default {
  // JOB-API-01: goalType별 직무·직렬·학과 코드 조회
  async findJobCodes(goalType) {
    const { data } = await instance.get(`${BASE_URL}/codes`, {
      params: { goalType },
    });

    return data.data;
  },

  // 취업·공무원 대분류·중분류 조회
  async findCategoryList(goalType) {
    const { data } = await instance.get(`${BASE_URL}/categories`, {
      params: { goalType },
    });

    return data.data;
  },

  // 편입 대학 목록 조회
  async findTransferUniversityList() {
    const { data } = await instance.get(
      `${BASE_URL}/transfer-universities`,
    );

    return data.data;
  },

  // 선택 대학의 편입 학과계열 조회
  async findTransferMajorList(univId) {
    const { data } = await instance.get(
      `${BASE_URL}/transfer-universities/${univId}/majors`,
    );

    return data.data;
  },

  // 진로 목표 신규 등록
  async createJobGoal(requestDTO) {
    const { data } = await instance.post(
      `${BASE_URL}/goals`,
      requestDTO,
    );

    return data.data;
  },

  // 자격증·어학 및 인강 추천 조회
  async findPrepItemRecommend(goalId) {
    const { data } = await instance.get(
      `${BASE_URL}/goals/${goalId}/prep-items`,
    );

    return data.data;
  },

  // 선택한 자격증·어학 및 인강 저장
  async createJobPlans(goalId, requestDTO) {
    const { data } = await instance.post(
      `${BASE_URL}/goals/${goalId}/plans`,
      requestDTO,
    );

    return data.data;
  },

  // 정책·KB 서비스·카드 추천 조회
  async findServiceRecommend(goalId) {
    const { data } = await instance.get(
      `${BASE_URL}/goals/${goalId}/services`,
    );

    return data.data;
  },

  // 목표 상세 조회
  async findJobGoalDetail(goalId) {
    const { data } = await instance.get(
      `${BASE_URL}/goals/${goalId}`,
    );

    return data.data;
  },
};
