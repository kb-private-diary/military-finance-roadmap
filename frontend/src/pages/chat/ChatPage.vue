<script setup>
// SCR-CHAT-01 · 챗봇  (담당: 에스더)
// FastAPI 챗봇 대화창 (JWT 공유) — 프로토타입(MilitaryChatbot.jsx) 기반, 실제 백엔드(chatApi) 연동
import { computed, nextTick, onActivated, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import chatApi from '@/api/chatApi';
import { useAuthStore } from '@/stores/auth';
import { formatDate } from '@/util/format';
import mascotImg from '@/assets/chat-mascot.png';
import moodLikeImg from '@/assets/chat-mood-like.png';
import moodNeutralImg from '@/assets/chat-mood-neutral.png';
import moodDislikeImg from '@/assets/chat-mood-dislike.png';
// 자유입력 목적/카테고리 매칭 로직은 겹치는 키워드 회귀가 잦아서 별도 모듈로 빼고
// 자동 테스트(counselRouting.test.js)로 고정해뒀다(2026-08-12) - 자세한 이유는 그 파일 주석 참고.
import { COUNSEL_GOALS, detectCounselGoal, detectDirectListCategory } from './counselRouting';

const router = useRouter();
const auth = useAuthStore();

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

// confirmGoalTarget이 실제 이동 확인 버튼에 쓰는 문구 - "여행 계획 세우기"처럼 그 화면을
// 고르는 라벨과, "여행 페이지로 이동"처럼 실제 이동을 확정하는 문구를 구분해서 쓴다(2026-08-06 피드백).
// 라우트명 기준이라 EXTERNAL_NAV/ALL_FEATURES 어느 쪽에서 와도 재사용된다.
const GUIDE_NAV_LABELS = {
  TravelGoalCreate: '여행 페이지로 이동',
  RentGoalCreate: '자취 페이지로 이동',
  JobGoalCreate: '진로 페이지로 이동',
  CarGoalCreate: '자차 페이지로 이동',
  Simulator: '군적금 시뮬레이션 페이지로 이동',
  Dashboard: '대시보드 페이지로 이동',
  RegretDashboard: '후회소비 회고 페이지로 이동',
  // RegretDashboard와 이름이 비슷해서 헷갈리기 쉬운데, "후회소비 회고" 관련 화면이 라우트가
  // 두 개(RegretReview/RegretDashboard)라 여기 없으면 label 그대로 노출돼서 빠져있던 걸 채움(2026-08-06)
  RegretReview: '후회소비 확인하러 이동',
  Social: '저축 비교 페이지로 이동',
};

/* 하단 태그줄 - "적금률 비교"는 단순 이동이 아니라 상담형 로직(WBS-6)으로 봐야 해서 아직 버튼에서 뺌.
   "저축 비교하기"(라우트명은 그대로 Social, 태석님 화면이 "소셜"에서 이름만 바뀜, 2026-08-06).
   "자주 묻는 질문"은 선정 기준이 애매하고 아직 데이터도 안 쌓여서 아예 뺐다(2026-08-06 피드백).
   나머지 3개도 바로 이동하지 않고 confirmGoalTarget으로 설명 먼저 보여준다(2026-08-06 피드백) */
const EXTERNAL_TAGS = [
  { label: '군적금 활용하기', onClick: () => showAllFeatures() },
  {
    label: '후회소비 회고',
    description: '지난 소비 내역을 되돌아보며 후회되는 지출을 기록하고 다음 소비 습관을 점검할 수 있는 회고 기능',
    to: { name: 'RegretReview' },
  },
  {
    label: '자금 시뮬레이션',
    description: '월 납입액과 기간을 넣어 만기 수령액(이자·정부기여금 포함)을 미리 계산해볼 수 있는 시뮬레이터 기능',
    to: { name: 'Simulator' },
  },
  {
    label: '저축 비교하기',
    description: '전우들과 저축·랭킹을 비교해볼 수 있는 기능',
    to: { name: 'Social' },
  },
];

/* "군적금 활용하기" 클릭 시 보여줄 전체 기능 목록 (라우트정의서 기준, 2026-07-30).
   각 항목에 description을 붙여서, 뭔지도 모르고 누르게 되지 않도록 confirmGoalTarget으로
   "이런 기능이 있는데 확인해 보시겠습니까?" 설명을 먼저 보여주고 이동한다(2026-08-06 피드백). */
const ALL_FEATURES = [
  { label: '여행 계획 세우기', description: EXTERNAL_NAV[0].description, to: { name: 'TravelGoalCreate' } },
  { label: '자취 준비하기', description: EXTERNAL_NAV[1].description, to: { name: 'RentGoalCreate' } },
  { label: '진로 준비하기', description: EXTERNAL_NAV[2].description, to: { name: 'JobGoalCreate' } },
  { label: '자차 준비하기', description: EXTERNAL_NAV[3].description, to: { name: 'CarGoalCreate' } },
  {
    label: '군적금 시뮬레이션',
    description: '월 납입액과 기간을 넣어 만기 수령액(이자·정부기여금 포함)을 미리 계산해볼 수 있는 시뮬레이터 기능',
    to: { name: 'Simulator' },
  },
  {
    label: '대시보드 (D-Day·휴가 관리)',
    description: '전역일까지 D-Day와 휴가 일정을 한눈에 관리할 수 있는 대시보드 기능',
    to: { name: 'Dashboard' },
  },
  {
    label: '후회소비 회고',
    description: '지난 소비 내역을 되돌아보며 후회되는 지출을 기록하고 다음 소비 습관을 점검할 수 있는 회고 기능',
    to: { name: 'RegretDashboard' },
  },
  {
    label: '저축 비교하기',
    description: '전우들과 저축·랭킹을 비교해볼 수 있는 기능',
    to: { name: 'Social' },
  },
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
    // 여행 관련 자유 질문("여행 계획은?" 등)이 지금까지 무관련 질문으로 걸러지고 있었음(2026-08-06 발견) -
    // 자취/자차처럼 여행 준비 페이지로 안내되게 추가
    keywords: ['여행', '휴가', '여행지'],
    label: '여행 계획 페이지로 이동',
    description: EXTERNAL_NAV[0].description,
    to: { name: 'TravelGoalCreate' },
  },
  {
    // "집" 하나만으로도 넓게 잡음 - "집 사려고" 같은 자연스러운 표현이 안 걸려서 엉뚱하게
    // 상담 되묻기로 새던 문제 발견(2026-08-06 피드백)
    keywords: ['전세', '자취', '신혼', '매매', '집 마련', '집', '주택'],
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
  {
    // 자격증·취업 관련 자유 질문이 진로 준비 페이지로 안 이어지고 있었음(2026-08-06 피드백)
    keywords: ['자격증', '취업', '진로', '이직', '전역 후'],
    label: '진로 준비 페이지로 이동',
    description: EXTERNAL_NAV[2].description,
    to: { name: 'JobGoalCreate' },
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
  'KB손해보험 자동차보험(개인)': [
    '어떤 보장을 받을 수 있어요?',
    '보험료를 어떻게 줄일 수 있어요?',
    '가입 전에 뭘 확인해야 해요?',
    '청약철회는 어떻게 해요?',
  ],
  'KB손해보험 이륜차보험(개인)': [
    '의무보험은 꼭 가입해야 하나요?',
    '용도는 어떻게 골라야 해요?',
    '어떤 보장을 받을 수 있어요?',
    '가입 전에 뭘 확인해야 해요?',
  ],
  'KB손해보험 운전자보험(3년 이상)': [
    '3대 운전자 비용이 뭐예요?',
    '보험료는 얼마 정도예요?',
    '가입 조건이 어떻게 돼요?',
    '중도해지하면 손해봐요?',
  ],
  'KB손해보험 오토바이 운전자보험': [
    '형사합의금은 얼마까지 보장돼요?',
    '변호사선임비는 얼마나 보장돼요?',
    'KB자동차보험 가입하면 할인되나요?',
  ],
  'KB손해보험 운전자보험(1~3년)': [
    '운전자플랜이랑 자전거플랜 차이가 뭐예요?',
    '왜 짧게(1~3년) 가입하는 게 유리해요?',
    '보장 내용이 어떻게 돼요?',
  ],
  'KB손해보험 하루운전자보험(1~7일)': [
    '며칠까지 가입할 수 있어요?',
    '어떤 상황에 필요해요?',
    '3대 운전자 비용이 뭐예요?',
  ],
  'KB손해보험 실손의료비보장보험(본인)': [
    '비급여 의료비도 보장돼요?',
    '몇 살까지 가입할 수 있어요?',
    '보험료 할인은 어떻게 받아요?',
    '재가입은 어떻게 되는 거예요?',
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

/* 상담 만족도 3단계 - value는 백엔드 feedback 값(like/neutral/dislike)과 그대로 매칭 */
const MOOD_OPTIONS = [
  { value: 'dislike', label: '불만', img: moodDislikeImg },
  { value: 'neutral', label: '보통', img: moodNeutralImg },
  { value: 'like', label: '만족', img: moodLikeImg },
];

const loading = ref(true);
const loadError = ref('');
const sessionId = ref(null); // 새 메시지를 보낼 때 붙일 세션 - 유저당 하나만 계속 재사용한다(날짜로 안 끊음)
const messages = ref([]);
// 가로 스크롤 상품 카드(menuCarousel)의 현재 페이지(카드) 인덱스 - 메시지 id별로 따로 추적한다.
// 카드 폭을 CSS에서 130px(+gap 10px)로 고정해뒀는데(2.5장씩 걸치게, 2026-08-06 피드백),
// 컨테이너 전체 폭 기준으로 계산하면 카드가 여러 개 보일 때 인덱스가 틀어져서 이 고정값을 그대로 쓴다.
const CAROUSEL_CARD_STEP = 140; // 카드 130px + gap 10px
const carouselActive = ref({});
const onCarouselScroll = (msg, event) => {
  const el = event.target;
  const index = Math.round(el.scrollLeft / CAROUSEL_CARD_STEP);
  carouselActive.value[msg.id] = index;
};

// 점(dot) 클릭이나 화살표 버튼으로도 넘길 수 있게 - 스크롤 되는 걸 몰라서 못 넘기겠다는 피드백(2026-08-06)
const scrollCarouselToIndex = (msg, index) => {
  const el = document.getElementById(`carousel-${msg.id}`);
  if (!el) return;
  const clampedIndex = Math.max(0, Math.min(index, msg.menu.length - 1));
  el.scrollTo({ left: clampedIndex * CAROUSEL_CARD_STEP, behavior: 'smooth' });
  carouselActive.value[msg.id] = clampedIndex;
};
const scrollCarouselBy = (msg, direction) => {
  scrollCarouselToIndex(msg, (carouselActive.value[msg.id] || 0) + direction);
};
const input = ref('');
const typing = ref(false);
const panel = ref(null); // 'actions' (종료하기 버튼 노출)

// 실시간 상품 "목록"을 보여준 직후, 사용자가 그 목록에 대해 자유롭게 타이핑해서 물어보면
// (예: "왜 하나밖에 없어?") 백엔드는 지금 화면에 뭐가 떠 있는지 전혀 모른 채로 일반 RAG 검색을
// 타서 엉뚱한 정책 문서가 근거로 잡히는 문제가 있었다(2026-08-11 피드백) - 상품 상세의
// product_context와 같은 방식으로, 방금 보여준 목록 정보를 바로 다음 자유입력 한 번에만
// 근거로 실어 보낸다(1회성 - 쓰고 나면 비움).
const lastListContext = ref(null);

// 목돈 상담(목돈모으기→적금) 흐름에서 금액·기간을 이미 물어본 뒤엔, 장병내일준비적금 같은 고정
// 상품(RAG문서)을 나중에 다른 경로로(예: "다른 적금 상품도 보여줘") 골라도 그 값 그대로 계산 결과가
// 나와야 한다는 피드백(2026-08-11) - 예전엔 "사용자가 이 상품 쓴다고 한 적 없는데 무조건 계산기
// 돌리는 게 부적절하다"는 반대 피드백으로 없앴었는데, 이번엔 "상담 흐름 안에서 명시적으로 금액·
// 기간을 물어본 뒤"로 조건을 좁혀서 다시 넣는다 - 그 전까진 무조건 계산 안 하니 이전 피드백과도 안 어긋남.
const activeSavingsBudget = ref(null); // { monthlyAmount, months }
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

// 날짜 구분선 문구 - 대화가 여러 날에 걸쳐 하나로 이어지므로(날짜별로 세션을 안 끊음),
// 날짜가 바뀔 때마다 이 문구로 구분선을 끼워 넣는다. 인자를 안 주면 오늘 날짜.
const dateLabelFor = (value) => {
  const d = value ? new Date(value) : new Date();
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

// 정책용어사전은 특정 상품 출처가 아니라 일반 용어 정의라 "출처 · 정책용어사전"이 의미가 없어서 숨긴다.
// "무관련 질문 안내"도 실제 출처가 아니라 백엔드 내부에서 무관련 질문을 표시하려고 붙인 라벨이라
// 화면에 그대로 노출하면 사용자 입장에선 뜬금없어서 같이 숨긴다(2026-08-07 피드백).
// 최신 백엔드 로직은 이미 정책용어사전의 경우 source 자체를 안 보내지만, 그 전에 저장된 과거 대화엔
// 남아있을 수 있어서 화면에서도 한 번 더 걸러준다.
const visibleSource = (msg) => {
  const text = msg.sourceDetail || msg.source || '';
  return text.includes('정책용어사전') || text.includes('무관련 질문 안내') ? '' : text;
};

const genId = () => `${Date.now()}-${Math.random()}`;

// 버튼으로 진행하는 되묻기·상품 목록 등은 화면에서만 만들어지고 서버엔 아무것도 안 남아서,
// 대화 중에 딴 페이지로 갔다 오면(예: 자취 준비 페이지) 몰라도 새로고침·재로그인 후엔 통째로 사라졌었다.
// pushBot/pushUser를 거칠 때마다 실제 표시 문구를 그대로 가볍게 기록해둬서(AI 호출 없음) 이 문제를 막는다.
// 실패해도 지금 화면엔 영향 없게 조용히 무시(catch)한다 - 저장은 어디까지나 "복구용"이라 최선 노력이면 충분.
const logTurn = (role, content) => {
  if (!sessionId.value || !content) return;
  chatApi.logMessage(sessionId.value, role, content).catch(() => {});
};

// 메뉴 버튼(action이 붙은 것)은 사람이 읽는 글 뒤에 이 마커 + JSON을 몰래 붙여서 같이 저장해둔다.
// 실제 대화 내용(사용자 입력·Gemini 응답)에 이 조합이 그대로 등장할 일은 거의 없고,
// 화면엔 항상 마커 앞부분만 잘라서 보여준다.
const MENU_MARKER = ' #MENU# ';

// 오류 턴도 서버엔 role='bot'으로만 저장할 수 있어서(role은 user/bot만 허용됨), 새로고침 후
// 복원하면 실시간일 땐 빨간 오류 말풍선(role='error')으로 보이던 게 일반 답변처럼 검게 바뀌어
// AI가 실제로 생성한 답변인 것처럼 오해할 수 있었다(2026-08-12 발견). MENU_MARKER와 같은 방식으로
// 텍스트 뒤에 몰래 마커를 붙여뒀다가, 복원할 때 이 마커가 있으면 role='error'로 되살린다.
const ERROR_MARKER = ' #ERROR# ';

// 메뉴 항목뿐 아니라 어떤 스타일(menuInCard/menuCarousel/menuFit/menuAlternate)로 보여줄지도
// 같이 저장해둬야, 새로고침 후 복원된 메뉴도 실제 대화 때와 같은 모양(카드 안/가로 캐러셀 등)으로
// 보인다 - 전엔 메뉴 항목만 복원되고 스타일은 기본값으로 되돌아갔었음(2026-08-06 피드백).
const encodeMenuMarker = (msg) => {
  const menuItems = msg.menu || [];
  const actionable = menuItems.filter((opt) => opt.action);
  if (!actionable.length) return '';
  const style = {};
  if (msg.menuInCard) style.menuInCard = true;
  if (msg.menuCarousel) style.menuCarousel = true;
  if (msg.menuFit) style.menuFit = true;
  if (msg.menuAlternate) style.menuAlternate = true;
  if (msg.menuLight) style.menuLight = true;
  if (msg.menuGrid3) style.menuGrid3 = true;
  return (
    MENU_MARKER +
    JSON.stringify({
      style,
      items: actionable.map((opt) => ({ label: opt.label, action: opt.action, args: opt.args })),
    })
  );
};

// 마지막으로 화면에 표시된 날짜 구분선의 날짜(toDateString() 형식). 히스토리를 불러올 때나
// 새 세션을 시작할 때 맞춰두고, pushUser/pushBot이 호출될 때마다 오늘 날짜와 비교해서
// 날짜가 바뀌었으면 구분선을 새로 끼워 넣는다 - 안 그러면 어제 대화를 이어서 오늘 첫 메시지를
// 보내도 그 시점엔 오늘 날짜 구분선이 안 생기고(로드된 히스토리엔 아직 오늘 메시지가 없어서),
// 새로고침해서 히스토리를 다시 불러와야만 구분선이 뒤늦게 생기는 문제가 있었다(2026-08-13 발견).
const lastShownDay = ref(null);
const ensureDateDivider = () => {
  const today = new Date().toDateString();
  if (lastShownDay.value === today) return;
  messages.value.push({ id: `date-live-${genId()}`, role: 'date', label: dateLabelFor() });
  lastShownDay.value = today;
};

const pushBot = (msg) => {
  ensureDateDivider();
  messages.value.push({ id: genId(), role: 'bot', time: formatBubbleTime(), ...msg });
  panel.value = 'actions'; // 봇 답변이 나오면 항상 "종료하기"를 보여준다 (개별 함수마다 챙기지 않아도 되게)
  scrollToBottom();
  // 가이드 카드(sections)는 저장 안 함 - "처음으로"는 언제든 다시 누를 수 있는 고정 버튼이라,
  // 새로고침하면 그 시점 화면이 사라지는 정도는 다른 챗봇들도 흔히 받아들이는 수준이라 판단함
  // (버튼 클릭으로 복원 시도했다가 안정적으로 안 돼서 되돌림, 2026-08-06)
  if (msg.sections) return;
  // action이 없는 메뉴 항목만 글자 목록으로 같이 남겨서, 새로고침 후에도 "무슨 선택지가 있었는지"는
  // 최소한 텍스트로 보이게 한다(action이 있는 건 버튼 자체가 되살아나므로 중복으로 안 넣는다).
  const menuItems = msg.menu || [];
  const plainLabels = menuItems.filter((opt) => !opt.action).map((opt) => `· ${opt.label}`);
  const lines = [msg.title, msg.text, ...plainLabels].filter(Boolean);
  if (lines.length) logTurn('bot', lines.join('\n') + encodeMenuMarker(msg));
};
const pushUser = (text, { skipLog = false } = {}) => {
  ensureDateDivider();
  messages.value.push({ id: genId(), role: 'user', text, time: formatBubbleTime() });
  scrollToBottom();
  // askBackend로 가는 자유 질문은 그쪽(/messages)이 이미 서버에 저장하므로 여기서 또 남기면 중복된다.
  if (!skipLog) logTurn('user', text);
};
// customText를 넘기면(예: 백엔드가 준 400번대 검증 메시지) 그걸 그대로 보여주고,
// 안 넘기면 기존 범용 오류 문구를 보여준다(2026-08-11 피드백 - 500자 초과처럼 사용자가
// 바로 고칠 수 있는 입력 오류까지 "서버 상의 오류"로 뭉뚱그려 보여주던 문제).
const pushError = (customText) => {
  ensureDateDivider();
  const errorText = customText || '서버 상의 오류가 있습니다. 잠시 후에 다시 시도해 주세요.';
  messages.value.push({
    id: genId(),
    role: 'error',
    text: errorText,
  });
  // pushBot과 달리 오류 턴은 서버에 안 남고 panel도 안 켜져서, 오류 직후 새로고침/재진입하면
  // 마지막 메시지가 사용자 턴에서 뚝 끊긴 것처럼 보이고 처음으로/종료하기 버튼도 영영 안 뜬다
  // (실제 화면 버그 재현: '적금' 클릭 후 오류로 응답이 끊긴 세션을 새로고침하니 버튼이 안 나타남).
  // 오류도 하나의 "턴 종료"로 취급해 로그를 남기고 패널을 다시 켜준다(2026-08-11).
  panel.value = 'actions';
  logTurn('bot', errorText + ERROR_MARKER);
  scrollToBottom();
};

const backToGuide = async () => {
  counselInputHandler.value = null;
  activeSavingsBudget.value = null;
  pushBot(buildGuideMessage());
};

// 세션을 새로 여는 시점(오늘 첫 진입)에만 인사말+가이드를 함께 보여준다
const buildGreetAndGuide = () => [
  { id: 'date-today', role: 'date', label: dateLabelFor() },
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
        // 뭔지도 모르고 누르게 되지 않도록, 바로 이동하지 않고 confirmGoalTarget으로 무슨 기능인지
        // 먼저 설명한 뒤 확인받고 이동한다. 버튼도 글씨 크기에 맞게 줄임(compact).
        // 가이드 카드의 라벨은 원래 문구("여행 계획 세우기" 등) 그대로 두고, 확인 버튼 쪽이
        // "여행 페이지로 이동" 식으로 바뀐다 (2026-08-06 피드백, 처음 요청과 반대로 정정됨)
        compact: true,
        items: EXTERNAL_NAV.map((n) => ({
          label: n.label,
          onClick: () => confirmGoalTarget(n),
        })),
      },
      {
        heading: '무엇이든 물어보세요',
        subtitle: '궁금한 걸 편하게 골라보세요.',
        items: [
          { label: '목돈 어떻게 쓸지 상담받기', onClick: () => openCounsel() },
          { label: '적금·청약 상품이 궁금해요', onClick: () => showAllProducts() },
          { label: '정책 용어가 궁금해요', onClick: () => openGlossary() },
        ],
      },
    ],
    // 이미 가이드 화면이라 "처음으로"는 여기선 의미가 없어서 빼고, 답변 메뉴에만 붙인다.
    tags: EXTERNAL_TAGS.map((t) => ({ label: t.label, onClick: t.onClick ?? (() => confirmGoalTarget(t)) })),
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
      // 카테고리 자체는 상품이 아니라 되묻기(선택) 성격이라 다른 되묻기랑 통일 - 카드 안 흰 배경으로
      // (예전엔 번갈아 색을 넣었는데, 이제 되묻기=카드/상품=캐러셀 기준이 명확해져서 정리함, 2026-08-06 피드백)
      menuInCard: true,
      // 카테고리가 6개(적금/예금/청약/투자/보험/대출)라 3개씩 2줄로 고르게 배치한다 - flex-wrap만 쓰면
      // 5+1로 어중간하게 쪼개져 보였다(2026-08-07 피드백)
      menuGrid3: true,
      menu: [
        // 대출 3종(주택담보대출/전세자금대출/개인신용대출)은 사용자 입장에선 "대출"이라는 상품 카테고리
        // 하나일 뿐이라, 여기 순회에서 빼고 showLoanProducts로 묶어서 버튼 하나만 보여준다(2026-08-06 피드백)
        ...Object.entries(LIVE_CATEGORY_LABELS)
          .filter(([category]) => !LOAN_CATEGORIES.includes(category))
          .map(([category, label]) => ({
            label,
            onClick: () => selectProductCategory(category, label),
            action: 'selectProductCategory',
            args: [category, label],
          })),
        { label: '대출', onClick: () => showLoanProducts(), action: 'showLoanProducts', args: [] },
      ],
    });
  }, TYPING_DELAY_MS);
};

