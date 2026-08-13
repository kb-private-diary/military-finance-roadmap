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

  // productContext: 실시간 상품 상세를 보고 나서 그 상품 하나에 대해 후속 질문할 때만 넘김
  // (이미 화면에 표시한 그 상품의 정보 텍스트 그대로) - 백엔드가 카테고리 전체가 아니라
  // 이 상품 하나만 근거로 답하게 한다(2026-08-07)
  // Gemini 답변 생성이 가끔 15초를 넘겨서 공용 타임아웃(15초)에 걸려 취소되는 게 확인됨
  // (2026-08-12 발견, Network 탭에서 15.0초에 cancel되는 것 재현) - listProducts처럼 이 호출만
  // 넉넉하게 45초로 늘림.
  sendMessage(sessionId, content, forceInfo = false, productContext = null) {
    return api.post(`${BASE_URL}/messages`, { sessionId, content, forceInfo, productContext }, { timeout: 45000 });
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

  // 서버가 외부(금융감독원 등) API를 캐시 없이 처음 조회할 때만 30~50초씩 걸릴 수 있어서
  // (2026-08-11 발견), 공통 axios 기본 타임아웃(15초)보다 넉넉하게 이 호출에만 60초를 준다.
  // 다른 화면(홈/대시보드 등)에서 쓰는 공통 axios 인스턴스 설정은 그대로 안 건드림.
  listProducts(category) {
    return api.get(`${BASE_URL}/products`, { params: category ? { category } : undefined, timeout: 60000 });
  },

  getProduct(name, category) {
    return api.get(`${BASE_URL}/products/${encodeURIComponent(name)}`, {
      params: category ? { category } : undefined,
      timeout: 60000,
    });
  },
};
