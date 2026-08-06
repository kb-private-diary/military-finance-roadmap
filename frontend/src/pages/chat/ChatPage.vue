<script setup>
// SCR-CHAT-01 · 챗봇  (담당: 에스더)
// FastAPI 챗봇 대화창 (JWT 공유) — 프로토타입(MilitaryChatbot.jsx) 기반, 실제 백엔드(chatApi) 연동
import { computed, nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import chatApi from '@/api/chatApi';
import simulatorApi from '@/api/simulatorApi';
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
  {
    label: '여행 계획 세우기',
    description: '여행 목표를 등록하고 예산에 맞는 여행 계획을 세울 수 있는 여행 준비 기능',
    to: { name: 'TravelGoalCreate' },
  }, // 태석님
  {
    label: '자취 준비하기',
    description: '자취 목표를 등록하고 예산에 맞는 매물·금융상품을 추천받는 자취 준비 기능',
    to: { name: 'RentGoalCreate' },
  }, // 수연님
  {
    label: '진로 준비하기',
    description: '진로 목표를 등록하고 준비 과정에 맞는 계획을 세울 수 있는 진로 준비 기능',
    to: { name: 'JobGoalCreate' },
  }, // 지원님
  {
    label: '자차 준비하기',
    description: '자동차 목표를 등록하고 예산에 맞는 차량·금융상품을 추천받는 자차 준비 기능',
    to: { name: 'CarGoalCreate' },
  }, // 호빈님
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
  {
    keywords: ['자동차', '자차', '차량', '벤츠', '차 사'],
    label: '자차 준비 페이지로 이동',
    description: '자동차 목표를 등록하고 예산에 맞는 차량·금융상품을 추천받는 자차 준비 기능',
    to: { name: 'CarGoalCreate' },
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

/* 상담(목적=투자 수익)에서 위험성향별 실시간 펀드를 추천할 때 쓰는 매핑.
   펀드 API(fndTp)엔 보유기간 데이터가 없어 목표기간은 필터링엔 못 쓰고 안내 문구에만 참고로 반영한다.
   변액보험은 보험 상품이라 중도해지 시 사업비·해지공제 손해 구조가 있어 추천 후보에서 제외한다. */
const FUND_RISK_TYPE = {
  단기금융: '안정추구형',
  채권형: '안정추구형',
  혼합채권형: '중립형',
  혼합자산: '중립형',
  재간접: '중립형',
  주식형: '공격투자형',
  파생상품: '공격투자형',
};

const COUNSEL_PERIOD_NOTE = {
  '1년 이하': '단기 목표라 변동성이 큰 상품은 주의가 필요해요.',
  '1~3년': '중기 목표시니 변동성을 어느 정도 감내할 수 있는 선에서 안내드려요.',
  '3년 이상': '장기 목표라 단기 변동성보다는 성향에 맞는 상품 위주로 안내드려요.',
};

/* 상담 되묻기 1단계 - 목적 선택지 (value는 이후 분기 흐름 판별용) */
const COUNSEL_GOALS = [
  { label: '목돈 모으기', value: 'savings' },
  { label: '내 집 마련(청약)', value: 'housing' },
  { label: '투자 수익', value: 'investment' },
  { label: '생활자금 관리', value: 'spending' },
];

/* 자유입력에 목적이 이미 드러나 있으면(예: "투자해보고싶어") 목적을 다시 묻지 않고
   바로 해당 목적의 되묻기로 들어간다. 애매하면(매칭 없음) 그대로 목적부터 물어본다. */
const COUNSEL_GOAL_KEYWORDS = [
  { value: 'investment', keywords: ['투자'] },
  { value: 'housing', keywords: ['청약', '내 집', '집 마련', '전세', '매매'] },
  { value: 'spending', keywords: ['생활비', '소비', '용돈'] },
];

const detectCounselGoal = (text) => {
  const found = COUNSEL_GOAL_KEYWORDS.find((g) => g.keywords.some((k) => text.includes(k)));
  return found ? COUNSEL_GOALS.find((g) => g.value === found.value) : null;
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
const todaySessionId = ref(null); // "이전 기록"에서 지난 세션을 보다가 다시 오늘 세션으로 돌아오기 위한 기준값
const messages = ref([]);
const input = ref('');
const typing = ref(false);
const panel = ref(null); // 'actions' (종료하기 버튼 노출)
const inputRef = ref(null);

// 상담 되묻기 중 숫자 등 자유입력 답변을 기다리는 상태 - 있으면 submitInput이 백엔드 대신 이 핸들러로 보낸다
const counselInputHandler = ref(null);

// 만족도 설문 모달 상태
const feedbackModalOpen = ref(false);
const selectedFeedback = ref(null); // 'like' | 'neutral' | 'dislike'
const feedbackReason = ref('');
const feedbackSubmitting = ref(false);

// 다른 화면(공통 헤더) -> 챗봇은 헤더가 달라져서, 최소 이 시간만큼은 이동 랜딩 화면을 보여준다
const MIN_LANDING_MS = 500;
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// 봇 응답 전 "타이핑 중" 딜레이 - 되묻기 흐름 곳곳에서 반복 사용
const TYPING_DELAY_MS = 700;

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
  panel.value = 'actions'; // 봇 답변이 나오면 항상 "종료하기"를 보여준다 (개별 함수마다 챙기지 않아도 되게)
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

// "이전 기록"에서 지난 세션을 보고 있는 도중이면, 오늘 세션으로 먼저 돌아온 뒤 새 메시지를 보낸다.
// (안 그러면 sessionId가 계속 예전 세션을 가리켜서, 이후 대화가 오늘 기록이 아니라 그 지난 세션에 쌓여버림)
const ensureTodaySession = async () => {
  if (todaySessionId.value && sessionId.value !== todaySessionId.value) {
    await resumeSession(todaySessionId.value);
  }
};

const backToGuide = async () => {
  counselInputHandler.value = null;
  await ensureTodaySession();
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
      text: '어떤 카테고리가 궁금하신가요?',
      menu: [
        ...Object.entries(LIVE_CATEGORY_LABELS).map(([category, label]) => ({
          label,
          onClick: () => {
            pushUser(label);
            showProductCategoryList(category, label, { includeListings: category !== 'subscription' });
          },
        })),
        FIRST_MENU_ITEM,
      ],
    });
  }, TYPING_DELAY_MS);
};