// showAllProducts 메뉴 클릭 핸들러를 이름 있는 함수로 빼둠 - 히스토리 복원 시 ACTIONS 레지스트리로 찾아서
// 같은 동작을 다시 실행할 수 있어야 하는데, 그러려면 인라인 화살표 함수가 아니라 참조 가능한 이름이 필요하다.
const selectProductCategory = (category, label) => {
  pushUser(label);
  showProductCategoryList(category, label, { includeListings: category !== 'subscription' });
};

// 대출 3종(주택담보대출/전세자금대출/개인신용대출)을 카테고리 선택 없이 바로 한 목록으로 합쳐서 보여준다.
// 처음엔 "대출 -> 어떤 대출이 궁금하신가요?(3종) -> 상품 목록"으로 한 단계 더 물어봤는데,
// "주택담보대출이 카테고리인 줄 몰랐다"는 피드백이 있어서 보험처럼 바로 상품을 보여주는 걸로 통일함(2026-08-06).
const showLoanProducts = async () => {
  pushUser('대출');
  panel.value = null;
  typing.value = true;
  try {
    const results = await Promise.all(
      LOAN_CATEGORIES.map((category) =>
        chatApi.listProducts(category).then(({ data }) => data.map((p) => ({ ...p, category }))),
      ),
    );
    typing.value = false;
    const top = results.flat().slice(0, 8);
    if (!top.length) {
      pushBot({ text: '지금은 표시할 수 있는 대출 상품이 없습니다.' });
      return;
    }
    pushBot({
      text: '대출 상품이에요. 궁금한 상품을 골라주세요.',
      // 대출 3종(주담대/전세자금/개인신용)을 한 목록으로 합쳐서 보여주다 보니 대표로 주담대 링크를 쓴다
      source: CATEGORY_LIST_SOURCE.mortgage.label,
      sourceUrl: CATEGORY_LIST_SOURCE.mortgage.url,
      menuCarousel: true,
      menu: top.map((p) => ({
        label: LIVE_ITEM_LABEL[p.category](p),
        onClick: () => showLiveProductDetail(LIVE_ITEM_NAME[p.category](p), p.category),
        action: 'showLiveProductDetail',
        args: [LIVE_ITEM_NAME[p.category](p), p.category],
      })),
    });
  } catch {
    typing.value = false;
    pushError();
  }
};

