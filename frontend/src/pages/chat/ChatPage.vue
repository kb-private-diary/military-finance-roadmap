<script setup>
// SCR-CHAT-01 · 챗봇  (담당: 에스더)
// FastAPI 챗봇 대화창 (JWT 공유) — 프로토타입(MilitaryChatbot.jsx) 기반, 실제 백엔드(chatApi) 연동
import { computed, nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import chatApi from '@/api/chatApi';
import { useAuthStore } from '@/stores/auth';
import { useToast } from '@/composables/useToast';
import { formatDate } from '@/util/format';
import mascotImg from '@/assets/chat-mascot.png';
import moodLikeImg from '@/assets/chat-mood-like.png';
import moodNeutralImg from '@/assets/chat-mood-neutral.png';
import moodDislikeImg from '@/assets/chat-mood-dislike.png';

const router = useRouter();
const auth = useAuthStore();
const { show: showToast } = useToast();

const userId = computed(() => auth.state.user.id);
const userName = computed(() => auth.state.user.name || '고객');

// 페이지 이동은 항상 이 함수를 거침. router.push는 문자열 경로/{name} 객체를 모두 받으므로
// target 자리에 실제 확정된 라우트는 { name: 'X' }, 아직 안 정해진 자리는 문자열 경로(TODO)를 넣는다.
const goTo = (target) => {
  router.push(target);
};

// 챗봇 화면은 공통 헤더(AppHeader) 대신 자체 헤더를 쓰기로 팀 협의됨 (다른 은행 챗봇 UX 참고)
const goBack = () => {
  router.back();
};
const goHome = () => {
  router.push({ name: 'Home' });
};

/* 전역 준비 바로가기 - 챗봇 밖 다른 팀원 화면으로 이동. 각 로드맵의 1단계(목표 등록)로 연결한다
   (라우트정의서 기준, 2026-07-30) */
const EXTERNAL_NAV = [
  { label: '여행 계획 세우기', to: { name: 'TravelGoalCreate' } }, // 태석님
  { label: '자취 준비하기', to: { name: 'RentGoalCreate' } }, // 수연님
  { label: '진로 준비하기', to: { name: 'JobGoalCreate' } }, // 지원님
  { label: '자차 준비하기', to: { name: 'CarGoalCreate' } }, // 호빈님
];

/* 하단 태그줄 - 자주 묻는 질문 빼곤 대부분 다른 페이지로 이동.
   "적금률 비교"는 단순 이동이 아니라 상담형 로직(WBS-6)으로 봐야 해서 아직 버튼에서 뺌 */
const EXTERNAL_TAGS = [
  { label: '군적금 활용하기', onClick: () => showAllFeatures() },
  { label: '후회소비 회고', to: { name: 'RegretReview' } },
  { label: '자금 시뮬레이션', to: { name: 'Simulator' } },
];

/* "군적금 활용하기" 클릭 시 보여줄 전체 기능 목록 (라우트정의서 기준, 2026-07-30) */
const ALL_FEATURES = [
  { label: '여행 계획 세우기', to: { name: 'TravelGoalCreate' } },
  { label: '자취 준비하기', to: { name: 'RentGoalCreate' } },
  { label: '진로 준비하기', to: { name: 'JobGoalCreate' } },
  { label: '자차 준비하기', to: { name: 'CarGoalCreate' } },
  { label: '군적금 시뮬레이션', to: { name: 'Simulator' } },
  { label: '대시보드 (D-Day·휴가 관리)', to: { name: 'Dashboard' } },
  { label: '후회소비 회고', to: { name: 'RegretDashboard' } },
  { label: '전우들과 비교(소셜)', to: { name: 'Social' } },
];

/* 다른 팀원이 만든 프로젝트 내 페이지로 연결 - 실제 키워드/경로는 페이지가 준비되는 대로 채워 넣으면 됨 */
const PAGE_LINKS = [
  {
    keywords: ['계산기', '이자 계산', '목돈 계산', '얼마 모'],
    label: '군적금 계산기 페이지로 이동',
    description: '군적금 납입 현황과 예상 만기 수령액을 계산해볼 수 있는 계산기 기능',
    to: { name: 'SimulatorCalc' },
  },
  {
    keywords: ['전세', '자취', '신혼', '매매', '집 마련'],
    label: '자취 준비 페이지로 이동',
    description: '자취 목표를 등록하고 예산에 맞는 매물·금융상품을 추천받는 자취 준비 기능',
    to: { name: 'RentGoalCreate' },
  },
];

/* RAG 정책 문서 3종 - 상품별 자주 묻는 질문(문구만). 실제 답변은 항상 chatApi.sendMessage로 받아온다 */
const PRODUCT_QUESTIONS = {
  장병내일준비적금: [
    '누가 가입할 수 있어요?',
    '한 달에 최대 얼마까지 넣을 수 있어요?',
    '정부에서도 지원해주나요?',
    '중도해지하면 손해봐요?',
  ],
  청년미래적금: [
    '누가 가입할 수 있어요?',
    '한 달에 얼마까지 넣을 수 있고 금리는 어떻게 돼요?',
    '정부기여금은 얼마나 받을 수 있어요?',
    '청년도약계좌에서 갈아탈 수 있나요?',
  ],
  청년주택드림청약통장: [
    '누가 가입할 수 있어요?',
    '금리는 얼마나 돼요?',
    '청약 당첨되면 어떻게 되나요?',
    '기존 청약통장에서 바꿀 수 있나요?',
  ],
};

/* 자주 묻는 질문 1단계 카테고리 - categoryId 기준(적금/청약은 실제 문서, 예금/투자는 준비중) */
const FAQ_CATEGORY_PRODUCTS = {
  savings: ['장병내일준비적금', '청년미래적금'],
  subscription: ['청년주택드림청약통장'],
  deposit: null,
  investment: null,
};

/* 목돈 상담 되묻기용 추천표 - 상담형 되묻기 API(WBS-6)가 아직 없어서 임시로 프론트에 유지 */
const RECO_TABLE = {
  '1년 이하_안정추구형': { product: '청년미래적금', note: '단기간 안전하게 모으기 좋은 조합이에요.' },
  '1년 이하_중립형': { product: '청년미래적금', note: '단기라도 정부기여금 혜택을 챙길 수 있어요.' },
  '1년 이하_공격투자형': {
    product: null,
    note: '1년 이하 단기에 공격적 투자는 리스크가 커요. 우선 예·적금으로 시작하는 걸 추천드려요.',
  },
  '1~3년_안정추구형': { product: '청년미래적금', note: '중기 목표에도 안전한 적금이 잘 맞아요.' },
  '1~3년_중립형': { product: '청년주택드림청약통장', note: '중기 목표라면 주택청약도 함께 고려해보세요.' },
  '1~3년_공격투자형': {
    product: null,
    note: '펀드·투자상품 데이터가 아직 준비 중이에요. 지금은 예·적금 조합을 우선 추천드려요.',
  },
  '3년 이상_안정추구형': { product: '청년주택드림청약통장', note: '장기 목표라면 청약통장으로 내 집 마련을 준비해보세요.' },
  '3년 이상_중립형': { product: '청년주택드림청약통장', note: '장기 목표엔 청약통장이 좋은 시작점이에요.' },
  '3년 이상_공격투자형': {
    product: null,
    note: '장기·공격투자 상품 데이터는 아직 준비 중이에요. 상담사 연결을 통해 자세히 안내받아보세요.',
  },
};

/* 상담 만족도 3단계 - value는 백엔드 feedback 값(like/neutral/dislike)과 그대로 매칭 */
const MOOD_OPTIONS = [
  { value: 'dislike', label: '불만', img: moodDislikeImg },
  { value: 'neutral', label: '보통', img: moodNeutralImg },
  { value: 'like', label: '만족', img: moodLikeImg },
];

/* 히스토리 제목용 - 가이드/태그 버튼 문구를 그대로 쓰지 않고 다듬어진 요약으로 보여줌 */
const TITLE_ALIASES = {
  '적금·청약 상품이 궁금해요': '적금·청약 상품 추천',
  '목돈 어떻게 쓸지 상담받기': '목돈 활용 상담',
  '정책 용어가 궁금해요': '정책 용어 설명',
  '직접 질문 입력하기': '직접 질문',
};

const loading = ref(true);
const loadError = ref('');
const sessionId = ref(null);
const messages = ref([]);
const input = ref('');
const typing = ref(false);
const panel = ref(null); // 'actions' (종료하기 버튼 노출)
const inputRef = ref(null);

// 만족도 설문 모달 상태
const feedbackModalOpen = ref(false);
const selectedFeedback = ref(null); // 'like' | 'neutral' | 'dislike'
const feedbackReason = ref('');
const feedbackSubmitting = ref(false);

// 다른 화면(공통 헤더) -> 챗봇은 헤더가 달라져서, 최소 이 시간만큼은 이동 랜딩 화면을 보여준다
const MIN_LANDING_MS = 500;
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const todayLabel = () => {
  const d = new Date();
  const weekday = ['일', '월', '화', '수', '목', '금', '토'][d.getDay()];
  return `- ${d.getFullYear()}. ${d.getMonth() + 1}. ${d.getDate()} ${weekday}요일 -`;
};

const formatBubbleTime = (value) => {
  const d = value ? new Date(value) : new Date();
  return d.toLocaleTimeString('ko-KR', { hour: 'numeric', minute: '2-digit' });
};

const scrollToBottom = () => {
  nextTick(() => {
    const container = document.querySelector('.app-content');
    container?.scrollTo({ top: container.scrollHeight, behavior: 'smooth' });
  });
};

const genId = () => `${Date.now()}-${Math.random()}`;

const pushBot = (msg) => {
  messages.value.push({ id: genId(), role: 'bot', time: formatBubbleTime(), ...msg });
  scrollToBottom();
};
const pushUser = (text) => {
  messages.value.push({ id: genId(), role: 'user', text, time: formatBubbleTime() });
  scrollToBottom();
};
const pushError = () => {
  messages.value.push({
    id: genId(),
    role: 'error',
    text: '서버 상의 오류가 있습니다. 잠시 후에 다시 시도해 주세요.',
  });
  scrollToBottom();
};

const backToGuide = () => {
  pushBot(buildGuideMessage());
};

// 어떤 답변에서든 처음 가이드 화면으로 돌아갈 수 있게 하는 공통 메뉴 항목
// (이름을 "메인으로"가 아니라 "처음으로"로 둔 이유: 앱 홈 화면(X 버튼)과 헷갈리지 않게)
const FIRST_MENU_ITEM = { label: '처음으로', onClick: backToGuide };

// 세션을 새로 여는 시점(오늘 첫 진입)에만 인사말+가이드를 함께 보여준다
const buildGreetAndGuide = () => [
  { id: 'greet', role: 'bot', time: formatBubbleTime(), text: `${userName.value}님, 안녕하세요! 어떤 내용이 궁금하세요?` },
  { id: 'guide', role: 'bot', time: formatBubbleTime(), ...buildGuideMessage() },
];

/* 0단계 초기 화면: [군 적금 로드맵] 카드(외부 이동) + [무엇이든 물어보세요] 카드(챗봇 내 대화) + 하단 태그줄 */
const buildGuideMessage = () => {
  return {
    sections: [
      {
        heading: '군 적금 로드맵',
        subtitle: '필요한 정보를 모아왔어요.',
        items: EXTERNAL_NAV.map((n) => ({ label: n.label, onClick: () => goTo(n.to) })),
      },
      {
        heading: '무엇이든 물어보세요',
        subtitle: '궁금한 걸 편하게 골라보세요.',
        items: [
          { label: '목돈 어떻게 쓸지 상담받기', onClick: () => openCounsel() },
          { label: '적금·청약 상품이 궁금해요', onClick: () => showAllProducts() },
          { label: '정책 용어가 궁금해요', onClick: () => openGlossary() },
          { label: '직접 질문 입력하기', onClick: () => freeform() },
        ],
      },
    ],
    // 이미 가이드 화면이라 "처음으로"는 여기선 의미가 없어서 빼고, 답변 메뉴에만 붙인다.
    tags: [
      ...EXTERNAL_TAGS.map((t) => ({ label: t.label, onClick: t.onClick ?? (() => goTo(t.to)) })),
      { label: '자주 묻는 질문', onClick: () => openFaqCategories() },
    ],
    caption: '챗봇은 질문 분석을 위해 AI를 활용하며, 서비스 개선 목적으로 사용됩니다.',
  };
};

const showAllProducts = () => {
  pushUser('적금·청약 상품이 궁금해요');
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    pushBot({
      text: '어떤 상품이 궁금하신가요?',
      menu: [
        ...Object.keys(PRODUCT_QUESTIONS).map((name) => ({ label: name, onClick: () => openDoc(name) })),
        FIRST_MENU_ITEM,
      ],
    });
  }, 700);
};

