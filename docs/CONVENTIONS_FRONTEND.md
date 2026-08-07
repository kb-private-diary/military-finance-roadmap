# 📗 개발 컨벤션: 프론트엔드 (텅장일병구하기)

30반 4팀 · KB IT's Your Life 7기 · Vue 3 + Vite

> 이 문서 = **프론트엔드 컨벤션**. 백엔드는 [`CONVENTIONS_BACKEND.md`](CONVENTIONS_BACKEND.md) 참고.
> §0(공통 규칙)·§2(커밋 컨벤션)는 양쪽 문서 공통. §1(브랜치)·§3(PR) 등 **Git 협업 절차는 [`GIT_WORKFLOW.md`](GIT_WORKFLOW.md)** 참고. §번호는 기존 참조 유지를 위해 그대로 둡니다.
> 변경 시 이 문서를 먼저 고치고 슬랙에 공유해주세요.

---

## 0. 📄 공통 규칙

- **인코딩**: UTF-8
- **줄바꿈**: LF
- **파일 끝**: 새줄로 종료
- **줄 끝 공백**: 제거
- 한글 발음 로마자 표기 금지 (예: `gaeguri` ❌)
- 의미 없는 한 글자 변수명 금지 (반복문 카운터 제외)

---

## 1. 🌳 Git 브랜치 전략

> 브랜치 전략·작업 순서·충돌·Stash 등 협업 절차는 [`GIT_WORKFLOW.md`](GIT_WORKFLOW.md) 참고.

**구조**: 개인 브랜치 → `dev` → `main` (3단계). `dev`·`main` 은 **PR로만 병합**(직접 push ❌).
**개인 브랜치**: `dev_seokyun` · `dev_taeseok` · `dev_hobin` · `dev_jiwon` · `dev_suyeon` · `dev_eseudeo`

---

## 2. ✅ 커밋 컨벤션

**Header**: `이모지[type] 설명 #이슈번호` / **Body**(선택): `-` bullet

| 이모지 | type         | 설명                                      |
| ------ | ------------ | ----------------------------------------- |
| ❇️     | `[feat]`     | 새로운 기능 추가                          |
| 🐞     | `[fix]`      | 버그 수정                                 |
| 💄     | `[style]`    | UI/Style 추가 및 업데이트                 |
| 🔨     | `[refactor]` | 리팩토링                                  |
| 🔧     | `[chore]`    | 잡일 (그 외 자잘한 수정)                  |
| 📝     | `[docs]`     | 문서 생성/업데이트                        |
| 🔍     | `[test]`     | 테스트                                    |
| 🚧     | `[setting]`  | ESLint / Dependency / Config 등 빌드·설정 |

```
💄[style] 자취 목표 등록 화면 구현 #21
🚧[setting] vite 프록시에 챗봇(/api/chat) 추가 #5
```

- `#이슈번호` 는 GitHub Issues 를 쓸 때만 / `Closes #12` 넣으면 병합 시 이슈 자동 종료
- `chore` vs `setting`: 의존성·빌드 설정 = **setting**, 그 외 잡일 = **chore**
- 프론트 화면 구현은 보통 `💄[style]` 또는 `❇️[feat]` (동작 로직 있으면 feat)

---

## 3. 🔀 Pull Request 규칙

> PR 생성·리뷰·병합(승인 인원 등) 절차는 [`GIT_WORKFLOW.md`](GIT_WORKFLOW.md) 참고.

- PR 제목은 커밋 컨벤션(§2)과 동일 양식
- 본문은 PR 템플릿(작업 내용 / 변경점 / 테스트) 채우기

---

## 5. 🎨 프론트엔드 컨벤션 (Vue 3 + Vite)

### 폴더 구조

```
src/
├── pages/{도메인}/XxxPage.vue     # 화면
├── router/{도메인}.js             # 라우트 → router/index.js 등록
├── api/{도메인}Api.js             # axios 호출 (공통 인스턴스 api/index.js)
├── stores/{도메인}.js             # Pinia 상태 (인증은 stores/auth.js)
├── components/
│   ├── common/                    # 재사용 공통 컴포넌트 (BaseCard 등)
│   └── layouts/                   # 공통 레이아웃 (AppLayout·AppHeader·AppTabNav)
├── composables/                   # 재사용 로직 (use- 접두사)
├── assets/                        # colors.css(색상 토큰)·typography.css
└── config/index.js                # 메뉴/타이틀
```