/* 실시간 은행 상품(FSS 예적금/청약홈/펀드) - 카테고리별로 목록을 받아와서 최대 8개까지 보여준다.
   장병내일준비적금 등 3개는 API로 못 받아오는 KB 군장병 전용 상품이라 텍스트로 직접 정리해둔 것뿐이고,
   실제로는 해당 카테고리(적금/청약)의 "상품 중 하나"라 API 상품들과 같은 목록에 같이 보여준다. */
// 대출 3종(주택담보대출/전세자금대출/개인신용대출)은 상품 성격이 아니라 API 카테고리 구분이라,
// "대출" 하나로 묶어서 보여주고(showLoanProducts) 카테고리 픽커 목록(LIVE_CATEGORY_LABELS)의
// Object.entries 순회에서는 제외한다 - 순회에 그대로 두면 "대출" 하나가 아니라 3개 버튼이 따로 나온다.
const LOAN_CATEGORIES = ['mortgage', 'jeonse', 'creditLoan'];
const LIVE_CATEGORY_LABELS = {
  savings: '적금',
  deposit: '예금',
  subscription: '청약',
  investment: '투자',
  mortgage: '주택담보대출',
  jeonse: '전세자금대출',
  creditLoan: '개인신용대출',
  insurance: '보험',
};
const FIXED_PRODUCTS_BY_CATEGORY = {
  savings: ['장병내일준비적금', '청년미래적금'],
  subscription: ['청년주택드림청약통장'],
  deposit: [],
  investment: [],
  mortgage: [],
  jeonse: [],
  creditLoan: [],
  // 보험은 KB손해보험다이렉트에 실시간 조회 API가 없어서(2026-08-06 확인), 7개 상품을 RAG 문서로
  // 직접 정리해두고 여기 고정 목록으로만 보여준다 - 다른 카테고리처럼 API 목록과 안 섞인다.
  insurance: [
    'KB손해보험 자동차보험(개인)',
    'KB손해보험 이륜차보험(개인)',
    'KB손해보험 운전자보험(3년 이상)',
    'KB손해보험 오토바이 운전자보험',
    'KB손해보험 운전자보험(1~3년)',
    'KB손해보험 하루운전자보험(1~7일)',
    'KB손해보험 실손의료비보장보험(본인)',
  ],
};
const LIVE_ITEM_LABEL = {
  savings: (p) => `${p.finPrdtNm} (${p.korCoNm} · 최고 ${p.maxRate}%)`,
  deposit: (p) => `${p.finPrdtNm} (${p.korCoNm} · 최고 ${p.maxRate}%)`,
  subscription: (p) => `${p.houseNm} (청약 ${p.rceptBgnde || '-'}~${p.rceptEndde || '-'})`,
  investment: (p) => p.fndNm,
  // 대출은 낮을수록 유리해서 적금/예금(최고금리)과 반대로 최저금리를 대표값으로 보여준다.
  mortgage: (p) => `${p.finPrdtNm} (${p.korCoNm} · 최저 ${p.rate}%)`,
  jeonse: (p) => `${p.finPrdtNm} (${p.korCoNm} · 최저 ${p.rate}%)`,
  creditLoan: (p) => `${p.finPrdtNm} (${p.korCoNm} · 최저 ${p.rate}%)`,
};
const LIVE_ITEM_NAME = {
  savings: (p) => p.finPrdtNm,
  deposit: (p) => p.finPrdtNm,
  subscription: (p) => p.houseNm,
  investment: (p) => p.fndNm,
  mortgage: (p) => p.finPrdtNm,
  jeonse: (p) => p.finPrdtNm,
  creditLoan: (p) => p.finPrdtNm,
};

// 목록 화면 자체(개별 항목을 누르기 전)에도 이 데이터가 어디서 왔는지 출처를 붙인다.
// 청약·예적금·대출은 실제로 열어서 확인된 링크가 있고, 펀드(투자)는 아직 확인된 링크가 없어 텍스트만 표기한다.
const CATEGORY_LIST_SOURCE = {
  savings: { label: '금융감독원 금융상품한눈에', url: 'https://finlife.fss.or.kr/finlife/svings/fdrmEnty/list.do?menuNo=700003' },
  deposit: { label: '금융감독원 금융상품한눈에', url: 'https://finlife.fss.or.kr/finlife/svings/fdrmDpst/list.do?menuNo=700002' },
  subscription: { label: '한국부동산원 청약홈', url: 'https://www.applyhome.co.kr' },
  investment: { label: '금융투자협회 펀드표준코드', url: null },
  mortgage: { label: '금융감독원 금융상품한눈에', url: 'https://finlife.fss.or.kr/finlife/ldng/houseMrtg/list.do?menuNo=700007' },
  jeonse: { label: '금융감독원 금융상품한눈에', url: 'https://finlife.fss.or.kr/finlife/ldng/lfstsFunds/list.do?menuNo=700008' },
  creditLoan: { label: '금융감독원 금융상품한눈에', url: 'https://finlife.fss.or.kr/finlife/ldng/indvlCrdt/list.do?menuNo=700009' },
  insurance: { label: 'KB손해보험다이렉트', url: 'https://direct.kbinsure.co.kr/home/' },
};

// "왜 하나밖에 없어?" 같은 목록 후속질문 답변에서 연결해줄 국민은행 "자체" 홈페이지 링크.
// CATEGORY_LIST_SOURCE(금감원 비교 페이지)는 전체 은행 비교용이라 이 상황엔 안 맞다 - 우리가
// 보여준 목록이 적은 건 국민은행 상품이 적어서가 아니라 우리가 쓰는 데이터 출처에 그거밖에
// 없어서일 뿐이고, 실제로 더 보려면 국민은행 홈페이지로 가야 한다(2026-08-11 피드백).
// 아직 예금만 링크 확보함 - 나머지 카테고리는 링크 받으면 추가.
const KB_HOMEPAGE_URL = {
  deposit: { label: 'KB국민은행 예금 상품 홈페이지', url: 'https://obank.kbstar.com/quics?page=C016528' },
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
      pushBot({ text: `지금은 표시할 수 있는 ${categoryLabel} 상품이 없습니다.` });
      return;
    }
    const top = liveProducts.slice(0, 8);
    // 실시간 매물이 하나도 없어도(예: 청약 카테고리 목록엔 고정 상품만 나옴) 그 카테고리 자체의
    // 출처는 항상 표시한다 - 예전엔 매물이 있을 때만 붙어서 청약 목록에 출처가 안 보였음(2026-08-06 피드백)
    const listSource = CATEGORY_LIST_SOURCE[category];
    pushBot({
      text: `${categoryLabel} 상품이에요. 궁금한 상품을 골라주세요.`,
      source: listSource?.label,
      sourceUrl: listSource?.url,
      // 목록 항목이 길어서 세로로 쌓으면 한눈에 안 들어와 - 가로로 넘기는 큰 카드로 보여준다(2026-08-06 피드백)
      menuCarousel: true,
      menu: [
        ...fixedNames.map((name) => ({ label: name, onClick: () => openDoc(name), action: 'openDoc', args: [name] })),
        ...top.map((p) => ({
          label: LIVE_ITEM_LABEL[category](p),
          onClick: () => showLiveProductDetail(LIVE_ITEM_NAME[category](p), category),
          action: 'showLiveProductDetail',
          args: [LIVE_ITEM_NAME[category](p), category],
        })),
      ],
    });
    const shownNames = [...fixedNames, ...top.map((p) => LIVE_ITEM_LABEL[category](p))];
    const kbHomepage = KB_HOMEPAGE_URL[category];
    lastListContext.value = {
      productContext:
        `[${categoryLabel} 목록] 지금 화면에 보여드린 상품은 총 ${shownNames.length}개(${shownNames.join(', ')})입니다. ` +
        `현재 안내된 상품은 국민은행 상품입니다. ` +
        `※ 다른 은행이나 전국은행연합회는 절대 언급하지 마세요. 더 많은 국민은행 상품이 궁금하다고 하면 ` +
        `${kbHomepage ? '국민은행 홈페이지에서 확인해보라고 짧게 안내하세요.' : '국민은행에 직접 문의해보라고 짧게 안내하세요.'}`,
      sourceUrl: kbHomepage?.url,
      sourceLabel: kbHomepage?.label,
    };
  } catch {
    typing.value = false;
    pushError();
  }
};

// 대출 3종 공통 상세 텍스트. loanLmt(대출한도)는 주담대/전세자금대출에만, cbName(신용평가사)은
// 개인신용대출에만 있는 필드라 옵션으로 켜고 끈다. 금리는 min~max 범위(주담대/전세자금) 또는
// crdt_grad_avg를 lendRateAvg 자리에 매핑한 값(개인신용대출, fss.py에서 통일)을 그대로 보여준다.
const loanDetailText = (p, { loanLmt = false, cbName = false } = {}) => {
  const lines = [`${p.korCoNm}에서 제공하는 상품입니다.`, `가입 방법: ${p.joinWay}`];
  if (loanLmt && p.loanLmt) lines.push(`대출한도: ${p.loanLmt}`);
  if (p.erlyRpayFee) lines.push(`중도상환수수료: ${p.erlyRpayFee}`);
  if (cbName && p.cbName) lines.push(`신용평가사: ${p.cbName}`);
  const rates = p.options || [];
  const mins = rates.map((o) => (o.lendRateMin != null ? o.lendRateMin : o.lendRateAvg)).filter((v) => v != null);
  const maxs = rates.map((o) => (o.lendRateMax != null ? o.lendRateMax : o.lendRateAvg)).filter((v) => v != null);
  if (mins.length) {
    const lo = Math.min(...mins);
    const hi = Math.max(...maxs);
    lines.push(lo === hi ? `금리: 연 ${lo}%` : `금리: 연 ${lo}% ~ ${hi}%`);
  }
  return lines.join('\n');
};

const LIVE_DETAIL_TEXT = {
  savings: (p) =>
    `${p.korCoNm}에서 제공하는 상품입니다.\n가입 방법: ${p.joinWay}\n가입 대상: ${p.joinMember}\n우대조건: ${p.spclCnd}${p.etcNote ? `\n기타: ${p.etcNote}` : ''}`,
  deposit: (p) =>
    `${p.korCoNm}에서 제공하는 상품입니다.\n가입 방법: ${p.joinWay}\n가입 대상: ${p.joinMember}\n우대조건: ${p.spclCnd}${p.etcNote ? `\n기타: ${p.etcNote}` : ''}`,
  subscription: (p) =>
    `주소: ${p.hssplyAdres || '정보 없음'}\n청약 접수: ${p.rceptBgnde || '-'}~${p.rceptEndde || '-'}\n입주 예정: ${p.mvnPrearngeYm || '미정'}`,
  investment: (p) => `분류: ${p.ctg || '정보 없음'}\n설정일: ${p.setpDt || '정보 없음'}\n유형: ${p.fndTp || '정보 없음'}`,
  mortgage: (p) => loanDetailText(p, { loanLmt: true }),
  jeonse: (p) => loanDetailText(p, { loanLmt: true }),
  creditLoan: (p) => loanDetailText(p, { cbName: true }),
};

// 후속 질문(LIVE_FOLLOWUP_QUESTIONS)에 실제로 답할 수 있으려면, 화면에 보여준 요약 텍스트보다
// 더 많은 정보가 필요하다 - 화면엔 안 보이지만 API 응답엔 있는 값(가입기간별 금리표, 당첨자
// 발표일 등)까지 넣어서 백엔드에 근거로 실어 보낸다(2026-08-07).
const buildLiveProductContext = (category, p) => {
  const base = LIVE_DETAIL_TEXT[category](p);
  if (category === 'savings' || category === 'deposit') {
    const table = (p.options || [])
      .map((o) => `- ${o.saveTrm}개월: 기본금리 ${o.intrRate}% / 우대금리 적용 시 최고 ${o.intrRate2}%`)
      .join('\n');
    return table ? `${base}\n\n가입기간별 금리:\n${table}` : base;
  }
  if (category === 'subscription') {
    return `${base}\n당첨자 발표일: ${p.przwnerPresnatnDe || '정보 없음'}`;
  }
  return base;
};

