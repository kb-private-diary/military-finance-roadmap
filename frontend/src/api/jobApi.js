import instance from '@/api'; // api/index.js

const BASE_URL = '/api/job';

export default {
  // 취업·공무원 대분류·중분류 조회
  async findCategoryList(goalType) {
    const { data } = await instance.get(`${BASE_URL}/categories`, {
      params: { goalType },
    });

    return data.data;
  },

  // 편입 대학 목록 조회
  async findTransferUniversityList() {
    const { data } = await instance.get(`${BASE_URL}/transfer-universities`);

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
    const { data } = await instance.post(`${BASE_URL}/goals`, requestDTO);

    return data.data;
  },

  // 작성 중인 진로 목표 수정
  async updateJobGoal(goalId, requestDTO) {
    const { data } = await instance.patch(
      `${BASE_URL}/goals/${goalId}`,
      requestDTO,
    );

    return data.data;
  },

  // 작성 중인 진로 목표 조회
  async findCurrentJobGoal() {
    const { data } = await instance.get(`${BASE_URL}/goals/current`);

    return data.data;
  },

  // 자격증·어학 및 인강 추천 조회
  async findPrepItemRecommend(goalId) {
    const { data } = await instance.get(
      `${BASE_URL}/goals/${goalId}/prep-items`,
    );

    return data.data;
  },

  // 선택한 직무와 지역 기준 고용24 훈련과정 추천 조회
  async findTrainingRecommend(goalId, regionCode) {
    const { data } = await instance.get(
      `${BASE_URL}/goals/${goalId}/trainings`,
      {
        params: { regionCode },
      },
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
    const { data } = await instance.get(`${BASE_URL}/goals/${goalId}/services`);

    return data.data;
  },

  // 목표 상세 조회
  async findJobGoalDetail(goalId) {
    const { data } = await instance.get(`${BASE_URL}/goals/${goalId}`);

    return data.data;
  },

  // 진로 로드맵 저장 확정
  async confirmJobGoal(goalId) {
    const { data } = await instance.post(`${BASE_URL}/goals/${goalId}/confirm`);

    return data.data;
  },
};
