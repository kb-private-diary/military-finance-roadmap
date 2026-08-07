import instance from '@/api'; // 공통 axios 인스턴스

const BASE_URL = '/api/main';

export default {
  async findSummary() {
    const { data } = await instance.get(`${BASE_URL}/summary`);
    return data.data;
  },
};