// 억지로 4개를 채우지 않고, 화면에 이미 보여준 내용과 겹치지 않는(그래서 실제로 새 정보를 주는)
// 질문만 카테고리별로 큐레이션했다. 투자·대출 3종은 지금 스키마상 화면 밖에 남는 필드가 없어서 비워둠
// - 없는 걸 억지로 만들면 "가입방법이 뭐예요?"처럼 이미 보여준 걸 또 물어보게 돼서 안 넣기로 함(2026-08-07 피드백).
const LIVE_FOLLOWUP_QUESTIONS = {
  savings: ['가입 기간별 금리가 어떻게 돼요?'],
  deposit: ['가입 기간별 금리가 어떻게 돼요?'],
  subscription: ['당첨자 발표는 언제예요?'],
  investment: [],
  mortgage: [],
  jeonse: [],
  creditLoan: [],
};

const showMoreProductsAction = (category) =>
  LOAN_CATEGORIES.includes(category)
    ? { label: '다른 대출 상품도 보여줘', onClick: () => showLoanProducts(), action: 'showLoanProducts', args: [] }
    : {
        label: `다른 ${LIVE_CATEGORY_LABELS[category]} 상품도 보여줘`,
        onClick: () => selectProductCategory(category, LIVE_CATEGORY_LABELS[category]),
        action: 'selectProductCategory',
        args: [category, LIVE_CATEGORY_LABELS[category]],
      };

// productContext: buildLiveProductContext로 만든, 이 상품 하나에 대한 (화면 표시분보다 풍부한) 정보 텍스트.
// 백엔드가 카테고리 전체 요약이 아니라 이 텍스트 하나만 근거로 답하게 된다(2026-08-07).
// category: 답변 뒤에도 "다른 O 상품도 보여줘" 버튼이 남아있어야 계속 다른 상품을 볼 수 있는데,
// 이 후속질문 경로에만 extraMenu가 안 붙어있어서 답변 후 버튼이 통째로 사라졌었다(2026-08-12 발견).
const askLiveProductQuestion = (name, productContext, question, category) => {
  askBackend(question, { title: name, productContext, extraMenu: [showMoreProductsAction(category)] });
};

// 상품 목록 API(개수 목록)와 상세 API(getProduct) 둘 다 options에 saveTrm별 intrRate/intrRate2를
// 그대로 내려주지만, 목록 쪽만 max_rate를 미리 계산해서 얹어준다(fss.py의 max_rate) - 상세 쪽엔
// 없어서 여기서 옵션 배열로 직접 같은 방식(우대금리 있으면 그걸, 없으면 기본금리)으로 계산한다.
const maxRateFromOptions = (options) =>
  Math.max(0, ...(options || []).map((o) => o.intrRate2 ?? o.intrRate ?? 0));

const showLiveProductDetail = async (name, category, { bypassBudgetCheck = false } = {}) => {
  // 목돈 상담(적금)에서 금액·기간을 이미 물어본 상태로 "다른 적금 상품도 보여줘" 등으로 돌아와
  // 실시간 상품을 고른 거면, 설명만 보여주지 말고 그 값으로 계산한 상담 결과를 보여준다
  // (2026-08-11 피드백 - "다른 거 눌러보니까 적금 계산 안 나오고 상품 설명으로 바로 나온다").
  // calculateSavingsEstimate가 자체적으로 pushUser를 호출하므로, 이 분기로 갈 땐 여기서
  // 먼저 pushUser(name)을 부르면 안 된다 - 부르면 사용자 말풍선이 두 번 찍힌다(2026-08-11 발견).
  // bypassBudgetCheck: calculateSavingsEstimate 결과 화면의 "자세히 보기" 버튼처럼, activeSavingsBudget이
  // 살아있는 동안에도 무조건 설명(상세)을 보여줘야 하는 경우 true로 넘긴다 - 안 그러면 그 버튼을 눌러도
  // 방금 본 계산 결과가 또 나오는 무한루프가 된다(고정 상품 자세히 보기에서 이미 한 번 겪은 것과 같은 패턴, 2026-08-11).
  if (!bypassBudgetCheck && activeSavingsBudget.value && category === 'savings') {
    panel.value = null;
    typing.value = true;
    try {
      const { data: p } = await chatApi.getProduct(name, category);
      typing.value = false;
      const { monthlyAmount, months } = activeSavingsBudget.value;
      calculateSavingsEstimate(
        { finPrdtNm: p.finPrdtNm, korCoNm: p.korCoNm, maxRate: maxRateFromOptions(p.options) },
        monthlyAmount,
        months,
      );
    } catch {
      typing.value = false;
      pushError();
    }
    return;
  }

  pushUser(name);
  panel.value = null;
  typing.value = true;
  try {
    const { data: p } = await chatApi.getProduct(name, category);
    typing.value = false;
    const detailText = LIVE_DETAIL_TEXT[category](p);
    const qaContext = buildLiveProductContext(category, p);
    pushBot({
      title: name,
      text: detailText,
      source: p.source,
      // 청약: API 응답에 그 공고의 실제 상세 페이지 링크(PBLANC_URL)가 그대로 들어있어서 바로 연결.
      // 예적금(FSS): 은행별 개별 상품 페이지는 없어서, 대신 이 데이터가 나온 금감원 비교 페이지로 연결(source_url).
      // 펀드(금투협 표준코드): 아직 검증된 링크가 없어서 비워둠.
      sourceUrl: category === 'subscription' ? p.pblancUrl : p.sourceUrl,
      menuFit: true,
      menu: [
        ...LIVE_FOLLOWUP_QUESTIONS[category].map((q) => ({
          label: q,
          onClick: () => askLiveProductQuestion(name, qaContext, q, category),
          action: 'askLiveProductQuestion',
          args: [name, qaContext, q, category],
        })),
        showMoreProductsAction(category),
      ],
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
      // 바로 이동하지 않고 confirmGoalTarget으로 무슨 기능인지 먼저 설명한 뒤 확인받고 이동한다.
      // 선택지 고르는 되묻기라 카드 안 + 글씨 크기에 맞는 버튼으로(2026-08-06 피드백)
      menuInCard: true,
      menu: ALL_FEATURES.map((f) => ({
        label: f.label,
        onClick: () => confirmGoalTarget(f),
        action: 'confirmGoalTarget',
        args: [f],
      })),
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
      // 캐러셀로 해봤는데 용어 개수가 너무 많아서 하나씩 옆으로 넘기는 게 오히려 더 불편하다는
      // 피드백으로 원래 방식(칩이 줄바꿈되는 목록)으로 되돌림. 색도 번갈아 넣어봤다가, 용어가
      // 워낙 많아서 줄마다 색이 섞이면 오히려 산만하다고 해서 밝은 국방색 하나로 통일(2026-08-06)
      menuLight: true,
      menu: terms.map((t) => ({ label: t.term, onClick: () => openTerm(t.term), action: 'openTerm', args: [t.term] })),
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
    pushBot({ title: term, text: data.definition });
    panel.value = 'actions';
  } catch {
    typing.value = false;
    pushError();
  }
};

/* 상품 소개 후 자주 묻는 질문을 하나씩 골라 물어볼 수 있게 함 - 이미 물어본 질문은 다음 메뉴에서 빠진다 */
const openDoc = (name) => {
  // 목돈 상담에서 이미 금액·기간을 물어본 상태로 이 상품(적금 고정 상품)을 고른 거면, 설명 대신
  // 그 값으로 계산한 상담 결과를 보여준다(2026-08-11 피드백). 그 전까진 일반 설명(RAG)으로 남긴다.
  if (activeSavingsBudget.value && FIXED_PRODUCTS_BY_CATEGORY.savings.includes(name)) {
    const { monthlyAmount, months } = activeSavingsBudget.value;
    calculateFixedSavingsEstimate(name, monthlyAmount, months);
    return;
  }
  askProductQuestion(name, null);
};

// FIXED_PRODUCTS_BY_CATEGORY(장병내일준비적금 등 RAG 고정 문서)는 실제로 어느 카테고리(적금/청약/보험)
// 소속인지 이름만 봐선 알 수 없어서, 역으로 찾아주는 조회용 맵 - "다른 O 상품도 보여줘" 버튼을
// 붙일 때 카테고리 라벨이 필요해서 만들었다.
const FIXED_PRODUCT_CATEGORY = Object.fromEntries(
  Object.entries(FIXED_PRODUCTS_BY_CATEGORY).flatMap(([category, names]) => names.map((name) => [name, category])),
);

// previouslyAsked: 지금까지 이 상품에 대해 물어본 질문 전부(호출마다 누적해서 넘겨받음).
// 전엔 이번에 물어본 것 하나만 메뉴에서 뺐어서, 그 전에 이미 물어봤던 질문이 다시 나타나는
// 문제가 있었다(2026-08-12 발견) - 방금 물어본 것까지 합쳐서 계속 쌓아가며 전부 제외한다.
const askProductQuestion = (name, askedQuestion, previouslyAsked = []) => {
  const askedSoFar = askedQuestion ? [...previouslyAsked, askedQuestion] : previouslyAsked;
  const remaining = (PRODUCT_QUESTIONS[name] || []).filter((q) => !askedSoFar.includes(q));
  const extraMenu = remaining.map((q) => ({ label: q, onClick: () => askProductQuestion(name, q, askedSoFar) }));
  // 실시간 상품 상세(showLiveProductDetail)에는 있던 "다른 O 상품도 보여줘" 버튼이 장병내일준비적금/
  // 청년미래적금 같은 고정 문서 상품에는 빠져있었다(2026-08-11 피드백) - 같은 카테고리 목록으로
  // 돌아갈 방법이 없어서 "처음으로"까지 눌러야 했던 문제라 여기도 똑같이 붙여준다.
  const category = FIXED_PRODUCT_CATEGORY[name];
  if (category) extraMenu.push(showMoreProductsAction(category));
  askBackend(askedQuestion || name, { title: askedQuestion || name, extraMenu });
};

/* 목돈 상담 - 되묻기형(목적 -> 목적별 분기) */
const openCounsel = () => {
  counselInputHandler.value = null;
  activeSavingsBudget.value = null; // 새 상담을 시작하니 이전에 물어봤던 금액·기간은 초기화
  pushUser('목돈 어떻게 쓸지 상담받기');
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    startCounsel();
  }, TYPING_DELAY_MS);
};

// introText: 자유입력에서 상담으로 분류됐을 때 백엔드가 주는 안내 문구("자세한 상담을 위해...")를
// 별도 말풍선으로 안 띄우고 이 카드 문구 앞줄에 합친다 - 안내와 되묻기가 나눠 뜰 필요가 없다(2026-08-08 피드백)
const startCounsel = (introText = null) => {
  pushBot({
    title: '자금 상담',
    text: `${introText || '몇 가지만 여쭤볼게요.'}\n어떤 목적으로 목돈을 활용하고 싶으세요?`,
    // 되묻기는 질문의 일부라서 답변 카드 밖으로 안 빼고 카드 안에서 바로 고르게 한다(2026-08-06 피드백)
    menuInCard: true,
    menu: COUNSEL_GOALS.map((g) => ({ label: g.label, onClick: () => askGoal(g), action: 'askGoal', args: [g] })),
  });
};