/* "군적금 활용하기" - 챗봇 밖 전체 기능 목록을 보여준다 */
const showAllFeatures = () => {
  pushUser('군적금 활용하기');
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    pushBot({
      text: '어떤 기능을 살펴보고 싶으신가요?',
      menu: [...ALL_FEATURES.map((f) => ({ label: f.label, onClick: () => goTo(f.to) })), FIRST_MENU_ITEM],
    });
  }, 700);
};

const openGlossary = async () => {
  pushUser('정책 용어가 궁금해요');
  panel.value = null;
  typing.value = true;
  try {
    const { data: terms } = await chatApi.listGlossary();
    typing.value = false;
    pushBot({
      text: '어떤 용어가 궁금하신가요?',
      menu: [...terms.map((t) => ({ label: t.term, onClick: () => openTerm(t.term) })), FIRST_MENU_ITEM],
    });
  } catch {
    typing.value = false;
    pushError();
  }
};

const openTerm = async (term) => {
  pushUser(term);
  panel.value = null;
  typing.value = true;
  try {
    const { data } = await chatApi.getGlossaryTerm(term);
    typing.value = false;
    pushBot({ title: term, text: data.definition, menu: [FIRST_MENU_ITEM] });
    panel.value = 'actions';
  } catch {
    typing.value = false;
    pushError();
  }
};

