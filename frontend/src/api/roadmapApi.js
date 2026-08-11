import instance from '@/api'; // api/index.js

// 로드맵(roadmap) 도메인 API
// 응답은 ApiResponse 래핑 → data.data로 벗겨서 반환
const BASE_URL = '/api/roadmap';

export default {
  // ROAD-API-01: 저장된 로드맵 목록 조회
  // category: all | travel | job | car | rent
  async findRoadmapList(category = 'all') {
    const { data } = await instance.get(`${BASE_URL}/goals/${category}`);
    return data.data;
  },

  // ROAD-API-02: 로드맵 목표 삭제
  async deleteRoadmapGoal(goalId, categoryId) {
    const { data } = await instance.delete(`${BASE_URL}/goals/${goalId}`, {
      params: {
        categoryId,
      },
    });

    return data.data;
  },
};