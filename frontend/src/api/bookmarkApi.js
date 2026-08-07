import instance from '@/api'; //api/index.js

const BASE_URL = '/api/bookmarks';

export default {
  // 관심 로드맵 조회
  async findBookmarks() {
    const { data } = await instance.get(BASE_URL);

    return data.data;
  },

  // 관심 로드맵 등록
  async createBookmark(requestDTO) {
    const { data } = await instance.post(BASE_URL, requestDTO);

    return data.data;
  },

  // 관심 로드맵 해제
  async deleteBookmark(bookmarkId) {
    const { data } = await instance.delete(`${BASE_URL}/${bookmarkId}`);

    return data.data;
  },
};