const openFaqCategories = async () => {
  pushUser('자주 묻는 질문');
  panel.value = null;
  typing.value = true;
  try {
    const { data: categories } = await chatApi.getFaqCategories();
    typing.value = false;
    pushBot({
      text: '어떤 카테고리가 궁금하신가요?',
      menu: [
        ...categories.map((c) => ({ label: c.label, onClick: () => openFaqCategory(c.categoryId, c.label) })),
        FIRST_MENU_ITEM,
      ],
    });
  } catch {
    typing.value = false;
    pushError();
  }
};

const openFaqCategory = (categoryId, label) => {
  pushUser(label);
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    const names = FAQ_CATEGORY_PRODUCTS[categoryId];
    if (!names) {
      pushBot({
        text: `${label} 관련 데이터는 아직 준비 중이에요. 곧 추가될 예정이니, 우선 다른 상품부터 확인해보시겠어요?`,
        menu: [FIRST_MENU_ITEM],
      });
      return;
    }
    pushBot({
      text: '어떤 상품이 궁금하신가요?',
      menu: [...names.map((name) => ({ label: name, onClick: () => openDoc(name) })), FIRST_MENU_ITEM],
    });
  }, 700);
};

/* 상품 소개 후 자주 묻는 질문을 하나씩 골라 물어볼 수 있게 함 - 이미 물어본 질문은 다음 메뉴에서 빠진다 */
const openDoc = (name) => {
  askProductQuestion(name, null);
};

