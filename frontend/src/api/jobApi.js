import instance from '@/api'; // api/index.js

const BASE_URL = '/api/job';

export default {
  // JOB-API-01: goalType별 직무·직렬·학과 코드 조회
  async findJobCodes(goalType) {
    const { data } = await instance.get(`${BASE_URL}/codes`, { params: { goalType } });
    return data.data;
  },

  // JOB-API-02: 진로 목표 신규 등록
  async createJobGoal(requestDTO) {
    const { data } = await instance.post(`${BASE_URL}/goals`, requestDTO);
    return data.data;
  },

  // JOB-API-04: 준비항목 추천 조회
  async findPrepItemRecommend(goalId) {
    const { data } = await instance.get(`${BASE_URL}/goals/${goalId}/prep-items`);
    return data.data;
  },

  // JOB-API-05: 준비항목 선택 저장
  async createJobPlans(goalId, prepCritIds) {
    const { data } = await instance.post(`${BASE_URL}/goals/${goalId}/plans`, { prepCritIds });
    return data.data;
  },
};