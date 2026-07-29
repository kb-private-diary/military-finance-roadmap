import instance from '@/api'; // api/index.js

const BASE_URL = '/api/users';

export default {
  // 아이디 중복 확인 (MEM-API: GET /api/users/check-id/{userId})
  async checkDuplicate(userId) {
    const { data } = await instance.get(`${BASE_URL}/check-id/${userId}`);
    return data.data; // true면 이미 사용중
  },

  // 회원가입 1단계: 기본정보 검증만, 계정 생성 안 함 (MEM-API: POST /api/users/join)
  async checkJoinBasic(basic) {
    await instance.post(`${BASE_URL}/join`, basic);
  },

  // 회원가입 2단계: 상세정보 + 약관동의로 계정 생성 (MEM-API: POST /api/users/join/detail)
  async createMember(detail) {
    const { data } = await instance.post(`${BASE_URL}/join/detail`, detail);
    return data.data; // 생성된 회원 id
  },

  // 이름+전화번호로 아이디 찾기, 마스킹된 아이디 반환 (MEM-API: POST /api/users/find-id)
  async findUserId(request) {
    const { data } = await instance.post(`${BASE_URL}/find-id`, request);
    return data.data; // { maskedUserId }
  },

  // 본인확인(아이디+이름+전화번호) 후 비밀번호 재설정 (MEM-API: POST /api/users/find-pw)
  async resetPassword(request) {
    await instance.post(`${BASE_URL}/find-pw`, request);
  },

  // 마이페이지 - 비밀번호 변경, 로그인 상태에서 현재 비밀번호 확인 후 변경 (MEM-API: PUT /api/users/password)
  async changePassword(request) {
    await instance.put(`${BASE_URL}/password`, request);
  },
};