const askProductQuestion = (name, askedQuestion) => {
  const remaining = (PRODUCT_QUESTIONS[name] || []).filter((q) => q !== askedQuestion);
  const extraMenu = remaining.map((q) => ({ label: q, onClick: () => askProductQuestion(name, q) }));
  askBackend(askedQuestion || name, { title: askedQuestion || name, extraMenu });
};

const freeform = () => {
  pushUser('직접 질문 입력하기');
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    pushBot({ text: '네, 궁금하신 내용을 편하게 입력해주세요 :)' });
    inputRef.value?.focus();
  }, 600);
};

/* 목돈 상담 - 되묻기형(목표기간 -> 투자성향 -> 추천) */
const openCounsel = () => {
  pushUser('목돈 어떻게 쓸지 상담받기');
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    startCounsel();
  }, 700);
};

const startCounsel = () => {
  pushBot({
    title: '자금 상담',
    text: '몇 가지만 여쭤볼게요.\n목표 기간이 어떻게 되세요?',
    menu: ['1년 이하', '1~3년', '3년 이상'].map((p) => ({ label: p, onClick: () => askType(p) })),
  });
};

const askType = (period) => {
  pushUser(period);
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    pushBot({
      text: '투자 성향은 어느 쪽에 가까우세요?',
      menu: ['안정추구형', '중립형', '공격투자형'].map((t) => ({
        label: t,
        onClick: () => finishCounsel(period, t),
      })),
    });
  }, 700);
};

const finishCounsel = (period, type) => {
  pushUser(type);
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    const reco = RECO_TABLE[`${period}_${type}`];
    if (reco.product) {
      pushBot({
        title: `${period} · ${type} 추천`,
        text: reco.note,
        menu: [{ label: `${reco.product} 자세히 보기`, onClick: () => openDoc(reco.product) }, FIRST_MENU_ITEM],
      });
    } else {
      pushBot({
        title: `${period} · ${type} 추천`,
        text: reco.note,
        menu: [FIRST_MENU_ITEM],
      });
    }
    panel.value = 'actions';
  }, 900);
};

/* 자유 입력 텍스트를 실제 백엔드(RAG/Gemini)로 보내고 답변을 받는다 */
const askBackend = async (text, { title, extraMenu = [] } = {}) => {
  pushUser(text);
  input.value = '';
  panel.value = null;
  typing.value = true;
  try {
    const { data: botMsg } = await chatApi.sendMessage(sessionId.value, text);
    typing.value = false;

    const menu = [...extraMenu, FIRST_MENU_ITEM];
    const pageLink = PAGE_LINKS.find((p) => p.keywords.some((k) => text.includes(k)));
    let answerText = botMsg.content;
    if (pageLink) {
      menu.unshift({ label: pageLink.label, onClick: () => goTo(pageLink.to) });
      // 버튼만 툭 주지 않고, 어떤 기능인지 먼저 설명하고 이동을 제안한다
      answerText += `\n\n저희 서비스에 ${pageLink.description}이 있는데, 확인해 보시겠습니까?`;
    }

    const bubble = {
      id: `bot-${botMsg.messageId}`,
      role: 'bot',
      time: formatBubbleTime(botMsg.createdDate),
      title,
      text: answerText,
      source: botMsg.source,
      sourceDetail: botMsg.sourceDetail,
      isAiGenerated: botMsg.isAiGenerated,
      menu,
    };
    messages.value.push(bubble);
    panel.value = 'actions';
    scrollToBottom();

    // 관련 콘텐츠 추천 (RAG-009) - 조회 실패해도 답변 자체엔 영향 없으니 조용히 무시
    // bubble(원본 객체)을 직접 수정하면 반응형 프록시를 거치지 않아 화면이 갱신되지 않으므로,
    // messages.value에서 다시 찾아 그 항목(프록시)에 대입한다.
    try {
      const { data: recos } = await chatApi.getRecommendations(botMsg.messageId);
      if (recos.length) {
        const target = messages.value.find((m) => m.id === bubble.id);
        if (target) target.recommendations = recos;
      }
    } catch {
      /* no-op */
    }
  } catch {
    typing.value = false;
    pushError();
  }
};

const submitInput = () => {
  const trimmed = input.value.trim();
  if (!trimmed) return;

  // 상담형 되묻기(WBS-6) API가 아직 없어 관련 키워드는 임시로 로컬 되묻기 흐름으로 유도
  if (trimmed.includes('투자') || trimmed.includes('상담') || trimmed.includes('돈 관리')) {
    pushUser(trimmed);
    input.value = '';
    panel.value = null;
    typing.value = true;
    setTimeout(() => {
      typing.value = false;
      startCounsel();
    }, 700);
    return;
  }

  askBackend(trimmed);
};

/* 만족도 설문 모달 - "종료하기" 클릭 시 오픈 */
const openFeedbackModal = () => {
  selectedFeedback.value = null;
  feedbackReason.value = '';
  feedbackModalOpen.value = true;
};