### 라우트 도메인 (14개) · 화면 54개

`member`(10) · `rent`(7) · `travel`(6) · `car`(5) · `job`(5) · `regret`(5) · `main`(3) · `openbanking`(3) · `dashboard`(3) · `simulator`(2) · `product`(2) · `social`(1) · `chat`(1) · `push`(1)

> 백엔드(15개)와 다른 이유: `bookmark`·`roadmap`·`saving` 은 **화면 없이 API만**, `chat` 은 **프론트 화면 + FastAPI 서버**

### 컴포넌트 순서

`<script setup>` → `<template>` → `<style scoped>` **순서 고정**

### 네이밍

- **컴포넌트/페이지**: PascalCase (`TravelGoalCreatePage.vue`)
- **변수/함수**: camelCase
- **컴포넌트명**: 두 단어 이상 (`Todo` ❌ / `TodoItem` ⭕), 접두사 규칙 없음
- **커스텀 이벤트**: kebab-case (`@user-updated`)

### 컴포저블

- 반복 로직은 `composables/` 에 분리, 이름 앞에 **`use-` 접두사** 필수 (`useJobGoal`)

### Props

- JS는 camelCase / 템플릿은 kebab-case / **타입·필수여부·(선택 prop이면) 기본값 명시**
- **`required: true` 와 `default` 는 같이 쓰지 않는다**: 필수면 항상 넘어오니 `default` 는 절대 안 쓰임(모순). → 필수 prop = `required: true`(default 생략) / 선택 prop = `default` 지정(required 생략)

```js
defineProps({
  goalId: { type: Number, required: true }, // 필수 → default 없음
  editable: { type: Boolean, default: false }, // 선택 → default 지정
});
```

### 템플릿 규칙

- `v-for` 엔 항상 `key` 지정
- `v-if` 와 `v-for` 동시 사용 금지 (필터링은 computed)
- 내용 없는 태그는 자체 닫기 (`<CommonInput />`)
- 함수는 화살표 함수로 선언
- 스타일은 `scoped` 또는 CSS Modules로 범위 제한

### 라우트 컨벤션 ⭐ Name 방식으로 통일

```js
// 정의
{ path: '/rent/goals/:goalId', name: 'RentGoalDetail', meta: { requiresAuth: true }, component: ... }
// 이동 (path 문자열 조합 ❌)
router.push({ name: 'RentGoalDetail', params: { goalId } });
```

**Name 규칙**: `[도메인][페이지역할]` PascalCase: `RentGoalCreate` / `RentListingDetail` / `RegretDashboard` / `Home` / `Login`

### 디자인 시스템 (공통 컴포넌트·토큰 재활용) ⭐

- **색상 토큰**: 모든 색은 `assets/colors.css` 의 CSS 변수만 참조 (하드코딩 색 ❌). 색을 바꿀 땐 이 파일만 고친다. 컨셉 = **KB 옐로우 + 군인(국방색·브라운)**:

  | 토큰                          | HEX                 | 용도                                                          |
  | ----------------------------- | ------------------- | ------------------------------------------------------------- |
  | `--kb-yellow`                 | `#ffcc00`           | **주 색상**: CTA·완료 버튼 (Positive가 진해 Negative를 메인으로) |
  | `--kb-yellow-deep`            | `#ffbc00`           | 강한 강조에만 소량 (KB Yellow Positive)                       |
  | `--kb-gray` / `--kb-dark-gray`| `#60584c` / `#545045` | 브라운 계열: 본문 텍스트·보조 포인트                          |
  | 국방색                        | `#536349`           | 군인 컨셉 포인트: D-Day 카드 배경·`BaseTag`(활성)             |

  **색 사용 규칙**

  - **모든 색은 토큰으로**: 화면에서 새 색이 필요하면 그때그때 하드코딩하지 말고 `colors.css` 에 토큰으로 올린 뒤 `var(--토큰)` 으로 쓴다 (상태색 성공·에러, 골드 등도 마찬가지).
  - **폴백 금지**: `var(--토큰, #폴백)` 처럼 폴백 붙이지 않기. `colors.css` 가 항상 로드돼 폴백은 안 쓰이고, 색 바꿀 때 두 군데를 고치게 된다.
  - ⚠️ 지금은 국방색 `#536349`·상태색·폴백이 `DashboardPage.vue`·`BaseTag.vue` 등에 **하드코딩** 상태 (아래 [정리 대상] 표 참고).