/* 실시간 은행 상품(FSS 예적금/청약홈/펀드) - 카테고리별로 목록을 받아와서 최대 8개까지 보여준다.
   장병내일준비적금 등 3개는 API로 못 받아오는 KB 군장병 전용 상품이라 텍스트로 직접 정리해둔 것뿐이고,
   실제로는 해당 카테고리(적금/청약)의 "상품 중 하나"라 API 상품들과 같은 목록에 같이 보여준다. */
const LIVE_CATEGORY_LABELS = { savings: '적금', deposit: '예금', subscription: '청약', investment: '투자' };
const FIXED_PRODUCTS_BY_CATEGORY = {
  savings: ['장병내일준비적금', '청년미래적금'],
  subscription: ['청년주택드림청약통장'],
  deposit: [],
  investment: [],
};
const LIVE_ITEM_LABEL = {
  savings: (p) => `${p.finPrdtNm} (${p.korCoNm} · 최고 ${p.maxRate}%)`,
  deposit: (p) => `${p.finPrdtNm} (${p.korCoNm} · 최고 ${p.maxRate}%)`,
  subscription: (p) => `${p.houseNm} (청약 ${p.rceptBgnde || '-'}~${p.rceptEndde || '-'})`,
  investment: (p) => p.fndNm,
};
const LIVE_ITEM_NAME = {
  savings: (p) => p.finPrdtNm,
  deposit: (p) => p.finPrdtNm,
  subscription: (p) => p.houseNm,
  investment: (p) => p.fndNm,
};

