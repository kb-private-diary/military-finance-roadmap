import api from '@/api';

const BASE_URL = '/api/chat';

export default {
  createSession(userId, title) {
    return api.post(`${BASE_URL}/sessions`, { userId, title });
  },

  listSessions(userId) {
    // GET /sessions는 Pydantic 모델이 아니라 순수 쿼리 파라미터(user_id)라서
    // CamelModel의 camelCase 자동 변환이 적용되지 않는다 - 백엔드 파라미터명 그대로 보낸다.
    return api.get(`${BASE_URL}/sessions`, { params: { user_id: userId } });
  },

  getHistory(sessionId) {
    return api.get(`${BASE_URL}/history/${sessionId}`);
  },

  sendMessage(sessionId, content) {
    return api.post(`${BASE_URL}/messages`, { sessionId, content });
  },

  getFaqCategories() {
    return api.get(`${BASE_URL}/faq-categories`);
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
