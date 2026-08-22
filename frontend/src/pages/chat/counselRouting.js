// 자유입력 텍스트로 "목돈 상담" 되묻기를 어디로 보낼지 판단하는 순수 함수들만 모아둔 모듈.
// ChatPage.vue에서 분리한 이유: 겹치는 키워드(예: "적금"이 "군적금"에도 포함)가 계속 늘어나는
// 영역이라, 하나 고칠 때 다른 문구가 조용히 깨지기 쉽다(2026-08-12 발견 - "적금" 추가로
// "군적금으로 뭐하지?"가 상담 되묻기 대신 상품 목록으로 잘못 튄 사례). Vue 컴포넌트를 마운트하지
// 않고도 대표 문구들의 라우팅 결과를 자동 테스트(counselRouting.test.js)로 고정해두기 위해
// 순수 함수만 따로 뺐다 - 이 파일을 고칠 땐 반드시 그 테스트를 같이 돌려서 회귀를 확인할 것.

/* 상담 되묻기 1단계 - 목적 선택지 (value는 이후 분기 흐름 판별용) */
export const COUNSEL_GOALS = [
  { label: '목돈 모으기', value: 'savings' },
  { label: '내 집 마련(청약)', value: 'housing' },
  { label: '투자 수익', value: 'investment' },
  { label: '생활자금 관리', value: 'spending' },
];

/* 자유입력에 목적이 이미 드러나 있으면(예: "투자해보고싶어") 목적을 다시 묻지 않고
   바로 해당 목적의 되묻기로 들어간다. 애매하면(매칭 없음) 그대로 목적부터 물어본다. */
export const COUNSEL_GOAL_KEYWORDS = [
  // savings('목돈 모으기')만 빠져있어서 "적금 추천해줘"처럼 목적이 뻔한 질문도 되묻기 없이 바로
  // 못 들어가고, 매번 목적부터 다시 물어보는 4지선다 카드가 떴다(2026-08-11 피드백) - "투자해보고
  // 싶어"가 investment로 바로 들어가는 것과 동일하게 맞춤
  { value: 'savings', keywords: ['적금', '예금', '저축'] },
  { value: 'investment', keywords: ['투자'] },
  { value: 'housing', keywords: ['청약', '내 집', '집 마련', '전세', '매매'] },
  { value: 'spending', keywords: ['생활비', '소비', '용돈'] },
];

export const detectCounselGoal = (text) => {
  // detectTentativeListCategory와 같은 함정: find()로 첫 매칭만 보면 "적금이랑 투자 중에 뭐가
  // 나아?"처럼 서로 다른 목적이 한 문장에 같이 언급됐을 때 뒤쪽(투자)이 무시된 채 앞쪽(적금→savings)
  // 으로 확정돼버린다(2026-08-21 발견, 같은 파일의 detectTentativeListCategory 버그와 동일 패턴).
  // 매칭되는 목적이 2개 이상이면 확정하지 않고 null을 반환해 startCounsel의 4지선다로 넘긴다.
  const found = COUNSEL_GOAL_KEYWORDS.filter((g) => g.keywords.some((k) => text.includes(k)));
  return found.length === 1 ? COUNSEL_GOALS.find((g) => g.value === found[0].value) : null;
};

// "적금 추천해줘"처럼 상품 종류(적금/예금)까지 이미 콕 집어 말한 경우엔, 투자와 달리 성향·기간을
// 더 물어볼 이유가 없다 - 그냥 실시간 상품 목록(카테고리 피커에서 적금 눌렀을 때와 동일)을 바로
// 보여준다. 투자는 위험성향에 따라 추천이 갈리니까 계속 되묻기(투자 수익 흐름)로 남겨둔다(2026-08-11 피드백).
export const COUNSEL_DIRECT_LIST_KEYWORDS = [
  { category: 'savings', label: '적금', keywords: ['적금'] },
  { category: 'deposit', label: '예금', keywords: ['예금'] },
];
// "적금" 글자만 있으면 걸리게 했더니 "군적금으로 뭐하지?"처럼 "적금"이 단어 일부로만 들어간
// 열린 질문까지 상담 되묻기를 건너뛰고 바로 상품 목록으로 튀어버렸다(2026-08-12 발견) - 이런
// 질문은 오히려 되묻기(적금/예금/투자/목표 정하기)로 들어가는 게 맞다. "적금 추천해줘"처럼
// 추천/목록 의도가 명확한 문장에만 바이패스가 걸리도록 좁힌다.
export const RECOMMEND_INTENT_KEYWORDS = ['추천', '보여줘', '알려줘', '뭐있어', '뭐 있어', '목록', '리스트'];
export const detectDirectListCategory = (text) => {
  if (!RECOMMEND_INTENT_KEYWORDS.some((k) => text.includes(k))) return null;
  return COUNSEL_DIRECT_LIST_KEYWORDS.find((g) => g.keywords.some((k) => text.includes(k)));
};

// "아 그냥 적금 들까"처럼 추천 의도 단어 없이 애매하게 적금/예금만 언급된 문장은 지금까지
// 목적 되묻기(적금/예금/투자/목표 정하기 4지선다)로 빠졌는데, 이미 상품 종류를 말한 사람한테
// 그 종류를 또 고르게 하는 건 불필요하다(2026-08-13 피드백) - "OO 상품을 보여드릴까요?" 확인
// 한 번만 거쳐 목록으로 보낼 후보를 여기서 찾는다. detectDirectListCategory와 달리 추천 의도
// 단어가 없어도 매칭되는 대신, 곧바로 목록행이 아니라 확인 절차를 한 단계 거치게 해서 안전판을 둔다.
// 단, text.includes만 쓰면 "군적금"의 "적금"까지 잡혀서 "군적금으로 뭐하지?"(2026-08-12에 발견된
// 바로 그 함정)도 걸려버린다 - 이 문장은 상품을 보여달라는 게 아니라 활용법을 묻는 거라 되묻기가
// 맞으므로, 앞이 공백이거나 문장 시작인 "단어로서의" 적금/예금만 잡히게 단어 경계를 둔다.
const _isStandaloneWordIn = (text, word) => new RegExp(`(^|\\s)${word}`).test(text);
// "적금이랑 예금 중에 뭐가 좋아?"처럼 두 종류가 한 문장에 같이 언급되면, 이건 "적금 들까"류의
// 단일 종류 확인이 아니라 비교 질문이다 - 첫 매칭(find)만 보고 그 종류로 확정해버리면 예금은
// 무시된 채 "적금 상품을 보여드릴까요?"로 잘못 새버린다(2026-08-21 발견). 매칭이 2개 이상이면
// 여기서 확정하지 않고 null을 반환해 원래 흐름(4지선다/자유입력)으로 넘긴다.
export const detectTentativeListCategory = (text) => {
  const matches = COUNSEL_DIRECT_LIST_KEYWORDS.filter((g) => g.keywords.some((k) => _isStandaloneWordIn(text, k)));
  return matches.length === 1 ? matches[0] : null;
};