// includeListings: false면 실시간 청약홈 "매물" 목록은 빼고 고정 상품만 보여준다.
// 청약은 "내 집 마련(청약)" 상담(askGoal → housing)에서만 매물을 같이 보여주고,
// 카테고리 목록(적금/예금/청약/투자) 탐색에서는 매물이 상품처럼 섞여 나오면 안 되니 뺀다.
const showProductCategoryList = async (category, categoryLabel, { includeListings = true } = {}) => {
  panel.value = null;
  typing.value = true;
  try {
    const shouldFetchLive = includeListings || category !== 'subscription';
    const { data: liveProducts } = shouldFetchLive
      ? await chatApi.listProducts(category)
      : { data: [] };
    typing.value = false;
    const fixedNames = FIXED_PRODUCTS_BY_CATEGORY[category] || [];
    if (!fixedNames.length && !liveProducts.length) {
      pushBot({ text: `지금은 표시할 수 있는 ${categoryLabel} 상품이 없습니다.`, menu: [FIRST_MENU_ITEM] });
      return;
    }
    const top = liveProducts.slice(0, 8);
    pushBot({
      text: `${categoryLabel} 상품이에요. 궁금한 상품을 골라주세요.`,
      menu: [
        ...fixedNames.map((name) => ({ label: name, onClick: () => openDoc(name) })),
        ...top.map((p) => ({
          label: LIVE_ITEM_LABEL[category](p),
          onClick: () => showLiveProductDetail(LIVE_ITEM_NAME[category](p), category),
        })),
        FIRST_MENU_ITEM,
      ],
    });
  } catch {
    typing.value = false;
    pushError();
  }
};

const LIVE_DETAIL_TEXT = {
  savings: (p) =>
    `${p.korCoNm}에서 제공하는 상품입니다.\n가입 방법: ${p.joinWay}\n가입 대상: ${p.joinMember}\n우대조건: ${p.spclCnd}${p.etcNote ? `\n기타: ${p.etcNote}` : ''}`,
  deposit: (p) =>
    `${p.korCoNm}에서 제공하는 상품입니다.\n가입 방법: ${p.joinWay}\n가입 대상: ${p.joinMember}\n우대조건: ${p.spclCnd}${p.etcNote ? `\n기타: ${p.etcNote}` : ''}`,
  subscription: (p) =>
    `주소: ${p.hssplyAdres || '정보 없음'}\n청약 접수: ${p.rceptBgnde || '-'}~${p.rceptEndde || '-'}\n입주 예정: ${p.mvnPrearngeYm || '미정'}`,
  investment: (p) => `분류: ${p.ctg || '정보 없음'}\n설정일: ${p.setpDt || '정보 없음'}\n유형: ${p.fndTp || '정보 없음'}`,
};

const showLiveProductDetail = async (name, category) => {
  pushUser(name);
  panel.value = null;
  typing.value = true;
  try {
    const { data: p } = await chatApi.getProduct(name, category);
    typing.value = false;
    pushBot({
      title: name,
      text: LIVE_DETAIL_TEXT[category](p),
      source: p.source,
      menu: [FIRST_MENU_ITEM],
    });
  } catch {
    typing.value = false;
    pushError();
  }
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
  }, TYPING_DELAY_MS);
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

/* 자주 묻는 질문 - "적금·청약 상품이 궁금해요"(상품 하나 깊게 탐색)와 겹치지 않게,
   여러 상품 중 뭘 고를지 비교·선택을 도와주는 질문으로 구성. 실제 백엔드(RAG)로 물어봐서
   여러 상품 문서를 종합한 답변을 받는다 */
const FAQ_QUESTIONS = [
  '적금이랑 예금 중에 뭐가 더 좋아요?',
  '장병내일준비적금이랑 청년미래적금 차이가 뭐예요?',
  '청약통장은 꼭 만들어야 해요?',
  '목돈 모으기엔 적금이 나아요, 청약이 나아요?',
];

const openFaqCategories = () => {
  pushUser('자주 묻는 질문');
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    pushBot({
      text: '어떤 게 궁금하신가요?',
      menu: [
        ...FAQ_QUESTIONS.map((q) => ({ label: q, onClick: () => askBackend(q, { forceInfo: true }) })),
        FIRST_MENU_ITEM,
      ],
    });
  }, TYPING_DELAY_MS);
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

/* 목돈 상담 - 되묻기형(목적 -> 목적별 분기) */
const openCounsel = () => {
  counselInputHandler.value = null;
  pushUser('목돈 어떻게 쓸지 상담받기');
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    startCounsel();
  }, TYPING_DELAY_MS);
};

const startCounsel = () => {
  pushBot({
    title: '자금 상담',
    text: '몇 가지만 여쭤볼게요.\n어떤 목적으로 목돈을 활용하고 싶으세요?',
    menu: COUNSEL_GOALS.map((g) => ({ label: g.label, onClick: () => askGoal(g) })),
  });
};

