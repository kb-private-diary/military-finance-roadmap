import api from '@/api';

const BASE_URL = '/api/chat';

export default {
  createSession(title) {
    // userId는 더 이상 안 보냄 - 백엔드가 JWT(Authorization 헤더, axios 인터셉터가 자동으로 붙임)에서 추출
    return api.post(`${BASE_URL}/sessions`, { title });
  },

  listSessions() {
    return api.get(`${BASE_URL}/sessions`);
  },

  getHistory(sessionId) {
    return api.get(`${BASE_URL}/history/${sessionId}`);
  },

  // 유저가 그동안 나눈 대화 전체(세션이 여러 개로 나뉘어 있어도 다 합쳐서) - 날짜순으로 반환됨.
  // 대화가 하루 단위로 안 끊기고 하나로 이어지게 보여줄 때 씀(진입 시 히스토리 로드).
  getAllHistory() {
    return api.get(`${BASE_URL}/history`);
  },

  sendMessage(sessionId, content, forceInfo = false) {
    return api.post(`${BASE_URL}/messages`, { sessionId, content, forceInfo });
  },

  // 버튼으로 진행하는 되묻기·상품 목록 등 - AI 호출 없이 화면 문구를 그대로 기록만 한다
  // (새로고침·재로그인 후에도 대화가 안 사라지게 하기 위한 용도)
  logMessage(sessionId, role, content) {
    return api.post(`${BASE_URL}/messages/log`, { sessionId, role, content });
  },

  listGlossary() {
    return api.get(`${BASE_URL}/glossary`);
  },

  getGlossaryTerm(term) {
    return api.get(`${BASE_URL}/glossary/${encodeURIComponent(term)}`);
  },

  createFeedback({ sessionId, messageId, feedback, reason }) {
    return api.post(`${BASE_URL}/feedback`, { sessionId, messageId, feedback, reason });
  },

  getRecommendations(messageId) {
    return api.get(`${BASE_URL}/messages/${messageId}/recommendations`);
  },

  listProducts(category) {
    return api.get(`${BASE_URL}/products`, { params: category ? { category } : undefined });
  },

  getProduct(name, category) {
    return api.get(`${BASE_URL}/products/${encodeURIComponent(name)}`, {
      params: category ? { category } : undefined,
    });
  },
};