- **프레임**: 미니앱 특성상 393px 모바일 프레임(`AppLayout`): 헤더·탭바 공통
- **재사용 공통 컴포넌트**: `BaseCard` · `BaseModal` · `BaseBottomSheet` · `BaseInput` · `CategoryButton` · `DonutChart` · `ProgressBar` · `RoadmapCharacterSlider` · `EmptyState` · `LikeButton` · `BottomButtonBar` 등 → **새로 만들기 전에 있는 것 먼저 확인**
- 탭바 노출은 라우트 `meta.showTabNav`/`requiresAuth` 로 제어 (`AppLayout` 주석 참고)

### 인증(JWT) 흐름

1. `POST /api/users/login` → JWT 발급
2. `stores/auth.js` 가 토큰을 `localStorage` 저장
3. `api/index.js` 인터셉터가 `Authorization: Bearer {token}` 자동 첨부
4. 401 시 자동 로그아웃 → 로그인 페이지 이동

**인증 필요 화면**은 `meta: { requiresAuth: true }` 만 붙이면 됨 → `router/index.js` 전역 가드가 자동 처리 (비로그인 시 `Login` 이동 + `redirect` 쿼리 보존)

### API 호출

- 공통 axios 인스턴스 `api/index.js` 사용 (`/api` → Spring :8080 프록시)
- 도메인별 호출은 `api/{도메인}Api.js` 로 분리
- 백엔드 응답은 항상 **5필드** `{ success, data, message, code, timestamp }` (`common/response/ApiResponse.java`). 성공 시 `message`·`code` 는 `null`, 실패 시 `data` 는 `null`·`code` 에 에러코드. 화면은 보통 `data` 만 꺼내 쓰고, 실패 분기는 `success`/`code` 로 판단
- 컴포넌트에서 `axios` 를 직접 import·호출 ❌: 반드시 도메인 api 파일(→ 공통 인스턴스) 경유 (JWT 자동 첨부·에러 공통 처리가 걸린다)

### 🍍 Pinia 스토어 패턴

- 스토어는 **setup 문법**으로 통일: `defineStore('rent', () => { ... })` (options 문법 ❌)
- 안에서 `ref`(상태)·`computed`(파생)·함수(액션) 선언 후 **필요한 것만 `return`**
- 서버 왕복이 필요한 액션은 스토어 안에서 `api/{도메인}Api.js` 호출
- **여러 화면에 걸친 상태**(위저드 입력·세션 등)는 스토어, **한 화면 안에서만** 쓰는 값은 컴포넌트 `ref`
- 초기화가 필요하면 `reset()` 제공 (참고: `stores/auth.js`·`stores/rent.js`·`stores/signup.js`)

### 🔌 환경설정 (API 주소·env)

- API 서버 주소 **하드코딩 금지**: dev 는 `vite.config.js` 프록시 사용(§7), 환경별 값은 `.env` + `import.meta.env.VITE_*`
- axios 는 **공통 인스턴스(`api/index.js`)만** 사용: baseURL·타임아웃·인터셉터가 거기 한 곳에

### 💰 금액·숫자 표기

거의 모든 도메인이 금액을 화면에 보여주는데 지금은 화면마다 표기가 달라(원 vs 만원, 복붙) 통일감이 떨어진다. 아래로 통일한다.

- **표시 기본 = "원" 단위 + 3자리 콤마 + "원"** → `1,200,000원`
- 금액이 커서 만원 단위가 자연스러운 화면(시뮬레이터·목표금액 등)은 **만원 단위** 허용: 단 **한 화면 안에서는 한 단위로 통일**
- 표기 로직은 화면에 복붙하지 말고 **`util/format.js` 공통 함수**를 쓴다 (바꿀 때 한 곳만)

```js
import { formatWon, formatManwon } from '@/util/format';
formatWon(1200000); //   "1,200,000원"
formatManwon(1200000); // "120만원"
```

- **입력(`BaseInput`)**: 원 단위 숫자로 받고 `suffix="원"`(또는 만원) 명시: 표시할 때만 위 함수로 포맷