// X(닫기)/상담종료 둘 다 설문 제출 없이 모달만 닫는다. 상담종료는 대화 자체도 종료하는 의미라 메인 가이드로 돌아간다.
const closeFeedbackModal = () => {
  feedbackModalOpen.value = false;
};
const endConsult = () => {
  feedbackModalOpen.value = false;
  panel.value = null;
  backToGuide();
};

const selectFeedbackLevel = (value) => {
  selectedFeedback.value = value;
};

const submitFeedbackModal = async () => {
  if (!selectedFeedback.value || feedbackSubmitting.value) return;
  feedbackSubmitting.value = true;
  try {
    await chatApi.createFeedback({
      sessionId: sessionId.value,
      feedback: selectedFeedback.value,
      reason: selectedFeedback.value === 'dislike' ? feedbackReason.value.trim() || undefined : undefined,
    });
  } catch {
    showToast('피드백 저장에 실패했어요', 'error');
  } finally {
    feedbackSubmitting.value = false;
  }
  feedbackModalOpen.value = false;
  panel.value = null;
  pushBot({ text: '소중한 의견 감사합니다 🙌', menu: [FIRST_MENU_ITEM] });
};

/* 히스토리 제목 - 그 대화에서 처음 물어본 질문을 짧게 요약해서 타이틀로 사용 */
const summarizeTitle = (historyMessages) => {
  const placeholderLabels = ['직접 질문 입력하기'];
  const userMsgs = historyMessages.filter((m) => m.role === 'user');
  const firstUser = userMsgs.find((m) => !placeholderLabels.includes(m.content)) || userMsgs[0];
  if (!firstUser) return '새 대화';
  const t = firstUser.content;
  if (TITLE_ALIASES[t]) return TITLE_ALIASES[t];
  return t.length > 14 ? `${t.slice(0, 14)}…` : t;
};

const toBubble = (m) => {
  if (m.role === 'user') {
    return { id: `hist-${m.messageId}`, role: 'user', text: m.content, time: formatBubbleTime(m.createdDate) };
  }
  return {
    id: `hist-${m.messageId}`,
    role: 'bot',
    time: formatBubbleTime(m.createdDate),
    text: m.content,
    source: m.source,
    sourceDetail: m.sourceDetail,
    isAiGenerated: m.isAiGenerated,
    menu: [FIRST_MENU_ITEM],
  };
};

const openHistory = async () => {
  try {
    const { data: sessions } = await chatApi.listSessions(userId.value);
    if (!sessions.length) {
      pushBot({
        title: '최근 이전 대화',
        text: '아직 지난 대화가 없어요. 실제로 질문을 나누면 오늘 날짜로 자동으로 기록돼요.',
        menu: [FIRST_MENU_ITEM],
      });
      return;
    }
    const recent = sessions.slice(0, 3);
    const withTitles = await Promise.all(
      recent.map(async (s) => {
        try {
          const { data: history } = await chatApi.getHistory(s.sessionId);
          return { ...s, summary: summarizeTitle(history) };
        } catch {
          return { ...s, summary: '새 대화' };
        }
      }),
    );
    pushBot({
      title: '최근 이전 대화',
      text: '날짜를 골라주세요.',
      menu: withTitles.map((s) => ({
        label: `${formatDate(s.createdDate)} · ${s.summary}`,
        onClick: () => resumeSession(s.sessionId),
      })),
    });
  } catch {
    pushError();
  }
};

// 마지막 메시지가 봇 답변이면 "종료하기" 버튼을 다시 보여준다.
// (실시간 대화 중엔 답변 직후 panel='actions'가 되지만, 새로고침/재진입으로 히스토리를
// 불러올 때는 이 상태가 초기화되므로 다시 계산해줘야 한다)
const restorePanelFromHistory = (historyMessages) => {
  const last = historyMessages[historyMessages.length - 1];
  panel.value = last?.role === 'bot' ? 'actions' : null;
};

const resumeSession = async (targetSessionId) => {
  try {
    const { data: history } = await chatApi.getHistory(targetSessionId);
    sessionId.value = targetSessionId;
    messages.value = [
      { id: 'guide', role: 'bot', time: formatBubbleTime(), ...buildGuideMessage() },
      ...history.map(toBubble),
    ];
    restorePanelFromHistory(history);
    scrollToBottom();
  } catch {
    pushError();
  }
};

onMounted(async () => {
  loading.value = true;
  const landingStartedAt = Date.now();
  try {
    const { data: session } = await chatApi.createSession(userId.value);
    sessionId.value = session.sessionId;

    if (session.isNew) {
      messages.value = buildGreetAndGuide();
    } else {
      const { data: history } = await chatApi.getHistory(sessionId.value);
      if (history.length) {
        // 가이드 카드는 대화가 이어져도 계속 보여야 하는 진입점이라, 히스토리 앞에 항상 붙인다
        messages.value = [
          { id: 'guide', role: 'bot', time: formatBubbleTime(), ...buildGuideMessage() },
          ...history.map(toBubble),
        ];
        restorePanelFromHistory(history);
      } else {
        messages.value = buildGreetAndGuide();
      }
    }
    scrollToBottom();
  } catch {
    loadError.value = '챗봇을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.';
  } finally {
    const remaining = MIN_LANDING_MS - (Date.now() - landingStartedAt);
    if (remaining > 0) await wait(remaining);
    loading.value = false;
  }
});
</script>

