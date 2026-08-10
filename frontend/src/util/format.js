// 화면 표기 공통 포맷 함수. 금액·날짜 표기는 이 파일 하나로 통일한다.
// (각 화면에 toLocaleString 을 복붙하지 말 것: 바꿀 때 한 곳만 고치도록)

// ── 금액 ──────────────────────────────────────────────────────
// 원 단위 + 3자리 콤마 + "원"  →  formatWon(1200000) === "1,200,000원"
export const formatWon = (amount) =>
  `${(amount ?? 0).toLocaleString('ko-KR')}원`;

// 만원 단위 + "만원"  →  formatManwon(1200000) === "120만원"
// 금액이 커서 만원 단위가 자연스러운 화면에서 사용 (한 화면 안에선 한 단위로 통일).
// 입력값은 "원" 단위여야 한다 (10000으로 나눔).
export const formatManwon = (amount) =>
  `${Math.round((amount ?? 0) / 10000).toLocaleString('ko-KR')}만원`;

// 이미 "만원" 단위로 내려오는 금액 표기용  →  formatManwonUnit(1200) === "1,200만원"
// car 도메인처럼 백엔드 DTO가 만원 단위 정수를 그대로 내려주는 화면에서 사용 (formatManwon과 달리 나누지 않음).
export const formatManwonUnit = (amount) =>
  `${(amount ?? 0).toLocaleString('ko-KR')}만원`;

// 딱 떨어지는 금액을 "1천원"·"30만원"·"1백만원"처럼 가장 자연스러운 단위로 축약 표기.
// 상품 납입한도(minLimit/maxLimit, 실제로는 1천~300만원대)처럼 천원 단위 이상으로 딱 떨어지는 금액에 쓴다.
// 10,000원 이상이면 사실상 formatManwon과 같은 결과라 그대로 재사용하고(중복 방지),
// formatManwon이 다루지 못하는 두 경우만 이 함수가 따로 처리한다:
//   ① 10,000원 미만(예: 1,000원) — formatManwon은 반올림으로 "0만원"이 돼버려서 못 씀
//   ② 100만~900만원 — "100만원" 대신 "1백만원" 스타일을 우선하고 싶을 때
// formatKoreanWon(1000) === "1천원" / formatKoreanWon(300000) === "30만원"(=formatManwon과 동일)
// formatKoreanWon(1000000) === "1백만원"
export const formatKoreanWon = (amount) => {
  const value = amount ?? 0;
  if (value === 0) return '0원';
  if (value < 10000 && value % 1000 === 0) {
    return `${value / 1000}천원`;
  }
  if (value % 1000000 === 0 && value / 1000000 < 10) {
    return `${value / 1000000}백만원`;
  }
  if (value % 10000 === 0) {
    return formatManwon(value);
  }
  return formatWon(value);
};

// ── 금액 입력창 (실시간 콤마 포맷) ──────────────────────────────
// 타이핑 중 표시용: 숫자 아닌 문자 제거 + 3자리 콤마  →  formatAmountInput('1200000') === "1,200,000"
// input type="text"에 v-model로 바로 물릴 표시값을 만들 때 사용 (숫자/커서 관리는 BaseInput의 amount 타입이 담당).
export const formatAmountInput = (value) => {
  const digitsOnly = String(value ?? '').replace(/[^0-9]/g, '');
  if (!digitsOnly) return '';
  return Number(digitsOnly).toLocaleString('ko-KR');
};

// 저장/전송용: 콤마 섞인 표시값 → 순수 숫자  →  parseAmountInput('1,200,000') === 1200000
export const parseAmountInput = (value) => {
  const digitsOnly = String(value ?? '').replace(/[^0-9]/g, '');
  return digitsOnly ? Number(digitsOnly) : 0;
};

// ── 날짜 ──────────────────────────────────────────────────────
// 화면 표시용: 점 구분  →  formatDate('2026-07-23') === "2026.07.23"
// (데이터/전송은 ISO 'YYYY-MM-DD' 그대로 두고, 보여줄 때만 이걸로 변환)
export const formatDate = (value) => {
  if (!value) return '';
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}.${month}.${day}`;
};

// 데이터 저장/전송용: ISO 'YYYY-MM-DD'  →  toIsoDate(new Date()) === "2026-07-28"
export const toIsoDate = (value) => {
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 날짜 그룹 헤더용(연도 없이): formatMonthDay('2026-08-06') === "8월 6일"
export const formatMonthDay = (value) => {
  if (!value) return '';
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return `${date.getMonth() + 1}월 ${date.getDate()}일`;
};

// 시:분만 표시: formatTime('2026-08-06T14:44:00') === "14:44"
export const formatTime = (value) => {
  if (!value) return '';
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
};