const askGoal = (goal, { announce = true } = {}) => {
  if (announce) pushUser(goal.label);
  panel.value = null;

  // 내 집 마련(청약): 되묻기 없이 바로 실시간 청약 상품 + 자취 준비 페이지 안내
  if (goal.value === 'housing') {
    typing.value = true;
    setTimeout(async () => {
      typing.value = false;
      await showProductCategoryList('subscription', '청약');
      const rentLink = PAGE_LINKS.find((p) => p.to.name === 'RentGoalCreate');
      pushBot({
        text: `저희 서비스에 ${rentLink.description}이 있는데, 확인해 보시겠습니까?`,
        // 이 안내는 자취 준비 페이지로 보내는 게 목적이라 "처음으로"는 넣지 않는다
        menu: [{ label: rentLink.label, onClick: () => goTo(rentLink.to) }],
      });
    }, TYPING_DELAY_MS);
    return;
  }

  // 생활자금 관리: 되묻기 없이 바로 시뮬레이터/후회소비 페이지 안내
  if (goal.value === 'spending') {
    typing.value = true;
    setTimeout(() => {
      typing.value = false;
      pushBot({
        text: '생활자금 관리는 자금 시뮬레이션이나 후회소비 회고 기능에서 도와드릴 수 있어요.',
        menu: [
          { label: '자금 시뮬레이션', onClick: () => goTo({ name: 'Simulator' }) },
          { label: '후회소비 회고', onClick: () => goTo({ name: 'RegretReview' }) },
          FIRST_MENU_ITEM,
        ],
      });
      panel.value = 'actions';
    }, TYPING_DELAY_MS);
    return;
  }

  // 투자 수익: 목표기간 -> 투자성향으로 이어감
  if (goal.value === 'investment') {
    typing.value = true;
    setTimeout(() => {
      typing.value = false;
      askPeriod();
    }, TYPING_DELAY_MS);
    return;
  }

  // 목돈 모으기: 상품을 바로 추천하지 않고, 방식(적금/투자)부터 되물어 실제 상담으로 이어간다
  askSavingsMethod();
};

/* 목돈 모으기 - "어떤 방식으로" 되묻기. 적금은 실제 계산(월납입액/기간 직접입력 -> 계산기 API),
   예금은 실시간 예금 상품 목록, 투자는 기존 투자 수익 흐름(기간->성향->실시간 펀드) 재사용,
   목표부터 정하기는 상품이 아니라 기존 목표 로드맵 페이지(여행/자취/진로/자차)로 안내한다 */
const askSavingsMethod = () => {
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    pushBot({
      text: '어떤 방식으로 모으고 싶으세요?',
      menu: [
        { label: '적금', onClick: () => chooseSavingsMethod('적금') },
        { label: '예금', onClick: () => chooseSavingsMethod('예금') },
        { label: '투자', onClick: () => chooseSavingsMethod('투자') },
        { label: '목표부터 정하기', onClick: () => chooseSavingsMethod('목표부터 정하기') },
      ],
    });
  }, TYPING_DELAY_MS);
};

const chooseSavingsMethod = (method) => {
  pushUser(method);
  panel.value = null;
  typing.value = true;

  if (method === '투자') {
    setTimeout(() => {
      typing.value = false;
      askPeriod();
    }, TYPING_DELAY_MS);
    return;
  }

  if (method === '예금') {
    setTimeout(async () => {
      typing.value = false;
      await showProductCategoryList('deposit', '예금');
      panel.value = 'actions';
    }, TYPING_DELAY_MS);
    return;
  }

  if (method === '목표부터 정하기') {
    setTimeout(() => {
      typing.value = false;
      askGoalTarget();
    }, TYPING_DELAY_MS);
    return;
  }

  // 적금
  setTimeout(() => {
    typing.value = false;
    pushBot({ text: '한 달에 얼마씩 저축하실 수 있으세요? 숫자로 입력해주세요. (예: 30만원, 300000)' });
    counselInputHandler.value = handleMonthlyAmountInput;
  }, TYPING_DELAY_MS);
};

