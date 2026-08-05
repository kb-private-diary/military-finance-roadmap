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

  sendMessage(sessionId, content, forceInfo = false) {
    return api.post(`${BASE_URL}/messages`, { sessionId, content, forceInfo });
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
