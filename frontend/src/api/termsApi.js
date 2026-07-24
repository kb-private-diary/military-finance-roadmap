import instance from '@/api'; // api/index.js

const BASE_URL = '/api/users';

export default {
  // 약관 목록 조회 (MEM-API: GET /api/users/terms)
  async findTerms() {
    const { data } = await instance.get(`${BASE_URL}/terms`);
    return data.data; // ApiResponse<List<TermsDTO>> 래핑 해제
  },
};