const askGoalTarget = () => {
  pushBot({
    text: '어떤 목표를 준비 중이세요?',
    menu: [...EXTERNAL_NAV.map((n) => ({ label: n.label, onClick: () => confirmGoalTarget(n) })), FIRST_MENU_ITEM],
  });
};

const confirmGoalTarget = (navItem) => {
  pushUser(navItem.label);
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    pushBot({
      text: `저희 서비스에 ${navItem.description}이 있는데, 확인해 보시겠습니까?`,
      menu: [{ label: navItem.label, onClick: () => goTo(navItem.to) }, FIRST_MENU_ITEM],
    });
    panel.value = 'actions';
  }, TYPING_DELAY_MS);
};

// "30만원", "300000", "300,000원" 형태를 원 단위 숫자로 변환. 못 알아들으면 null.
const parseKoreanAmount = (text) => {
  const cleaned = text.replace(/[,원\s]/g, '');
  const manMatch = cleaned.match(/^(\d+(?:\.\d+)?)만$/);
  if (manMatch) return Math.round(parseFloat(manMatch[1]) * 10000);
  if (/^\d+$/.test(cleaned)) return parseInt(cleaned, 10);
  return null;
};

const handleMonthlyAmountInput = (text) => {
  pushUser(text);
  const amount = parseKoreanAmount(text);
  if (!amount || amount <= 0) {
    pushBot({ text: '금액을 다시 확인해주세요. 숫자로 입력해주세요. (예: 30만원, 300000)' });
    counselInputHandler.value = handleMonthlyAmountInput;
    return;
  }
  pushBot({ text: '몇 개월 동안 모으실 계획이세요? 숫자로 입력해주세요. (예: 24)' });
  counselInputHandler.value = (t) => handleSaveMonthsInput(t, amount);
};

const handleSaveMonthsInput = async (text, monthlyAmount) => {
  pushUser(text);
  const months = parseInt(text.replace(/[^0-9]/g, ''), 10);
  if (!months || months <= 0) {
    pushBot({ text: '기간을 다시 확인해주세요. 숫자로 입력해주세요. (예: 24)' });
    counselInputHandler.value = (t) => handleSaveMonthsInput(t, monthlyAmount);
    return;
  }
  panel.value = null;
  typing.value = true;
  try {
    const result = await simulatorApi.calculateConstant({ monthlySave: monthlyAmount, saveMonths: months });
    typing.value = false;
    const won = (n) => `${Number(n).toLocaleString('ko-KR')}원`;
    pushBot({
      title: '적금 상담 결과',
      text:
        `월 ${won(monthlyAmount)}씩 ${months}개월 납입하면\n` +
        `원금 ${won(result.totalPrincipal)} + 이자 ${won(result.totalInterest)} + 정부기여금 ${won(result.totalMatchingFund)}\n` +
        `= 총 ${won(result.totalReceiptAmount)}을 받으실 수 있습니다.`,
      menu: [{ label: '장병내일준비적금 자세히 보기', onClick: () => openDoc('장병내일준비적금') }, FIRST_MENU_ITEM],
    });
    panel.value = 'actions';
  } catch {
    typing.value = false;
    pushError();
  }
};

const askPeriod = () => {
  pushBot({
    text: '목표 기간이 어떻게 되세요?',
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
  }, TYPING_DELAY_MS);
};

const finishCounsel = async (period, type) => {
  pushUser(type);
  panel.value = null;
  typing.value = true;
  try {
    const { data: funds } = await chatApi.listProducts('investment');
    typing.value = false;
    const matched = funds.filter((f) => FUND_RISK_TYPE[f.fndTp] === type).slice(0, 5);
    if (!matched.length) {
      pushBot({
        title: `${period} · ${type} 추천`,
        text: '지금은 조건에 맞는 펀드 상품이 없습니다.',
        menu: [FIRST_MENU_ITEM],
      });
    } else {
      pushBot({
        title: `${period} · ${type} 추천`,
        text: `${COUNSEL_PERIOD_NOTE[period]}\n${type}에 맞는 펀드를 모아봤어요.`,
        menu: [
          ...matched.map((f) => ({
            label: f.fndNm,
            onClick: () => showLiveProductDetail(f.fndNm, 'investment'),
          })),
          FIRST_MENU_ITEM,
        ],
      });
    }
    panel.value = 'actions';
  } catch {
    typing.value = false;
    pushError();
  }
};