const askGoal = (goal, { announce = true, introText = null } = {}) => {
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
        menuFit: true,
        menu: [{ label: rentLink.label, onClick: () => goTo(rentLink.to), action: 'goTo', args: [rentLink.to] }],
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
        // 목록에 바로 한 줄 설명을 붙여서(클릭 한 번 더 안 거치고) 뭔지 알고 고를 수 있게 한다(2026-08-06 피드백)
        text:
          '생활자금 관리는 아래 기능에서 도와드릴 수 있어요.\n' +
          '· 자금 시뮬레이션: 월 납입액과 기간을 넣어 향후 자금 흐름을 미리 계산해볼 수 있어요.\n' +
          '· 후회소비 회고: 지난 소비를 되돌아보며 후회되는 지출을 기록하고 다음 소비 습관을 점검할 수 있어요.',
        // 버튼을 글씨 크기에 맞게 줄이고, 문구도 "~페이지로 이동"류로 다른 이동 버튼들과 통일(2026-08-06 피드백)
        menuFit: true,
        menu: [
          {
            label: GUIDE_NAV_LABELS.Simulator,
            onClick: () => goTo({ name: 'Simulator' }),
            action: 'goTo',
            args: [{ name: 'Simulator' }],
          },
          {
            label: GUIDE_NAV_LABELS.RegretReview,
            onClick: () => goTo({ name: 'RegretReview' }),
            action: 'goTo',
            args: [{ name: 'RegretReview' }],
          },
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
  askSavingsMethod(introText);
};

/* 목돈 모으기 - "어떤 방식으로" 되묻기. 적금은 실제 계산(월납입액/기간 직접입력 -> 계산기 API),
   예금은 실시간 예금 상품 목록, 투자는 기존 투자 수익 흐름(기간->성향->실시간 펀드) 재사용,
   목표부터 정하기는 상품이 아니라 기존 목표 로드맵 페이지(여행/자취/진로/자차)로 안내한다 */
// introText: 자유입력에서 목적이 이미 추론돼 안내 문구가 있는 경우, 별도 말풍선으로 안 띄우고
// 이 되묻기 문구 앞에 합쳐서 한 말풍선으로 보여준다(2026-08-08 피드백의 startCounsel과 동일 패턴 -
// "군적금으로 뭐하지?"처럼 목적 자동 인식으로 여기까지 바로 온 경우엔 이 병합이 빠져있었음, 2026-08-12 발견).
const askSavingsMethod = (introText = null) => {
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    pushBot({
      text: introText ? `${introText}\n어떤 방식으로 모으고 싶으세요?` : '어떤 방식으로 모으고 싶으세요?',
      // 되묻기는 질문의 일부라서 답변 카드 밖으로 안 빼고 카드 안에서 바로 고르게 한다(2026-08-06 피드백)
      menuInCard: true,
      menu: [
        { label: '적금', onClick: () => chooseSavingsMethod('적금'), action: 'chooseSavingsMethod', args: ['적금'] },
        { label: '예금', onClick: () => chooseSavingsMethod('예금'), action: 'chooseSavingsMethod', args: ['예금'] },
        { label: '투자', onClick: () => chooseSavingsMethod('투자'), action: 'chooseSavingsMethod', args: ['투자'] },
        {
          // 한 줄에 나머지 3개(적금/예금/투자)랑 같이 걸치게 짧은 문구로(2026-08-06 피드백)
          label: '목표 정하기',
          onClick: () => chooseSavingsMethod('목표부터 정하기'),
          action: 'chooseSavingsMethod',
          args: ['목표부터 정하기'],
        },
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
    menuInCard: true,
    menu: EXTERNAL_NAV.map((n) => ({ label: n.label, onClick: () => confirmGoalTarget(n), action: 'confirmGoalTarget', args: [n] })),
  });
};

// {label, description, to} 모양이면 어디서든 재사용 - "목표부터 정하기" 되묻기 말고도
// 가이드 카드 첫 섹션, "군적금 활용하기" 전체 목록에서도 같은 패턴(설명 먼저, 그다음 이동)으로 쓴다
const confirmGoalTarget = (navItem) => {
  pushUser(navItem.label);
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    pushBot({
      text: `저희 서비스에 ${navItem.description}이 있는데, 확인해 보시겠습니까?`,
      menuFit: true,
      menu: [
        {
          label: GUIDE_NAV_LABELS[navItem.to.name] || navItem.label,
          onClick: () => goTo(navItem.to),
          action: 'goTo',
          args: [navItem.to],
        },
      ],
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

// "24", "24개월", "2년", "2년 6개월" 형태를 모두 개월수로 변환. 못 알아들으면 null.
// 적금은 보통 1년 단위로도 얘기하는데 개월수로만 받으면 불편하다는 피드백(2026-08-06) 반영.
const parseKoreanDuration = (text) => {
  const cleaned = text.replace(/\s/g, '');
  const yearMatch = cleaned.match(/^(\d+)년(?:(\d+)개월)?$/);
  if (yearMatch) {
    const years = parseInt(yearMatch[1], 10);
    const extraMonths = yearMatch[2] ? parseInt(yearMatch[2], 10) : 0;
    return years * 12 + extraMonths;
  }
  const monthMatch = cleaned.match(/^(\d+)개월$/);
  if (monthMatch) return parseInt(monthMatch[1], 10);
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
  pushBot({ text: '얼마 동안 모으실 계획이세요? 개월 수든 연 단위든 편하게 입력해주세요. (예: 24, 24개월, 2년)' });
  counselInputHandler.value = (t) => handleSaveMonthsInput(t, amount);
};

// 목돈 모으기는 군적금을 든다고 확정한 게 아니라 "적금으로 모으고 싶다"는 것뿐이라,
// 실제 은행별 적금 상품 중 하나를 고르게 한 다음 그 상품 금리로 계산해준다
// (예전엔 군적금(장병내일준비적금) 전용 계산기를 무조건 썼는데, 그건 최대 24개월 제한도 있고
// 사용자가 군적금을 든다고 한 적도 없어서 부적절했음, 2026-08-06 피드백) */
const handleSaveMonthsInput = async (text, monthlyAmount) => {
  pushUser(text);
  const months = parseKoreanDuration(text);
  if (!months || months <= 0) {
    pushBot({ text: '기간을 다시 확인해주세요. (예: 24, 24개월, 2년)' });
    counselInputHandler.value = (t) => handleSaveMonthsInput(t, monthlyAmount);
    return;
  }
  panel.value = null;
  typing.value = true;
  // 이후 다른 경로(예: "다른 적금 상품도 보여줘")로 장병내일준비적금 같은 고정 상품을 골라도
  // 이 금액·기간이 그대로 계산에 쓰이도록 기억해둔다.
  activeSavingsBudget.value = { monthlyAmount, months };
  try {
    const { data: products } = await chatApi.listProducts('savings');
    typing.value = false;
    if (!products.length) {
      pushBot({ text: '지금은 표시할 수 있는 적금 상품이 없습니다.' });
      return;
    }
    const top = products.slice(0, 8);
    pushBot({
      text: `월 ${won(monthlyAmount)}씩 ${months}개월 모을 적금 상품을 골라주세요.`,
      source: CATEGORY_LIST_SOURCE.savings.label,
      sourceUrl: CATEGORY_LIST_SOURCE.savings.url,
      menuCarousel: true,
      menu: [
        // 장병내일준비적금/청년미래적금도 이 목록에서 바로 고를 수 있게 같이 넣는다(2026-08-11 피드백) -
        // 예전엔 실시간 상품만 있어서, 군적금을 원하는 사용자는 "다른 상품도 보여줘"까지 돌아가야 했다.
        ...FIXED_PRODUCTS_BY_CATEGORY.savings.map((name) => ({
          label: name,
          onClick: () => calculateFixedSavingsEstimate(name, monthlyAmount, months),
          action: 'calculateFixedSavingsEstimate',
          args: [name, monthlyAmount, months],
        })),
        ...top.map((p) => ({
          label: `${p.finPrdtNm} (${p.korCoNm} · 최고 ${p.maxRate}%)`,
          onClick: () => calculateSavingsEstimate(p, monthlyAmount, months),
          action: 'calculateSavingsEstimate',
          args: [p, monthlyAmount, months],
        })),
      ],
    });
    panel.value = 'actions';
  } catch {
    typing.value = false;
    pushError();
  }
};

// 장병내일준비적금처럼 금리·매칭지원금 비율이 고정돼 공개된 상품만 계산해준다 - 청년미래적금처럼
// "은행마다 자율 결정"이라 고정 수치가 없는 상품은 숫자를 지어내면 안 되니 여기 안 넣는다.
// 수치는 RAG 문서(장병내일준비적금 안내)에 안내된 것과 동일하게 맞춤(기본이자 5%, 매칭지원금 100%,
// 최대 24개월, 개인별 월 최대 55만원).
const FIXED_SAVINGS_RULES = {
  장병내일준비적금: { maxMonths: 24, annualRate: 0.05, matchRate: 1.0, maxMonthlyAmount: 550000 },
};

const calculateFixedSavingsEstimate = (name, monthlyAmount, months) => {
  pushUser(name);
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    const rule = FIXED_SAVINGS_RULES[name];
    if (!rule) {
      // 청년미래적금 등 고정 금리가 없는 상품은 예상 금액 대신, 확실히 아는 사실(정부기여금 비율)만 안내한다.
      pushBot({
        title: name,
        text:
          `${name}은(는) 금리를 은행마다 자율로 정해서 정확한 예상 금액은 계산해드리기 어려워요.\n` +
          `다만 정부기여금은 월 납입액 기준 일반형 6%, 우대형 12%로 지급돼요.\n` +
          `구체적인 가입 조건이 더 궁금하시면 말씀해주세요.`,
        menuFit: true,
        menu: [{ label: `${name} 자세히 보기`, onClick: () => askProductQuestion(name, null), action: 'askProductQuestion', args: [name, null] }],
      });
      panel.value = 'actions';
      return;
    }
    const cappedMonths = Math.min(months, rule.maxMonths);
    const cappedAmount = Math.min(monthlyAmount, rule.maxMonthlyAmount);
    let totalInterest = 0;
    for (let i = 1; i <= cappedMonths; i += 1) {
      const investedMonths = cappedMonths - i + 1;
      totalInterest += cappedAmount * rule.annualRate * (investedMonths / 12);
    }
    const totalPrincipal = cappedAmount * cappedMonths;
    const matchingFund = totalPrincipal * rule.matchRate;
    const totalReceiptAmount = totalPrincipal + totalInterest + matchingFund;
    const capNotes = [];
    if (months > rule.maxMonths) capNotes.push(`최대 가입기간이 ${rule.maxMonths}개월이라 ${rule.maxMonths}개월로 계산했어요.`);
    if (monthlyAmount > rule.maxMonthlyAmount) capNotes.push(`개인별 최대 월 납입액이 ${won(rule.maxMonthlyAmount)}이라 그 금액으로 계산했어요.`);
    pushBot({
      title: '적금 상담 결과',
      text:
        `${name} 기준으로\n` +
        `월 ${won(cappedAmount)}씩 ${cappedMonths}개월 납입하면\n` +
        `원금 ${won(totalPrincipal)} + 이자(세전 예상) ${won(totalInterest)} + 정부 매칭지원금 ${won(matchingFund)}\n` +
        `= 총 ${won(totalReceiptAmount)}을 받으실 수 있어요.\n` +
        (capNotes.length ? `(${capNotes.join(' ')})\n` : '') +
        `(실제 가입 조건 충족 여부에 따라 달라질 수 있어요)`,
      menuFit: true,
      // openDoc(name)을 쓰면 activeSavingsBudget이 살아있는 동안 이 버튼을 눌러도 방금 본 계산
      // 결과가 또 나오는 무한루프가 됐다(2026-08-11 발견) - "자세히 보기"는 예산 상태와 상관없이
      // 항상 설명(RAG)으로 가야 해서 askProductQuestion을 직접 부른다.
      menu: [{ label: `${name} 자세히 보기`, onClick: () => askProductQuestion(name, null), action: 'askProductQuestion', args: [name, null] }],
    });
    panel.value = 'actions';
  }, TYPING_DELAY_MS);
};

// won() - 상담 결과 문구 곳곳에서 반복 쓰여서 공용으로 뺌
const won = (n) => `${Math.round(n).toLocaleString('ko-KR')}원`;

// 고른 적금 상품의 실제 공시금리(maxRate, 세전)로 간단 이자를 추정한다. 은행 상품 비교 API라
// 군적금 같은 정부기여금 구조는 없어서 원금+이자만 보여주고, 실제와 다를 수 있다고 안내한다.
const calculateSavingsEstimate = (product, monthlyAmount, months) => {
  pushUser(product.finPrdtNm);
  panel.value = null;
  typing.value = true;
  setTimeout(() => {
    typing.value = false;
    const annualRate = (product.maxRate || 0) / 100;
    let totalInterest = 0;
    for (let i = 1; i <= months; i += 1) {
      const investedMonths = months - i + 1;
      totalInterest += monthlyAmount * annualRate * (investedMonths / 12);
    }
    const totalPrincipal = monthlyAmount * months;
    const totalReceiptAmount = totalPrincipal + totalInterest;
    pushBot({
      title: '적금 상담 결과',
      text:
        `${product.finPrdtNm}(${product.korCoNm}) 최고금리 기준으로\n` +
        `월 ${won(monthlyAmount)}씩 ${months}개월 납입하면\n` +
        `원금 ${won(totalPrincipal)} + 이자(세전 예상) ${won(totalInterest)}\n` +
        `= 총 ${won(totalReceiptAmount)}을 받으실 수 있어요.\n` +
        `(실제 금리·우대조건 충족 여부에 따라 달라질 수 있어요)`,
      menuFit: true,
      menu: [
        {
          label: `${product.finPrdtNm} 자세히 보기`,
          // bypassBudgetCheck: true - 안 넘기면 activeSavingsBudget이 살아있는 동안 이 버튼이 방금 본
          // 계산 결과를 또 보여주는 무한루프가 된다(2026-08-11 발견, showLiveProductDetail 정의부 참고).
          onClick: () => showLiveProductDetail(product.finPrdtNm, 'savings', { bypassBudgetCheck: true }),
          action: 'showLiveProductDetail',
          args: [product.finPrdtNm, 'savings'],
        },
      ],
    });
    panel.value = 'actions';
  }, TYPING_DELAY_MS);
};

const askPeriod = () => {
  pushBot({
    text: '목표 기간이 어떻게 되세요?',
    menuInCard: true,
    menu: ['1년 이하', '1~3년', '3년 이상'].map((p) => ({ label: p, onClick: () => askType(p), action: 'askType', args: [p] })),
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
      menuInCard: true,
      menu: ['안정추구형', '중립형', '공격투자형'].map((t) => ({
        label: t,
        onClick: () => finishCounsel(period, t),
        action: 'finishCounsel',
        args: [period, t],
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
      });
    } else {
      pushBot({
        title: `${period} · ${type} 추천`,
        text: `${COUNSEL_PERIOD_NOTE[period]}\n${type}에 맞는 펀드를 모아봤어요.`,
        source: CATEGORY_LIST_SOURCE.investment.label,
        // 목록 항목이 길어서 세로로 쌓으면 한눈에 안 들어와 - 가로로 넘기는 큰 카드로 보여준다(2026-08-06 피드백)
        menuCarousel: true,
        menu: matched.map((f) => ({
          label: f.fndNm,
          onClick: () => showLiveProductDetail(f.fndNm, 'investment'),
          action: 'showLiveProductDetail',
          args: [f.fndNm, 'investment'],
        })),
      });
    }
    panel.value = 'actions';
  } catch {
    typing.value = false;
    pushError();
  }
};

/* 자유 입력 텍스트를 실제 백엔드(RAG/Gemini)로 보내고 답변을 받는다 */
// productContext: 실시간 상품 상세를 이미 보여준 뒤 그 상품 하나에 대해 후속 질문할 때만 채운다
// (buildLiveProductContext로 만든 텍스트). 채워지면 카테고리 전체가 아니라 이 상품 하나만 근거로
// 답하도록 백엔드에 그대로 실어 보낸다(2026-08-07).
const askBackend = async (
  text,
  { title, extraMenu = [], forceInfo = false, productContext = null, fallbackSourceUrl = null, fallbackSourceLabel = null } = {},
) => {
  counselInputHandler.value = null;

  // 자동차/자취/진로/계산기처럼 이미 우리 서비스에 있는 기능과 명확히 관련된 질문이면, AI(Gemini) 호출도
  // 안 하고 바로 그 기능 안내로 답한다. 예전엔 일단 RAG 답변부터 받아서(대부분 "참고 자료에 없다"는
  // 엉뚱한 내용) 뒤에 안내 문구만 덧붙였는데, 그 앞부분이 질문이랑 안 맞아서 오히려 헷갈린다는
  // 피드백(2026-08-06) - 이제 그 답변 자체를 아예 안 보여주고 깔끔하게 안내만 한다.
  // productContext가 있거나(실시간 상품 후속질문) extraMenu가 있는(상품명 되묻기 흐름, askProductQuestion)
  // 경우는 이 페이지-링크 안내로 새지 않고 그대로 상품 Q&A로 간다. "KB손해보험 자동차보험(개인)"처럼
  // 상품명 자체에 다른 기능 키워드("자동차")가 우연히 들어있으면, 답변 없이 바로 그 기능 안내로
  // 튀어버리는 문제가 있었다(2026-08-08 피드백) - 아래에서 답변을 먼저 보여준 뒤 안내를 덧붙이는 걸로 바꿈.
  const pageLink =
    !productContext && !extraMenu.length && PAGE_LINKS.find((p) => p.keywords.some((k) => text.includes(k)));
  if (pageLink) {
    pushUser(text);
    input.value = '';
    panel.value = null;
    typing.value = true;
    setTimeout(() => {
      typing.value = false;
      pushBot({
        text: `저희 서비스에 ${pageLink.description}이 있는데, 확인해 보시겠습니까?`,
        menuFit: true,
        menu: [{ label: pageLink.label, onClick: () => goTo(pageLink.to), action: 'goTo', args: [pageLink.to] }],
      });
      panel.value = 'actions';
    }, TYPING_DELAY_MS);
    return;
  }

  // 이 질문은 아래 chatApi.sendMessage가 서버에 직접 저장하니, pushUser에서 또 기록하면 중복된다.
  pushUser(text, { skipLog: true });
  input.value = '';
  panel.value = null;
  typing.value = true;
  try {
    const { data: botMsg } = await chatApi.sendMessage(sessionId.value, text, forceInfo, productContext);
    typing.value = false;

    // 백엔드가 자유입력을 상담(counsel)으로 분류하면, 일반 RAG 답변 대신
    // 되묻기 플로우로 분기한다 (WBS-6) - 가이드 화면의 "목돈 상담받기" 버튼과 동일한 흐름 재사용.
    // 텍스트에 목적이 이미 드러나 있으면(예: "투자해보고싶어") 목적 질문은 건너뛴다.
    if (botMsg.intent === 'counsel') {
      const directCategory = detectDirectListCategory(text);
      if (directCategory) {
        // 상담 안내 문구("자세한 상담을 위해...")는 안 어울려서 안 보여주고 바로 목록으로 간다
        await showProductCategoryList(directCategory.category, directCategory.label);
        return;
      }
      const matchedGoal = detectCounselGoal(text);
      if (matchedGoal) {
        // 안내 문구와 되묻기 카드를 별도 말풍선 2개로 따로 띄웠었는데, "목적 되묻기와 안내 문구를
        // 한 말풍선으로 합친다(2026-08-08)"는 이미 정해진 방향이라 여기도 맞춘다(2026-08-12 발견) -
        // introText로 넘겨서 askGoal 쪽(현재는 savings만) 되묻기 문구 앞에 합쳐서 한 번에 띄운다.
        askGoal(matchedGoal, { announce: false, introText: botMsg.content });
      } else {
        // 목적 되묻기 카드와 안내 문구를 한 말풍선으로 합친다(2026-08-08 피드백)
        startCounsel(botMsg.content);
      }
      return;
    }

    const menu = [...extraMenu];
    const answerText = botMsg.content;

    // 답변에서 특정 상품이 언급됐으면, 그 상품의 큐레이션된 후속 질문들을 바로 붙인다.
    // 예전엔 "더 자세한 내용 확인해보기"로 뭉뚱그려서 물어봤는데, 그럼 백엔드가 뭘 더 알고
    // 싶은 건지 몰라서 "은행연합회 가서 확인하라"는 식으로 떠넘기는 답이 나왔다(2026-08-07 피드백).
    // 이미 그 상품의 되묻기 메뉴(extraMenu)가 붙어있으면(=이미 상품 Q&A 흐름 안) 중복이라 스킵
    if (!extraMenu.length) {
      const relatedProduct = Object.keys(PRODUCT_QUESTIONS).find(
        (name) => botMsg.content.includes(name) || (botMsg.sourceDetail || '').includes(name),
      );
      if (relatedProduct) {
        menu.push(
          ...PRODUCT_QUESTIONS[relatedProduct].map((q) => ({
            label: q,
            onClick: () => askProductQuestion(relatedProduct, q),
            action: 'askProductQuestion',
            args: [relatedProduct, q],
          })),
        );
      }
    }

    // 상품명 자체가 다른 서비스 기능 키워드와 겹치는 경우(예: "자동차보험" -> 자차 준비 기능),
    // 위에서 실제 답변은 이미 정상적으로 보여줬으니 그 답변을 대체하지 않고 안내만 추가로 붙인다
    // (내 집 마련 흐름과 같은 패턴 - 청약 상품 먼저 보여주고 자취 준비 기능을 덧붙임, 2026-08-08 피드백)
    const relatedPageLink = PAGE_LINKS.find((p) => p.keywords.some((k) => text.includes(k)));
    if (relatedPageLink) {
      menu.push({
        label: relatedPageLink.label,
        onClick: () => goTo(relatedPageLink.to),
        action: 'goTo',
        args: [relatedPageLink.to],
      });
    }

    const bubble = {
      id: `bot-${botMsg.messageId}`,
      role: 'bot',
      time: formatBubbleTime(botMsg.createdDate),
      title,
      text: answerText,
      source: botMsg.source,
      // 목록 후속질문(product_context)은 백엔드가 RAG를 안 타서 sourceUrl이 안 내려온다 - 그 경우
      // 방금 보여준 목록의 출처(금융감독원 비교 페이지 등)로라도 연결해준다(2026-08-11 피드백).
      sourceDetail: botMsg.sourceUrl ? botMsg.sourceDetail : botMsg.sourceDetail || fallbackSourceLabel,
      sourceUrl: botMsg.sourceUrl || fallbackSourceUrl,
      isAiGenerated: botMsg.isAiGenerated,
      // 카드 안에 넣었었는데, 상품 후속 질문 목록은 답변과는 별개의 선택지라 카드 밖으로 다시 빼고
      // 가이드 태그줄과 같은 국방색 번갈아 넣기 스타일로(2026-08-06 피드백)
      menuFit: true,
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
  } catch (error) {
    typing.value = false;
    // 400번대(글자수 초과 등 사용자가 바로 고칠 수 있는 입력 오류)는 백엔드가 준 구체적인 안내
    // 문구를 그대로 보여주고, 그 외(500·네트워크 오류 등 사용자가 어찌할 수 없는 상황)는 기존처럼
    // 범용 오류 문구를 보여준다.
    const status = error?.response?.status;
    const backendMessage = error?.response?.data?.message;
    pushError(status && status < 500 ? backendMessage : undefined);
  }
};

// 메뉴 버튼의 onClick은 클로저라 그대로 서버에 저장할 수 없다. 대신 각 메뉴 항목에 같이 실어 보내는
// action(함수 이름 문자열) + args(단순 값 배열)를 여기 등록해둔 함수로 다시 매핑해서, 새로고침·재로그인
// 후에도(또는 "이전 기록"에서 지난 세션을 열어도) 히스토리의 버튼이 실제로 다시 눌리게 한다.
// 여기 없는 액션은 그냥 못 누르는 문구로만 남는다(치명적이지 않음 - 텍스트는 항상 보존됨).
const ACTIONS = {
  selectProductCategory,
  askProductQuestion,
  showLoanProducts,
  askLiveProductQuestion,
  openDoc,
  showLiveProductDetail,
  goTo,
  askGoal,
  chooseSavingsMethod,
  confirmGoalTarget,
  askType,
  finishCounsel,
  askBackend,
  openTerm,
  calculateSavingsEstimate,
};

const runAction = (opt) => {
  const fn = opt.action && ACTIONS[opt.action];
  if (fn) fn(...(opt.args || []));
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

  // 방금 실시간 상품 "목록"을 보여준 직후의 자유 후속질문이면(예: "왜 하나밖에 없어?"), 그 목록
  // 정보를 1회성으로 근거에 실어 보낸다 - 안 그러면 화면에 뭐가 떠 있는지 모른 채 일반 RAG 검색을
  // 타서 엉뚱한 문서가 근거로 잡힌다(2026-08-11 피드백). 쓰고 나면 바로 비워서 다음 질문엔 안 새게 함.
  const listContext = lastListContext.value;
  lastListContext.value = null;

  // 그 외엔 백엔드의 classify_intent("counsel") 분류 결과로 상담형 되묻기 진입 여부를 판단한다
  // (askBackend 내부에서 botMsg.intent === 'counsel'이면 되묻기 플로우로 분기)
  askBackend(
    trimmed,
    listContext
      ? {
          productContext: listContext.productContext,
          fallbackSourceUrl: listContext.sourceUrl,
          fallbackSourceLabel: listContext.sourceLabel,
        }
      : {},
  );
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

// 만족도(좋아요/보통/싫어요)는 저위험 데이터라 저장 실패를 사용자에게 따로 알릴 필요가 없다고
// 판단해 토스트 안내를 뺐다(2026-08-12 피드백) - 서버 쪽 진짜 오류는 서버 로그로 이미 잡히고,
// 클라이언트에서 요청 자체가 막힌 경우(네트워크 등)는 애초에 서버 로그로도 못 잡는 케이스라
// 토스트를 남겨도 실효성이 없었다. 성공/실패 상관없이 모달은 항상 닫고 감사 인사로 마무리한다.
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
    // 실패해도 사용자에게 별도 안내 없이 조용히 넘어간다.
  } finally {
    feedbackSubmitting.value = false;
  }
  feedbackModalOpen.value = false;
  panel.value = null;
  pushBot({ text: '소중한 의견 감사합니다 🙌' });
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
      // 그 상품에 대한 후속 질문 중 하나였던 경우 -> 지금까지 이 상품에 물어본 질문을 전부 모아서
      // 그것들만 빼고 다시 보여준다. 전엔 방금 물어본 것 하나만 뺐어서, 새로고침 후 복원하면
      // 그 전에 이미 물어봤던 질문이 되살아나는 문제가 있었다(2026-08-12 발견, 라이브 화면과
      // 동일한 버그가 히스토리 복원 로직에도 그대로 있었음).
      const askedSoFar = [];
      for (let i = index - 1; i >= 0; i -= 1) {
        const msg = history[i];
        if (msg.role !== 'user') continue;
        if (msg.content === activeProduct) break; // 이 상품 Q&A가 시작된 지점까지 왔으면 멈춤
        if (PRODUCT_QUESTIONS[activeProduct].includes(msg.content)) askedSoFar.push(msg.content);
      }
      const remaining = PRODUCT_QUESTIONS[activeProduct].filter((q) => !askedSoFar.includes(q));
      return { menu: remaining.map((q) => ({ label: q, onClick: () => askProductQuestion(activeProduct, q, askedSoFar) })) };
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
    // askBackend와 동일하게 뭉뚱그린 재질문 대신 그 상품의 큐레이션된 후속 질문을 그대로 복원(2026-08-07)
    return {
      menu: PRODUCT_QUESTIONS[relatedProduct].map((q) => ({
        label: q,
        onClick: () => askProductQuestion(relatedProduct, q),
      })),
    };
  }
  return { menu: [] };
};

// pushBot이 남겨둔 #MENU# 마커를 다시 읽어서 그 메뉴 버튼을 되살린다(ACTIONS에 등록된 액션만 가능).
// 마커가 없으면(예전 자유 질문 답변 등) null을 돌려주고, 그럴 땐 deriveHistoryMenu의 추측 로직을 대신 쓴다.
const decodeMenuMarker = (content) => {
  const idx = content.indexOf(MENU_MARKER);
  if (idx === -1) return { text: content, menu: null, style: null };
  const text = content.slice(0, idx);
  try {
    const parsed = JSON.parse(content.slice(idx + MENU_MARKER.length));
    // 예전 형식(스타일 없이 항목 배열만 저장)과도 호환되게 - 배열이면 그냥 items로 취급
    const items = Array.isArray(parsed) ? parsed : parsed.items;
    const style = Array.isArray(parsed) ? {} : parsed.style || {};
    return {
      text,
      style,
      menu: items.map((it) => ({ label: it.label, onClick: () => runAction(it) })),
    };
  } catch {
    return { text, menu: null, style: null };
  }
};

const toBubble = (m, history, index) => {
  if (m.role === 'user') {
    return { id: `hist-${m.messageId}`, role: 'user', text: m.content, time: formatBubbleTime(m.createdDate) };
  }
  if (m.content.includes(ERROR_MARKER)) {
    return { id: `hist-${m.messageId}`, role: 'error', text: m.content.replace(ERROR_MARKER, '') };
  }
  const decoded = decodeMenuMarker(m.content);
  if (decoded.menu) {
    return {
      id: `hist-${m.messageId}`,
      role: 'bot',
      time: formatBubbleTime(m.createdDate),
      text: decoded.text,
      source: m.source,
      sourceDetail: m.sourceDetail,
      isAiGenerated: m.isAiGenerated,
      menu: decoded.menu,
      ...decoded.style,
    };
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
    // 실시간 렌더링에서 이런 되묻기(상품 후속질문 등)는 카드 밖 태그칩 스타일(menuFit)로 뜨는데,
    // 여기가 menuInCard였어서 새로고침 후 히스토리 복원 때만 카드 안으로 잘못 들어가 보였다(2026-08-07 피드백)
    menuFit: menu.length > 0,
    menu,
  };
};

// 날짜가 바뀌는 지점마다 구분선(role: 'date')을 끼워 넣으면서 히스토리 전체를 버블로 바꾼다.
// 대화가 하루 단위로 안 끊기고 쭉 이어지므로(세션도 유저당 하나만 재사용), 날짜 구분은
// 세션 경계가 아니라 실제 날짜가 바뀌는 지점 기준으로 계산해야 한다.
const buildHistoryWithDateDividers = (history, initialDay = null) => {
  const out = [];
  let lastDay = initialDay;
  history.forEach((m, i) => {
    const day = new Date(m.createdDate).toDateString();
    if (day !== lastDay) {
      out.push({ id: `date-${day}`, role: 'date', label: dateLabelFor(m.createdDate) });
      lastDay = day;
    }
    out.push(toBubble(m, history, i));
  });
  return out;
};

// 그 날짜 구분선 다음, 다음 구분선(또는 끝)이 나오기 전까지에서 첫 사용자 메시지를 찾아
// 짧게 요약한다 - 날짜만 덜렁 있으면 뭘 물어본 날인지 구분이 안 돼서 붙여준다.
const placeholderUserLabels = ['직접 질문 입력하기'];
const summarizeDateEntry = (dateIndex) => {
  for (let j = dateIndex + 1; j < messages.value.length; j += 1) {
    const next = messages.value[j];
    if (next.role === 'date') break;
    if (next.role === 'user' && next.text && !placeholderUserLabels.includes(next.text)) {
      return next.text.length > 14 ? `${next.text.slice(0, 14)}…` : next.text;
    }
  }
  return null;
};

// "이전 기록" - 예전엔 날짜별로 세션을 따로 열어봤지만, 지금은 대화가 하나로 이어져 있어서
// 이미 화면에(messages) 다 올라와 있다. 그래서 서버에 다시 물어볼 필요 없이, 지금 떠 있는
// 날짜 구분선 목록만 보여주고 고르면 그 지점으로 스크롤만 이동시킨다.
const openHistory = () => {
  const dateEntries = messages.value
    .map((m, i) => (m.role === 'date' ? { ...m, index: i } : null))
    .filter(Boolean);
  if (dateEntries.length <= 1) {
    pushBot({
      title: '최근 이전 대화',
      text: '아직 다른 날짜의 기록이 없어요.',
    });
    return;
  }
  pushBot({
    title: '최근 이전 대화',
    text: '날짜를 골라주세요.',
    // 카드 안 흰 버튼 대신 다른 선택지들과 같은 국방색 톤(연한 배경 + hover 시 진하게)으로 통일(2026-08-08 피드백)
    menuFit: true,
    // 제목이 "최근" 이전 대화인데 실제론 날짜가 있는 만큼 전부 다 나오고 있었다(2026-08-11 피드백) -
    // dateEntries는 오래된 순으로 쌓여있으니 뒤에서 3개(가장 최근 3일)만 자른다.
    menu: dateEntries.slice(-3).map((d) => {
      const summary = summarizeDateEntry(d.index);
      const label = summary ? `${formatDate(d.id.replace('date-', ''))} · ${summary}` : d.label;
      return { label, onClick: () => jumpToDate(d.id), action: 'jumpToDate', args: [d.id] };
    }),
  });
};

const jumpToDate = (dateId) => {
  nextTick(() => {
    document.getElementById(dateId)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
};
// ACTIONS는 jumpToDate보다 앞에서 선언돼서(const라 선언 전엔 참조 못 함) 여기서 뒤늦게 등록해준다.
ACTIONS.jumpToDate = jumpToDate;

// 마지막 메시지가 봇 답변이면 "종료하기" 버튼을 다시 보여준다.
// (실시간 대화 중엔 답변 직후 panel='actions'가 되지만, 새로고침/재진입으로 히스토리를
// 불러올 때는 이 상태가 초기화되므로 다시 계산해줘야 한다)
const restorePanelFromHistory = (historyMessages) => {
  const last = historyMessages[historyMessages.length - 1];
  panel.value = last?.role === 'bot' ? 'actions' : null;
};

onMounted(async () => {
  loading.value = true;
  const landingStartedAt = Date.now();
  try {
    const { data: session } = await chatApi.createSession();
    sessionId.value = session.sessionId;

    if (session.isNew) {
      messages.value = buildGreetAndGuide();
      lastShownDay.value = new Date().toDateString();
    } else {
      // 유저당 세션을 하나만 재사용하지만, 예전에(하루 단위로 세션을 나누던 시절에) 만들어진
      // 계정은 세션이 여러 개 흩어져 있을 수 있다 - getAllHistory가 그 유저의 모든 세션을
      // 합쳐서 날짜순으로 돌려주므로, 세션이 몇 개든 대화가 하나로 이어져 보인다.
      const { data: history } = await chatApi.getAllHistory();
      if (history.length) {
        // 가이드 카드는 대화가 이어져도 계속 보여야 하는 진입점이라, 히스토리 앞에 항상 붙인다.
        // 날짜 구분선은 (새 세션일 때와 마찬가지로) 항상 맨 위 고정 - 히스토리 첫 메시지 날짜로
        // 구분선을 먼저 만들어 가이드 카드보다 위에 두고, buildHistoryWithDateDividers에는
        // 그 날짜를 넘겨서 같은 구분선이 가이드 카드 밑에 또 한 번 중복되지 않게 한다(2026-08-07 피드백).
        const firstDay = new Date(history[0].createdDate).toDateString();
        messages.value = [
          { id: `date-${firstDay}`, role: 'date', label: dateLabelFor(history[0].createdDate) },
          { id: 'guide', role: 'bot', time: formatBubbleTime(), ...buildGuideMessage() },
          ...buildHistoryWithDateDividers(history, firstDay),
        ];
        restorePanelFromHistory(history);
        // 마지막으로 보인 구분선은 히스토리의 마지막 메시지 날짜 - 그 이후 pushUser/pushBot이
        // 호출될 때 오늘 날짜와 달라야만(즉 날짜가 실제로 바뀌었을 때만) 새 구분선이 생기게 한다.
        lastShownDay.value = new Date(history[history.length - 1].createdDate).toDateString();
      } else {
        messages.value = buildGreetAndGuide();
        lastShownDay.value = new Date().toDateString();
      }
    }
  } catch {
    loadError.value = '챗봇을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.';
  } finally {
    const remaining = MIN_LANDING_MS - (Date.now() - landingStartedAt);
    if (remaining > 0) await wait(remaining);
    loading.value = false;
    // loading이 false가 되기 전(랜딩 화면이 떠 있는 동안)엔 .app-content가 DOM에 없어서
    // scrollToBottom을 여기서 부르기 전엔 아무 효과가 없었다 - 채팅 화면(맨 아래)이 아니라
    // 맨 위부터 보이던 문제(2026-08-11 피드백). 실제 채팅 화면이 뜬 뒤로 옮김.
    scrollToBottom();
  }
});

// App.vue에서 ChatPage를 KeepAlive로 감싸고 있어서(대화 상태 유지 목적), 다른 화면(자차 준비 등)
// 갔다가 "뒤로가기"나 이동 버튼으로 돌아오면 컴포넌트가 새로 마운트되는 게 아니라 그대로 다시
// 보여지기만 한다 - 그래서 onMounted는 다시 안 불리고, 스크롤 위치도 떠나기 전 그대로(주로 맨 위)
// 남아있었다(2026-08-11 피드백). keep-alive 전용 훅인 onActivated에서 다시 맨 아래로 내려준다.
onActivated(() => {
  scrollToBottom();
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
        <div class="chat-header__title">텅장일병구하기</div>
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

      <!-- 고정된 "오늘" 배너 없음 - 날짜 구분선은 오직 실제 메시지 데이터를 기준으로만 생긴다
           (buildGreetAndGuide/buildHistoryWithDateDividers가 role: 'date' 항목을 끼워 넣음).
           예전엔 이 자리에 항상 오늘 날짜를 고정으로 하나 더 띄웠는데, 그러면 스크롤 안의 진짜 구분선이랑
           중복으로 겹쳐 보여서(예: "8/6"이 위아래로 두 번) 뺐다. -->
      <div class="chat-page__messages">
        <template v-for="msg in messages" :key="msg.id">
          <div v-if="msg.role === 'date'" :id="msg.id" class="chat-page__date">{{ msg.label }}</div>

          <div v-else-if="msg.role === 'user'" class="bubble-row bubble-row--user">
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
                    :class="{ 'menu-btn--fit': section.compact }"
                    @click="opt.onClick"
                  >
                    {{ opt.label }}
                  </button>
                </div>
              </div>
              <div v-if="msg.tags" class="tag-row">
                <button
                  v-for="(tag, i) in msg.tags"
                  :key="i"
                  type="button"
                  class="tag-chip tag-chip--guide"
                  @click="tag.onClick"
                >
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
                <!-- 출처는 AI 생성 답변(정책문서 RAG)이든 실시간 상품·매물 조회든 둘 다 보여준다.
                     실제 상세 페이지 링크(sourceUrl)가 있으면 클릭해서 바로 넘어가게, 없으면 텍스트로만.
                     정책용어사전 출처는 의미가 없어서 visibleSource가 걸러낸다. -->
                <div v-if="visibleSource(msg)" class="answer-source">
                  출처 ·
                  <a
                    v-if="msg.sourceUrl"
                    :href="msg.sourceUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="answer-source-link"
                  >
                    {{ visibleSource(msg) }}
                  </a>
                  <template v-else>{{ visibleSource(msg) }}</template>
                </div>
                <div v-if="msg.isAiGenerated" class="answer-ai-caption">AI가 생성한 답변이에요</div>

                <!-- 되묻기(목돈 상담/목돈 모으기)는 질문의 일부라서 답변 카드 밖으로 안 빼고,
                     같은 카드 안에서 바로 고르게 한다. 색은 일단 흰 배경+검정 글씨로 되돌려두고,
                     전체 색상은 나중에 한번에 몰아서 정하기로 함(2026-08-06 피드백) -->
                <div
                  v-if="msg.menu && msg.menuInCard"
                  class="menu-col menu-col--incard"
                  :class="{ 'menu-col--grid3': msg.menuGrid3 }"
                >
                  <button
                    v-for="(opt, i) in msg.menu"
                    :key="i"
                    type="button"
                    class="menu-btn menu-btn--fit"
                    @click="opt.onClick"
                  >
                    {{ opt.label }}
                  </button>
                </div>
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

              <!-- 상품·매물·펀드처럼 항목 하나하나 내용이 길어서 세로 목록으로는 한눈에 안 들어오는 경우 -
                   가로로 넘기는 큰 카드 + 아래 점(dot)으로 몇 번째인지 보여준다(2026-08-06 피드백) -->
              <div v-if="msg.menu && msg.menuCarousel" class="menu-carousel-wrap">
                <div class="menu-carousel-track">
                  <div :id="`carousel-${msg.id}`" class="menu-carousel" @scroll="onCarouselScroll(msg, $event)">
                    <button
                      v-for="(opt, i) in msg.menu"
                      :key="i"
                      type="button"
                      class="menu-carousel-card"
                      @click="opt.onClick"
                    >
                      {{ opt.label }}
                    </button>
                  </div>
                  <!-- 스크롤/스와이프인 줄 몰라서 못 넘기겠다는 피드백(2026-08-06) - 클릭 가능한 화살표 버튼 추가 -->
                  <button
                    v-if="msg.menu.length > 1 && (carouselActive[msg.id] || 0) > 0"
                    type="button"
                    class="menu-carousel-arrow menu-carousel-arrow--prev"
                    aria-label="이전 카드"
                    @click="scrollCarouselBy(msg, -1)"
                  >
                    ‹
                  </button>
                  <button
                    v-if="msg.menu.length > 1 && (carouselActive[msg.id] || 0) < msg.menu.length - 1"
                    type="button"
                    class="menu-carousel-arrow menu-carousel-arrow--next"
                    aria-label="다음 카드"
                    @click="scrollCarouselBy(msg, 1)"
                  >
                    ›
                  </button>
                </div>
                <div v-if="msg.menu.length > 1" class="menu-carousel-dots">
                  <button
                    v-for="(opt, i) in msg.menu"
                    :key="i"
                    type="button"
                    class="menu-carousel-dot"
                    :class="{ 'menu-carousel-dot--active': (carouselActive[msg.id] || 0) === i }"
                    :aria-label="`${i + 1}번째 카드로 이동`"
                    @click="scrollCarouselToIndex(msg, i)"
                  />
                </div>
              </div>

              <!-- 카드 안(menuInCard)·캐러셀(menuCarousel)이 아닌 나머지 전부 - 되묻기든 용어 목록이든
                   연한 국방색 칩으로 통일하고, 마우스 올렸을 때만 진한 국방색으로 바뀐다(2026-08-07 피드백) -->
              <div v-if="msg.menu && !msg.menuInCard && !msg.menuCarousel" class="menu-col menu-col--fit">
                <button
                  v-for="(opt, i) in msg.menu"
                  :key="i"
                  type="button"
                  class="tag-chip"
                  :class="{ 'tag-chip--nav': msg.menuFit }"
                  @click="opt.onClick"
                >
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
          <!-- 이름을 "메인으로"가 아니라 "처음으로"로 둔 이유: 앱 홈 화면(X 버튼)과 헷갈리지 않게 -->
          <button type="button" class="pill-btn" @click="backToGuide">처음으로</button>
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

.answer-card {
  max-width: 94%;
  width: 100%;
}

/* 가이드 카드는 옆에 남는 배경 공간을 써서 최대한 넓게 - 재진입 시 스크롤바가 생기면서
   폭이 살짝 줄어들어 글자가 줄바꿈되는 문제(문구 끝 "다." 나 버튼 글씨 일부가 밑으로 내려가던 것)가 있었다 */
.guide-card {
  max-width: 100%;
  width: 100%;
}

.guide-card__sections {
  display: flex;
  gap: 8px;
  align-items: stretch;
}

/* 왼쪽/오른쪽 카드 크기가 꼭 같을 필요는 없음 - 각자 글씨 크기(내용)에 맞게 폭이 정해지게 한다 */
.guide-section {
  flex: 0 1 auto;
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
  white-space: nowrap;
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
  background: var(--military-green-light);
  border: none;
  border-radius: 14px;
  padding: 6px 11px;
  font-size: 11px;
  font-weight: 600;
  color: var(--military-green);
  cursor: pointer;
}

/* 한 칸씩 번갈아 진하게 넣었더니 오히려 산만하다는 피드백 - 기본은 연한 국방색으로 통일하고,
   마우스를 올렸을 때만 진한 배경+흰 글씨로 바뀌게 한다(2026-08-07 피드백) */
.tag-chip:hover {
  background: var(--military-green);
  color: #ffffff;
}

/* "~페이지로 이동"류 단독 확인 버튼(menuFit)은 목록형 태그보다 눈에 잘 띄어야 해서 살짝 더 크게(2026-08-07 피드백) */
.tag-chip--nav {
  padding: 8px 14px;
  font-size: 13px;
}

/* 가이드 카드 밑 태그줄(군적금 활용하기·후회소비 회고 등)도 살짝 더 크게(2026-08-07 피드백) */
.tag-chip--guide {
  padding: 7px 12px;
  font-size: 12px;
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

.answer-source-link {
  color: var(--military-green);
  text-decoration: underline;
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

/* 선택지 5개 이상(상품·용어·기능 목록 등) - 줄바꿈되는 칩 형태로 바꾼다 */
.menu-col--many {
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8px;
}

/* 카드 밖 되묻기 메뉴도 버튼 크기를 글씨에 맞추고 싶을 때(생활자금 관리 등, 2026-08-06 피드백) */
.menu-col--fit {
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8px;
}

/* menuInCard - 되묻기를 답변 카드 밖으로 안 빼고 같은 카드 안에서 바로 고르게 할 때(2026-08-06 피드백).
   답변 텍스트 바로 아래라 margin 줄이고, 버튼도 흰 테두리 박스(이중 테두리로 보임) 대신
   옅은 배경 칩으로 가볍게 처리한다 */
.menu-col--incard {
  margin-top: 10px;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8px;
}

/* 카테고리 픽커(적금/예금/청약/투자/보험/대출 6개)처럼 항목 수가 고정이고 한눈에 격자로 보여줄 때.
   flex-wrap만 쓰면 5+1처럼 마지막 줄이 어중간하게 쪼개져 보였다(2026-08-07 피드백) */
.menu-col--incard.menu-col--grid3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
}
.menu-col--incard.menu-col--grid3 .menu-btn {
  width: 100%;
  text-align: center;
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

/* 카드 안 흰 버튼도 다른 드롭다운·칩과 같은 국방색 hover 톤으로(2026-08-07 피드백) */
.menu-btn:hover {
  background: var(--military-green-light);
  border-color: var(--military-green);
  color: var(--military-green);
}

.menu-col--many .menu-btn {
  margin-top: 0;
}

/* 선택지가 많을 때만 - 되묻기(4개 이하) 카드는 기존 흰 배경 그대로 두고,
   상품·용어처럼 개수가 많은 목록만 국방색 칩으로 눈에 띄게 한다 (CategoryButton과 같은 톤) */
.menu-btn--accent {
  display: inline-block;
  width: auto;
  text-align: center;
  background: var(--military-green-light);
  border: none;
  border-radius: 999px;
  padding: 8px 14px;
  color: var(--military-green);
  font-weight: 600;
}

/* menuAlternate 전용 - 색이 없는 쪽(짝수번째) 칩. --accent와 같은 필 모양을 쓰되 흰 배경으로 번갈아 보이게 한다 */
.menu-btn--outline {
  display: inline-block;
  width: auto;
  text-align: center;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 8px 14px;
  color: var(--text-body);
  font-weight: 600;
}

.menu-btn--narrow {
  border-radius: 8px;
  padding: 7px 8px;
  font-size: 11px;
  line-height: 1.3;
  margin-top: 6px;
  white-space: nowrap;
}

.menu-btn--narrow:first-child {
  margin-top: 0;
}

/* 가로 꽉 채우는 블록 버튼 대신, 텍스트 길이만큼만 차지하게 한다(2026-08-06 피드백) */
.menu-btn--fit {
  display: inline-block;
  width: auto;
  text-align: center;
  /* 부모(menu-col--incard/--fit)의 gap이 이미 항목 사이 간격을 주고 있어서, 여기서 또 margin까지
     더하면 간격이 두 배로 벌어져 한 줄에 다 못 들어갔음 - 그래서 margin은 빼고 패딩만 살짝 줄임
     (2026-08-06 피드백 - 4개가 한 줄에 안 들어가서 "목표 정하기"만 다음 줄로 밀림) */
  padding: 8px 10px;
  font-size: 12.5px;
  margin-top: 0;
}

/* 상품·매물·펀드 목록 - 가로로 넘기는 큰 카드 (2026-08-06 피드백) */
.menu-carousel-wrap {
  margin-top: 10px;
}

/* 화살표 버튼을 카드 위에 절대위치로 띄우기 위한 기준 컨테이너 */
.menu-carousel-track {
  position: relative;
}

.menu-carousel {
  display: flex;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  gap: 10px;
  padding-bottom: 2px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.menu-carousel::-webkit-scrollbar {
  display: none;
}

/* 스크롤/스와이프인 걸 몰라도 누르면 넘어가는 게 보이도록 - 마우스로 테스트할 때를 위한 화살표(2026-08-06 피드백) */
.menu-carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
  color: var(--military-green);
  font-size: 16px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.menu-carousel-arrow--prev {
  left: -6px;
}

.menu-carousel-arrow--next {
  right: -6px;
}

.menu-carousel-card {
  /* 카드 2장이 딱 맞아떨어지면 책장 넘기듯 딱딱해 보인다는 피드백 - 폭을 줄여서 2.5장 정도
     걸치게(끝에 다음 카드가 살짝 보여야 "이어지는 슬라이드" 느낌이 남) 하고, 위아래 여백도 줄임.
     폭 130px는 스크립트의 CAROUSEL_CARD_STEP(130+gap10=140)과 맞춰져 있어서,
     여기 숫자 바꾸면 그쪽도 같이 바꿔야 함(2026-08-06) */
  scroll-snap-align: start;
  flex: 0 0 130px;
  width: 130px;
  min-height: 92px;
  display: flex;
  align-items: center;
  /* 연한 국방색이 기본, 마우스 올렸을 때만 진하게(2026-08-07 피드백) */
  background: var(--military-green-light);
  border: none;
  border-radius: 14px;
  padding: 12px;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--military-green);
  text-align: left;
  white-space: normal;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(180, 150, 80, 0.08);
}

.menu-carousel-card:hover {
  background: var(--military-green);
  color: #ffffff;
}

.menu-carousel-dots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 5px;
  margin-top: 8px;
}

.menu-carousel-dot {
  /* 전역 box-sizing: border-box라 width에 padding까지 포함됨 - 점(6px)보다 padding을 크게 주면
     실제 보이는 면적이 0이 되어(음수라 0으로 잘림) 안 보이는 버그가 있었음(2026-08-06) */
  width: 6px;
  height: 6px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background-color: var(--line);
  cursor: pointer;
  transition: all 0.15s ease;
}

.menu-carousel-dot--active {
  width: 16px;
  border-radius: 3px;
  background-color: var(--military-green);
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
  /* max-width를 프레임 폭(393px)보다 살짝 줄여 .app-content의 세로 스크롤바(15px)를 안 덮게 함 -
     그대로 393px면 이 fixed 패널이 스크롤바 자리까지 배경으로 덮어버려서, 채팅이 다 안 보이는
     시점에도 스크롤바가 안 보이거나 클릭이 안 먹혔다(2026-08-11 피드백) */
  transform: translateX(calc(-50% - 11.5px));
  width: 100%;
  max-width: 370px;
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
  /* .chat-page__panel과 동일한 이유로 스크롤바(15px) 자리를 남겨둠(2026-08-11 피드백) */
  transform: translateX(calc(-50% - 11.5px));
  display: flex;
  gap: 8px;
  width: 100%;
  max-width: 370px;
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