<template>
  <div class="chat-page">
    <div v-if="loading" class="landing" role="status">
      <img :src="mascotImg" alt="마스코트" class="landing__mascot" width="48" height="48" />
      <p>챗봇 화면으로 이동 중입니다…</p>
    </div>

    <div v-else-if="loadError" class="status-box status-box--error text-caption" role="alert">
      <p>{{ loadError }}</p>
    </div>

    <template v-else>
      <div class="chat-header">
        <button type="button" class="chat-header__icon-btn" aria-label="뒤로가기" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M15 18L9 12L15 6"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
        <div class="chat-header__title">텅장일병일기</div>
        <div class="chat-header__spacer" />
        <button type="button" class="history-btn" aria-label="이전 대화" @click="openHistory">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M3 12A9 9 0 1 0 6 5.3M3 12V6M3 12H9"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path d="M12 8V12L15 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
          이전 기록
        </button>
        <button type="button" class="chat-header__icon-btn" aria-label="홈으로" @click="goHome">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6 6L18 18M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
        </button>
      </div>

      <div class="chat-page__date">{{ todayLabel() }}</div>

      <div class="chat-page__messages">
        <template v-for="msg in messages" :key="msg.id">
          <div v-if="msg.role === 'user'" class="bubble-row bubble-row--user">
            <div v-if="msg.time" class="bubble-time bubble-time--user">{{ msg.time }}</div>
            <div class="bubble bubble--user">{{ msg.text }}</div>
          </div>

          <div v-else-if="msg.role === 'error'" class="bubble-row">
            <div class="bubble bubble--error">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path
                  d="M12 9V13M12 17H12.01M10.29 3.86L1.82 18A1 1 0 0 0 2.68 19.5H21.32A1 1 0 0 0 22.18 18L13.71 3.86A1 1 0 0 0 10.29 3.86Z"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              {{ msg.text }}
            </div>
          </div>

          <div v-else-if="msg.sections" class="bubble-row">
            <div class="bot-header">
              <img :src="mascotImg" alt="마스코트" class="mascot" width="22" height="22" />
              <span class="bot-name">노이일병</span>
              <span v-if="msg.time" class="bubble-time">{{ msg.time }}</span>
            </div>
            <div class="guide-card">
              <div class="guide-card__sections">
                <div v-for="(section, si) in msg.sections" :key="si" class="guide-section">
                  <div class="guide-section__heading">{{ section.heading }}</div>
                  <div v-if="section.subtitle" class="guide-section__subtitle">{{ section.subtitle }}</div>
                  <button
                    v-for="(opt, i) in section.items"
                    :key="i"
                    type="button"
                    class="menu-btn menu-btn--narrow"
                    @click="opt.onClick"
                  >
                    {{ opt.label }}
                  </button>
                </div>
              </div>
              <div v-if="msg.tags" class="tag-row">
                <button v-for="(tag, i) in msg.tags" :key="i" type="button" class="tag-chip" @click="tag.onClick">
                  {{ tag.label }}
                </button>
              </div>
              <div v-if="msg.caption" class="guide-caption">{{ msg.caption }}</div>
            </div>
          </div>

          <div v-else class="bubble-row">
            <div class="bot-header">
              <img :src="mascotImg" alt="마스코트" class="mascot" width="22" height="22" />
              <span class="bot-name">노이일병</span>
              <span v-if="msg.time" class="bubble-time">{{ msg.time }}</span>
            </div>
            <div class="answer-card">
              <div class="bubble bubble--bot">
                <div v-if="msg.title" class="answer-title">{{ msg.title }}</div>
                <div class="answer-text">{{ msg.text }}</div>
                <div v-if="msg.isAiGenerated && msg.sourceDetail" class="answer-source">출처 · {{ msg.sourceDetail }}</div>
                <div v-else-if="msg.isAiGenerated && msg.source" class="answer-source">출처 · {{ msg.source }}</div>
                <div v-if="msg.isAiGenerated" class="answer-ai-caption">AI가 생성한 답변이에요</div>
              </div>

              <div v-if="msg.recommendations?.length" class="recommend-card">
                <div class="recommend-card__label">관련 콘텐츠 추천</div>
                <button
                  v-for="(r, i) in msg.recommendations"
                  :key="i"
                  type="button"
                  class="tag-chip"
                  @click="goTo(r.pageLink)"
                >
                  {{ r.label }}
                </button>
              </div>

              <div v-if="msg.menu" class="menu-col">
                <button v-for="(opt, i) in msg.menu" :key="i" type="button" class="menu-btn" @click="opt.onClick">
                  {{ opt.label }}
                </button>
              </div>
            </div>
          </div>
        </template>

        <div v-if="typing" class="bubble-row">
          <div class="bot-header">
            <img :src="mascotImg" alt="마스코트" class="mascot" width="22" height="22" />
            <span class="bot-name">노이일병</span>
          </div>
          <div class="typing-dots">
            <span v-for="i in 3" :key="i" :style="{ animationDelay: `${(i - 1) * 0.15}s` }" />
          </div>
        </div>

        <div class="chat-page__spacer" />
      </div>

      <div v-if="panel === 'actions' && !typing" class="chat-page__panel">
        <div class="panel-row">
          <button type="button" class="pill-btn" @click="openFeedbackModal">종료하기</button>
        </div>
      </div>

      <div v-if="feedbackModalOpen" class="feedback-modal-backdrop" @click.self="closeFeedbackModal">
        <div class="feedback-modal" role="dialog" aria-label="AI 챗봇 상담 만족도 조사">
          <button type="button" class="feedback-modal__close" aria-label="닫기" @click="closeFeedbackModal">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
              <path d="M6 6L18 18M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
          </button>

          <div class="feedback-modal__title">AI 챗봇 상담이 도움이 되셨나요?</div>

          <div class="mood-row">
            <button
              v-for="opt in MOOD_OPTIONS"
              :key="opt.value"
              type="button"
              class="mood-option"
              :class="[`mood-option--${opt.value}`, { 'mood-option--selected': selectedFeedback === opt.value }]"
              @click="selectFeedbackLevel(opt.value)"
            >
              <img :src="opt.img" :alt="opt.label" class="mood-option__img" width="68" height="68" />
              <span>{{ opt.label }}</span>
            </button>
          </div>

          <textarea
            v-if="selectedFeedback === 'dislike'"
            v-model="feedbackReason"
            class="feedback-modal__reason"
            rows="3"
            placeholder="불만을 느끼신 내용을 알려주시면 다음엔 더 발전하겠습니다."
          />

          <div class="feedback-modal__actions">
            <button type="button" class="feedback-modal__btn feedback-modal__btn--secondary" @click="endConsult">
              상담종료
            </button>
            <button
              type="button"
              class="feedback-modal__btn feedback-modal__btn--primary"
              :disabled="!selectedFeedback || feedbackSubmitting"
              @click="submitFeedbackModal"
            >
              설문완료
            </button>
          </div>
        </div>
      </div>

      <div class="chat-page__composer">
        <input
          ref="inputRef"
          v-model="input"
          type="text"
          placeholder="궁금한 점을 물어보세요"
          class="composer-input"
          @keydown.enter.prevent="submitInput"
        />
        <button
          type="button"
          class="composer-send"
          :disabled="!input.trim()"
          aria-label="전송"
          @click="submitInput"
        >
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="17" height="17">
            <path
              d="M3 12L21 3L14 21L11 13L3 12Z"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  margin: 0 -20px -20px;
  padding: 0 20px 20px;
  background: var(--surface-cream);
}