/* 자유 입력 텍스트를 실제 백엔드(RAG/Gemini)로 보내고 답변을 받는다 */
const askBackend = async (text, { title, extraMenu = [], forceInfo = false } = {}) => {
  counselInputHandler.value = null;
  await ensureTodaySession();
  pushUser(text);
  input.value = '';
  panel.value = null;
  typing.value = true;
  try {
    const { data: botMsg } = await chatApi.sendMessage(sessionId.value, text, forceInfo);
    typing.value = false;

    // 백엔드가 자유입력을 상담(counsel)으로 분류하면, 일반 RAG 답변 대신
    // 되묻기 플로우로 분기한다 (WBS-6) - 가이드 화면의 "목돈 상담받기" 버튼과 동일한 흐름 재사용.
    // 텍스트에 목적이 이미 드러나 있으면(예: "투자해보고싶어") 목적 질문은 건너뛴다.
    if (botMsg.intent === 'counsel') {
      pushBot({ text: botMsg.content });
      const matchedGoal = detectCounselGoal(text);
      if (matchedGoal) {
        askGoal(matchedGoal, { announce: false });
      } else {
        startCounsel();
      }
      return;
    }

    const menu = [...extraMenu, FIRST_MENU_ITEM];
    const pageLink = PAGE_LINKS.find((p) => p.keywords.some((k) => text.includes(k)));
    let answerText = botMsg.content;
    if (pageLink) {
      menu.unshift({ label: pageLink.label, onClick: () => goTo(pageLink.to) });
      // 버튼만 툭 주지 않고, 어떤 기능인지 먼저 설명하고 이동을 제안한다
      answerText += `\n\n저희 서비스에 ${pageLink.description}이 있는데, 확인해 보시겠습니까?`;
    }

    // 답변에서 특정 상품이 언급됐으면 "더 자세한 내용 확인해보기" 버튼을 붙인다.
    // 고정된 FAQ를 다시 보여주는 게 아니라, 실제로 백엔드에 새 질문을 보내서
    // (멀티턴 문맥 덕분에) 지금까지 대화 주제에 맞는 답변을 받아오게 한다.
    // - 이미 그 상품의 되묻기 메뉴(extraMenu)가 붙어있으면(=이미 상품 Q&A 흐름 안) 중복이라 스킵
    // - pageLink가 떴으면(=진짜 관련 있는 답을 못 찾아서 다른 기능으로 유도 중) 언급된 상품은
    //   그냥 스쳐간 참고용이라 더 파고들면 오히려 엉뚱한 대화로 새서 같이 스킵
    if (!extraMenu.length && !pageLink) {
      const relatedProduct = Object.keys(PRODUCT_QUESTIONS).find(
        (name) => botMsg.content.includes(name) || (botMsg.sourceDetail || '').includes(name),
      );
      if (relatedProduct) {
        menu.unshift({
          label: '더 자세한 내용 확인해보기',
          onClick: () => askBackend('더 자세한 내용을 확인하고 싶어요'),
        });
      }
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
  // 이전 질문 답변을 기다리는 중이면 새 질문을 못 보내게 막는다 - 안 막으면 답변 순서가
  // 실제 도착 순서대로 뒤섞여 보이는 문제가 생긴다(질문1→질문2→답변1→답변2처럼).
  if (typing.value) return;

  const trimmed = input.value.trim();
  if (!trimmed) return;
  input.value = '';

  // 상담 되묻기 중 숫자 등 자유입력 답변을 기다리고 있으면, 백엔드로 보내지 않고 그 핸들러가 받는다
  if (counselInputHandler.value) {
    const handler = counselInputHandler.value;
    counselInputHandler.value = null;
    handler(trimmed);
    return;
  }

  // 그 외엔 백엔드의 classify_intent("counsel") 분류 결과로 상담형 되묻기 진입 여부를 판단한다
  // (askBackend 내부에서 botMsg.intent === 'counsel'이면 되묻기 플로우로 분기)
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

// 히스토리를 다시 불러왔을 때도 그 시점에 있던 버튼(더 자세히/추천 이동 등)을 최대한 그대로 복원한다.
// 실제로 눌렀던 버튼 자체가 저장되는 게 아니라서, 그 답변 직전 사용자 메시지를 보고 같은 로직으로 재계산한다.
const findActiveProductBefore = (history, index) => {
  for (let i = index - 1; i >= 0; i -= 1) {
    const msg = history[i];
    if (msg.role === 'user' && PRODUCT_QUESTIONS[msg.content]) {
      return msg.content;
    }
  }
  return null;
};

const deriveHistoryMenu = (history, index) => {
  const m = history[index];
  const prev = index > 0 ? history[index - 1] : null;
  const precedingUserText = prev && prev.role === 'user' ? prev.content : null;

  if (precedingUserText) {
    if (PRODUCT_QUESTIONS[precedingUserText]) {
      // 직전에 상품명 자체를 물어본 경우 -> 그 상품의 질문 목록을 보여준다
      const name = precedingUserText;
      return { menu: PRODUCT_QUESTIONS[name].map((q) => ({ label: q, onClick: () => askProductQuestion(name, q) })) };
    }
    const activeProduct = findActiveProductBefore(history, index);
    if (activeProduct && PRODUCT_QUESTIONS[activeProduct].includes(precedingUserText)) {
      // 그 상품에 대한 후속 질문 중 하나였던 경우 -> 방금 물어본 것만 빼고 다시 보여준다
      const remaining = PRODUCT_QUESTIONS[activeProduct].filter((q) => q !== precedingUserText);
      return { menu: remaining.map((q) => ({ label: q, onClick: () => askProductQuestion(activeProduct, q) })) };
    }
    const pageLink = PAGE_LINKS.find((p) => p.keywords.some((k) => precedingUserText.includes(k)));
    if (pageLink) {
      return {
        menu: [{ label: pageLink.label, onClick: () => goTo(pageLink.to) }],
        extraText: `\n\n저희 서비스에 ${pageLink.description}이 있는데, 확인해 보시겠습니까?`,
      };
    }
  }

  const relatedProduct = Object.keys(PRODUCT_QUESTIONS).find(
    (name) => m.content.includes(name) || (m.sourceDetail || '').includes(name),
  );
  if (relatedProduct) {
    return {
      menu: [{ label: '더 자세한 내용 확인해보기', onClick: () => askBackend('더 자세한 내용을 확인하고 싶어요') }],
    };
  }
  return { menu: [] };
};

const toBubble = (m, history, index) => {
  if (m.role === 'user') {
    return { id: `hist-${m.messageId}`, role: 'user', text: m.content, time: formatBubbleTime(m.createdDate) };
  }
  const { menu, extraText } = deriveHistoryMenu(history, index);
  return {
    id: `hist-${m.messageId}`,
    role: 'bot',
    time: formatBubbleTime(m.createdDate),
    text: extraText ? `${m.content}${extraText}` : m.content,
    source: m.source,
    sourceDetail: m.sourceDetail,
    isAiGenerated: m.isAiGenerated,
    menu: [...menu, FIRST_MENU_ITEM],
  };
};

const openHistory = async () => {
  try {
    const { data: sessions } = await chatApi.listSessions();
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
      ...history.map((m, i) => toBubble(m, history, i)),
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
    const { data: session } = await chatApi.createSession();
    sessionId.value = session.sessionId;
    todaySessionId.value = session.sessionId;

    if (session.isNew) {
      messages.value = buildGreetAndGuide();
    } else {
      const { data: history } = await chatApi.getHistory(sessionId.value);
      if (history.length) {
        // 가이드 카드는 대화가 이어져도 계속 보여야 하는 진입점이라, 히스토리 앞에 항상 붙인다
        messages.value = [
          { id: 'guide', role: 'bot', time: formatBubbleTime(), ...buildGuideMessage() },
          ...history.map((m, i) => toBubble(m, history, i)),
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
              <span class="bot-name">노이병장</span>
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
              <span class="bot-name">노이병장</span>
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
            <span class="bot-name">노이병장</span>
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
          :placeholder="counselInputHandler ? '숫자로 입력해주세요' : '궁금한 점을 물어보세요'"
          class="composer-input"
          @keydown.enter.prevent="submitInput"
        />
        <button
          type="button"
          class="composer-send"
          :disabled="!input.trim() || typing"
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