> 📌 폴더는 `util/`(단수, 기존 `util/guards.js` 와 통일), 함수명은 `formatWon`/`formatManwon` 로 통일.
> (팀 제안 `utils/formatAmount` 은 이걸로 수렴 · 널가드 `?? 0` 포함본 사용)

### 📅 날짜 표기

- **데이터·API 전송 = ISO `YYYY-MM-DD`** (백엔드 `LocalDate` 와 동일)
- **화면 표시 = `YYYY.MM.DD`** (점 구분): `util/format.js` 의 `formatDate()` 사용
- D-Day·"n일 전" 처럼 상대 표기가 자연스러운 곳은 전용 헬퍼 사용
- 기존에 하이픈으로 보여주던 화면은 **그 화면을 다시 만질 때** util 로 교체 (일괄 강제 아님)

```js
import { formatDate } from '@/util/format';
formatDate('2026-07-23'); // "2026.07.23"
```

### ⚠️ API 에러 처리

에러 처리는 **2층**이다. 백엔드가 전역 `ApiExceptionAdvice` 에서 모든 예외를 공통 처리하듯,
프론트는 **`api/index.js` 응답 인터셉터**가 같은 자리다 (모든 응답이 반드시 지나는 길목).

**① 전역 (인터셉터 `api/index.js`)**: 어느 화면이든 똑같이 반응할 것. 한 번 짜두면 전 화면 공통 적용.

- `401`(인증 만료) → 자동 로그아웃 + 로그인 이동 _(이미 처리됨)_
- 네트워크 끊김 · 타임아웃 · `5xx` → 공통 토스트(`"잠시 후 다시 시도해주세요"`)

**② 화면별 (컴포넌트 `try/catch`)**: 그 화면 맥락에서만 의미 있는 것.

- 입력값 검증(예측 가능) → **화면 인라인** (`BaseInput` 의 `error` prop, `alert()` 지양)
- 비즈니스 에러(`success:false`, 예: 404 없음 · 409 중복) → 화면 맥락에 맞게(인라인/토스트)

- **모든 API 호출은 `try/catch/finally`** 로 감싼다 (성공·실패·로딩종료 명확히)
- 사용자 알림은 브라우저 `alert()` 대신 **공통 토스트**(`useToast`)로 통일

> 🛠 **토스트**: 라이브러리 없이 자체 구현 ✅ `components/common/BaseToast.vue` + `composables/useToast.js`.
> `App.vue` 에 `<BaseToast />` 마운트 완료 ✅ 어디서든 `useToast().show('...')` 로 띄운다.
>
> ```js
> import { useToast } from '@/composables/useToast';
> const { show } = useToast();
> show('저장했어요', 'success'); // type: 'info'(기본)|'success'|'error'
> ```
>
> 전역 토스트(①) 인터셉터 연결도 완료 ✅ (네트워크 끊김·`5xx` 시 자동 노출).

### ⏳ 로딩 상태 표시

- 화면 진입 데이터 로딩 = **`loading` ref + `try/finally`** 패턴으로 관리
- 표시는 **공통 인디케이터로 통일**: 카드/리스트가 여러 개면 **스켈레톤** 권장, 단순 화면은 **스피너/`불러오는 중…`**
- **액션 중**(제출·저장)엔 버튼을 비활성(`BottomButtonBar` 의 `primary-disabled`)해 중복 클릭 방지

```js
const loading = ref(false);
const load = async () => {
  loading.value = true;
  try {
    list.value = await rentApi.findBoardList();
  } finally {
    loading.value = false; // 성공·실패 상관없이 항상 끔
  }
};
```

### 🧮 계산 위치

- 간단한 사칙연산·합계·비율(총 비용·월 환산 등)은 **프론트 `computed`** 에서 처리
- **계산만을 위한 별도 백엔드 요청 금지**: 이미 받은 데이터로 화면에서 계산
- 단, 이자·정책·금액 검증처럼 **규칙이 복잡하거나 정확성이 중요한 계산은 백엔드**에서 (프론트는 표시만)

### 🫙 빈 상태(Empty) 처리

- 리스트·목록이 **0건이면 빈 상태 UI 필수**: 공통 `EmptyState` 컴포넌트 사용
- 빈 상태엔 **다음 행동 CTA** 를 함께 (예: 매물 없음 → "조건 바꾸기" 버튼)
- **로딩 중 / 에러 / 빈 상태**를 구분해 표시 (로딩과 빈 상태를 헷갈리게 하지 않기)