.status-box {
  margin-top: 80px;
  color: var(--text-muted);
  text-align: center;
}

.status-box--error {
  color: var(--danger);
}

.landing {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 160px;
  color: var(--text-muted);
  font-size: 13px;
  text-align: center;
}

.landing__mascot {
  object-fit: contain;
}

/* 챗봇 전용 자체 헤더 - 공통 AppHeader 대신 사용 (팀 협의된 예외, AppLayout.vue 주석 참고) */
.chat-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 -20px;
  padding: 12px 20px;
  background: #ffffff;
  border-bottom: 1px solid var(--line);
}

.chat-header__icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  flex-shrink: 0;
}

.chat-header__icon-btn svg {
  width: 20px;
  height: 20px;
}

.chat-header__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-strong);
}

.chat-header__spacer {
  flex: 1;
}

.history-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f5f5f5;
  border: 1px solid var(--line);
  border-radius: 8px;
  color: var(--text-muted);
  padding: 6px 9px;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  flex-shrink: 0;
}

.history-btn svg {
  width: 14px;
  height: 14px;
}

.chat-page__date {
  text-align: center;
  color: var(--text-muted);
  font-size: 11.5px;
  margin-top: 16px;
  margin-bottom: 14px;
}

.chat-page__messages {
  flex: 1;
  padding-bottom: 8px;
}

.bubble-row {
  margin-bottom: 28px;
}

.bubble-row--user {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.bubble-time {
  font-size: 10.5px;
  color: var(--text-hint);
  margin-bottom: 4px;
}

.bubble-time--user {
  align-self: flex-end;
}

.bubble--user {
  background: var(--kb-yellow);
  color: var(--text-strong);
  padding: 10px 14px;
  border-radius: 16px 16px 4px 16px;
  max-width: 78%;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
}

.bubble--error {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #fdeceb;
  border: 1px solid #f0b9b2;
  color: #a1493d;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13.5px;
}

.bot-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.mascot {
  flex-shrink: 0;
  object-fit: contain;
}

.bot-name {
  font-weight: 600;
  font-size: 13px;
  color: var(--text-strong);
}

.guide-card,
.answer-card {
  max-width: 94%;
  width: 100%;
}

.guide-card__sections {
  display: flex;
  gap: 8px;
  align-items: stretch;
}

.guide-section {
  flex: 1 1 0;
  min-width: 0;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px 10px 10px 12px;
  box-shadow: 0 1px 3px rgba(180, 150, 80, 0.08);
}

.guide-section__heading {
  display: inline-block;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 3px;
  font-size: 12.5px;
  background-image: linear-gradient(#fff0b3, #fff0b3);
  background-repeat: no-repeat;
  background-size: 100% 6px;
  background-position: 0 88%;
}

.guide-section__subtitle {
  color: var(--text-muted);
  font-size: 10.5px;
  margin-bottom: 7px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.tag-chip {
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 6px 11px;
  font-size: 11px;
  color: var(--text-body);
  cursor: pointer;
}

/* 브라우저 기본 포커스 링(파랑/청록색)이 버튼 색과 안 어울려서 톤에 맞는 것으로 교체 */
.chat-page button:focus-visible {
  outline: 2px solid var(--kb-yellow-deep);
  outline-offset: 1px;
}

.guide-caption {
  margin-top: 8px;
  font-size: 10.5px;
  color: var(--text-muted);
  line-height: 1.4;
}

.bubble--bot {
  background: #ffffff;
  border: 1px solid var(--line);
  color: var(--text-body);
  padding: 12px 14px;
  border-radius: 4px 16px 16px 16px;
  font-size: 14px;
  line-height: 1.55;
  box-shadow: 0 1px 3px rgba(180, 150, 80, 0.08);
}

.answer-title {
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 4px;
}

.answer-text {
  white-space: pre-line;
}

.answer-source {
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-hint);
}

.answer-ai-caption {
  margin-top: 2px;
  font-size: 10.5px;
  color: var(--text-hint);
}

.recommend-card {
  margin-top: 8px;
  background: #f5f5f5;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px 12px;
}

.recommend-card__label {
  color: var(--text-muted);
  font-size: 12.5px;
  font-weight: 700;
  margin-bottom: 6px;
}

.menu-col {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
}

.menu-btn {
  display: block;
  width: 100%;
  text-align: left;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 13px;
  color: var(--text-body);
  margin-top: 8px;
  cursor: pointer;
  font-weight: 500;
}

.menu-btn--narrow {
  border-radius: 8px;
  padding: 7px 8px;
  font-size: 11px;
  line-height: 1.3;
  margin-top: 6px;
}

.menu-btn--narrow:first-child {
  margin-top: 0;
}

.typing-dots {
  display: inline-flex;
  gap: 4px;
  padding: 10px 14px;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 4px 16px 16px 16px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--line-strong);
  animation: bounceDot 1.2s infinite ease-in-out;
}

@keyframes bounceDot {
  0%,
  80%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  40% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

.chat-page__spacer {
  height: 128px;
}

.chat-page__panel {
  position: fixed;
  bottom: 68px;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 393px;
  padding: 10px 16px;
  background: var(--surface-cream);
  box-sizing: border-box;
}

.panel-row {
  display: flex;
  gap: 8px;
}

.pill-btn {
  background: #ffffff;
  border: 1px solid var(--line);
  color: var(--text-strong);
  border-radius: 16px;
  font-size: 12px;
  padding: 6px 12px;
  cursor: pointer;
}

/* 만족도 설문 모달 */
.feedback-modal-backdrop {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  z-index: 20;
}

.feedback-modal {
  position: relative;
  width: calc(100% - 64px);
  max-width: 320px;
  background: #ffffff;
  border-radius: 16px;
  padding: 28px 20px 20px;
  box-sizing: border-box;
}

.feedback-modal__close {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
}

.feedback-modal__title {
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 20px;
}

.mood-row {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 16px;
}

.mood-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
}

.mood-option__img {
  object-fit: contain;
  border-radius: 50%;
  padding: 4px;
  filter: grayscale(1);
  opacity: 0.45;
  transition: filter 0.15s ease, opacity 0.15s ease, background-color 0.15s ease;
}

.mood-option span {
  color: var(--text-muted);
}

.mood-option--dislike.mood-option--selected .mood-option__img {
  background: color-mix(in srgb, var(--danger) 16%, transparent);
}

.mood-option--neutral.mood-option--selected .mood-option__img {
  background: color-mix(in srgb, var(--kb-gray) 16%, transparent);
}

.mood-option--like.mood-option--selected .mood-option__img {
  background: color-mix(in srgb, var(--mood-positive) 16%, transparent);
}

.mood-option--selected .mood-option__img {
  filter: none;
  opacity: 1;
}

.mood-option--selected span {
  color: var(--text-strong);
  font-weight: 700;
}

.feedback-modal__reason {
  width: 100%;
  box-sizing: border-box;
  resize: none;
  background: #f5f5f5;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 12.5px;
  font-family: inherit;
  color: var(--text-body);
  margin-bottom: 16px;
}

.feedback-modal__actions {
  display: flex;
  gap: 8px;
}

.feedback-modal__btn {
  flex: 1;
  height: 42px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.feedback-modal__btn--secondary {
  background: var(--kb-gray-pale);
  color: var(--kb-dark-gray);
}

.feedback-modal__btn--primary {
  background: var(--mood-positive);
  color: #ffffff;
}

.feedback-modal__btn--primary:disabled {
  background: var(--kb-gray-pale);
  color: var(--text-disabled);
  cursor: not-allowed;
}

.chat-page__composer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  width: 100%;
  max-width: 393px;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: var(--surface-cream);
  box-sizing: border-box;
}

.composer-input {
  flex: 1;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 20px;
  padding: 10px 14px;
  color: var(--text-body);
  font-size: 14px;
  outline: none;
}

.composer-send {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: var(--kb-yellow);
  color: var(--text-strong);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}

.composer-send:disabled {
  background: var(--kb-gray-pale);
  color: var(--text-disabled);
  cursor: not-allowed;
}
</style>