> 🛠 `EmptyState` 는 `title`·`description`·`#icon` + **`#action` 슬롯**(CTA) 지원 ✅. CTA 버튼은 이 슬롯에 넣는다.

### 🧹 현재 코드에 남은 정리 대상 (착수 시 함께 교체)

컨벤션 확정 전에 짠 코드라 아래가 남아있다. **일괄 강제 아님**: 해당 화면·컴포넌트를 손댈 때 같이 정리한다. **새 화면은 처음부터 규칙대로** (정리 대상 늘리지 않기).

| 항목                      | 위치(예)                                                                            | 바꿀 방향                                       |
| ------------------------- | ----------------------------------------------------------------------------------- | ----------------------------------------------- |
| `alert()`                 | `MyPage.vue` · `MyPagePasswordPage.vue` · `TermsPage.vue`                            | 공통 토스트 `useToast().show()`                 |
| 금액 포맷 자체 정의       | `DashboardPage.vue` · `SimulatorPage.vue` · `TravelCostPage.vue` · `SavingsBreakdown.vue` | `util/format.js` 의 `formatWon` / `formatManwon` |
| 날짜 수동 조립            | `TravelGoalCreatePage.vue` · `SignupMilitaryPage.vue`                                | `util/format.js` 의 `formatDate` / ISO 헬퍼      |
| 색 폴백 · 오프팔레트 색   | `DashboardPage.vue` 등 다수                                                          | `var(토큰)` 로, 없는 색은 `colors.css` 에 토큰 먼저 추가 |
| `console.log` 잔재        | 5곳                                                                                 | 제거 (디버그 로그 커밋 금지)                    |

---

### Prettier 설정

```json
{ "semi": true, "tabWidth": 2, "bracketSpacing": true, "trailingComma": "all", "arrowParens": "always", "singleQuote": true }
```

`frontend/.prettierrc` ✅: VSCode Prettier 확장 켜고 "저장 시 포맷" 사용 (불필요한 diff·충돌 방지)

### 🔍 프론트 테스트 (선택)

- 현재 테스트 라이브러리 **미설치**. **필수 아님**: 일정 여유 시 도입.
- 도입 시 **Vitest** 사용, 대상은 **순수 로직 우선**(`util/`·`stores/`·`composables/`). 컴포넌트 렌더 테스트는 여력 될 때.
- 파일명: 대상 옆에 **`*.spec.js`** (예: `format.spec.js`)
- 설치(도입 시): `npm i -D vitest @vue/test-utils jsdom` + `package.json` 에 `"test": "vitest"`

> 팀 논의 필요: 테스트 도입(=Vitest 의존성 추가) 여부는 팀 합의 후 결정. 위는 도입 시 표준안.

---

## 7. 🤖 챗봇(FastAPI) 연동: B안

- 프론트는 챗봇만 **FastAPI(:8000)** 로 직접 호출, 그 외는 **Spring(:8080)**
- `vite.config.js` 프록시: `/api/chat → :8000`, `/api → :8080`
- 두 서버가 **동일 JWT(HS256)** 검증 → secret key 공유
- `chat_session` / `chat_message` / `chat_feedback` 테이블은 **FastAPI 소유**
- 상세: [chatbot/README.md](chatbot/README.md)

---

## 8. 📝 문서 관리

| 문서                   | 내용                                               |
| ---------------------- | -------------------------------------------------- |
| 화면목록·라우트 정의서 | 노션 DB (화면 ID·경로·담당자·도메인·라우트명·인증) |
| API 명세서             | 노션 DB (기능명·API ID·Method·URL·담당자·상태)     |
| WBS                    | 노션 DB (작업명·유형·우선순위·상태·기간)           |

- 컨벤션 변경 시 관련 문서에 동시 반영 / 남의 파트 화면·컴포넌트 사용은 담당자와 협의 / 변경 이력 슬랙 공유

---

## ✅ 프론트 착수 체크리스트

- [x] `frontend/.prettierrc` 생성 (§5 Prettier 설정)
- [x] `frontend/src/composables/` 폴더 생성 (§5 폴더 구조)
- [ ] VSCode **Prettier 확장** 설치 및 "저장 시 포맷" 켜기
- [ ] 새 컴포넌트 만들기 전 `components/common/` 에 비슷한 것 있는지 확인
