# 📗 개발 컨벤션 — 텅장일병구하기

30반 4팀 · KB IT's Your Life 7기 · 최종 갱신 2026.07.22

> 팀 합의 컨벤션 최종본입니다. 변경 시 이 문서를 먼저 고치고 슬랙에 공유해주세요.

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

**개인 브랜치 → dev → main** (3단계)

| 브랜치      | 역할                            | 직접 push |
| ----------- | ------------------------------- | --------- |
| `main`      | 배포용. 완성·검증된 것만        | ❌ (PR만) |
| `dev`       | 통합/테스트용. 각자 기능을 모음 | ❌ (PR만) |
| 개인 브랜치 | 각자 개발 공간                  | ⭕ 자유   |

**개인 브랜치 목록** (생성 완료)
`dev_seokyun` · `dev_taeseok` · `dev_hobin` · `dev_jiwon` · `dev_suyeon` · `dev_eseudeo`

**흐름**: 개인 브랜치에서 개발 → `개인 → dev` PR(리뷰 후 병합) → 배포 전 `dev → main` PR

**규칙**

- `dev` / `main` 은 PR로만 병합, 리뷰 1명 이상 승인
- 개인 브랜치 작업 전/중 `dev` 를 자주 pull 해서 충돌 예방
- Git 클라이언트는 자유 (Fork / CLI / VSCode 등)

---

## 2. ✅ 커밋 컨벤션

**Header**: `이모지[type] 설명 #이슈번호`
**Body** (선택): 상세 내용을 `-` bullet 로

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
❇️[feat] 여행 목표 등록 API #12

- travel domain VO/Mapper 작성
- travel service 등록 로직 구현
```

```
🐞[fix] 로그인 시 토큰 만료 처리 오류 #23
🚧[setting] vite 프록시에 챗봇(/api/chat) 추가 #5
```

💡 **팁**

- `#이슈번호` 는 GitHub Issues 를 쓸 때만 붙이기
- `Closes #12` 를 넣으면 병합 시 이슈 자동 종료
- `chore` vs `setting` 기준: 의존성·빌드 설정 = **setting**, 그 외 잡일 = **chore**

---

## 3. 🔀 Pull Request 규칙

- 대상: `개인 → dev`, 배포 전 `dev → main`
- 리뷰어 1명 이상 승인 후 병합
- PR 제목은 커밋 컨벤션과 동일 양식 권장
- 본문: 작업 내용 / 변경점 / 테스트 여부 간단히

---

## 4. ⚙️ 백엔드 컨벤션 (Spring Legacy + MyBatis)

### 패키지 구조

```
org.scoula.{도메인}.{controller, service, mapper, domain, dto}
```

- **domain** = DB 테이블 매핑 VO (감사컬럼은 `common.domain.BaseVO` 상속)
- **dto** = 요청/응답 전용 객체 (VO 직접 노출 ❌)
- **mapper** = MyBatis 매퍼 인터페이스
- **매퍼 XML**: `resources/org/scoula/{도메인}/mapper/`

### 도메인 패키지 (15개)

| 도메인        | 담당                | 비고                 |
| ------------- | ------------------- | -------------------- |
| `member`      | 호빈                | `/api/users`         |
| `main`        | 지원                | 메인 요약            |
| `dashboard`   | 석윤                | D-Day·휴가           |
| `simulator`   | 석윤                | 만기 시뮬레이션      |
| `saving`      | 석윤                | 군적금 계좌          |
| `product`     | 석윤·지원·태석·수연 | `/api/products`      |
| `roadmap`     | 지원                | 카테고리별 목표 조회 |
| `travel`      | 태석                | 로드맵 / 여행        |
| `rent`        | 수연                | 로드맵 / 자취(월세)  |
| `car`         | 호빈                | 로드맵 / 자동차      |
| `job`         | 지원                | 로드맵 / 진로        |
| `regret`      | 수연                | 후회소비             |
| `openbanking` | 수연                | 계좌 연동(인프라)    |
| `social`      | 태석                | 랭킹                 |
| `bookmark`    | 공통                | `/api/bookmarks`     |

> `chat` 은 별도 FastAPI 담당 → **Spring 백엔드에 없음**

### 클래스 접미사

`~Controller` · `~Service` · `~ServiceImpl` · `~Mapper` · `~DTO` · `~VO` · `~Test`

### 메서드 네이밍

조회는 **`find` 계열**로 통일한다. (팀 다수 코드가 `find` 를 사용 중)

| 기능      | Controller · Service               | Mapper        | Service 반환값 |
| --------- | ---------------------------------- | ------------- | -------------- |
| 단건 조회 | `find...` (예: `findGoal`)         | `find...`     | DTO            |
| 목록 조회 | `find...List` (예: `findGoalList`) | `find...List` | `List<DTO>`    |
| 등록      | `create...`                        | `insert...`   | **생성된 id**  |
| 수정      | `update...`                        | `update...`   | `void`         |
| 삭제      | `delete...`                        | `delete...`   | `void`         |

> 📌 **Service 는 `create`, Mapper 는 `insert`** — 서비스는 "무엇을 하는지", 매퍼는 "DB에 무슨 SQL을 날리는지"를 나타낸다.
> 참고 코드: `member/mapper/MemberMapper.java`, `member/service/MemberService.java`

### 파라미터 네이밍

- 조건이 붙으면 `By` 사용 — `getSavingAccountListByUserId(Long userId)`
- 파라미터 2개 이상은 **DTO 로 묶기** (`@Param` 남발 금지)

### DTO 네이밍

- 요청/응답 **분리**: `TravelGoalCreateRequestDTO` / `TravelGoalCreateResponseDTO`
- 웹 요청과 무관한 DTO: `UserInfoDTO`
- **VO를 컨트롤러 밖으로 노출하지 않는다** (항상 DTO로 변환)

### VO ↔ DTO 변환

- **DTO 안에 정적 팩토리 메서드 `of()`** 로 통일한다 (팀 다수 방식)
- Service/Controller 에 `new XxxDTO(...)` 로 변환 로직을 흩뿌리지 않는다

```java
// XxxDTO 안에
public static XxxDTO of(XxxVO vo) {
    return XxxDTO.builder()
            .id(vo.getId())
            .name(vo.getName())
            .build();
}
// Service 에서
return XxxDTO.of(vo);
// 목록은
return voList.stream().map(XxxDTO::of).toList();
```

### 날짜·시간 타입

- **`java.util.Date` ❌ → `java.time` 사용** (팀 다수가 LocalDate)
- DB `DATE` → `LocalDate` / DB `DATETIME` → `LocalDateTime`
- (수업 코드 잔재로 `Date` 쓴 곳은 리팩토링 시 교체)

### 요청 검증 (@Valid)

- `@RequestBody` 요청 DTO 에는 **`@Valid` 를 붙인다**
- DTO 필드는 `@NotNull`·`@NotBlank`·`@Positive` 등으로 제약을 명시
- 검증 실패는 `ApiExceptionAdvice` 가 400 으로 자동 처리 (Service 진입 전 차단)

```java
@PostMapping("/goals")
public ResponseEntity<...> createGoal(@Valid @RequestBody XxxCreateRequestDTO request) { ... }
```

### 외부 API 연동

외부 API 호출이 필요한 도메인은 **`{도메인}.client` 패키지로 분리**한다.

```
org.scoula.{도메인}.client.XxxClient
```

- Service 는 client 를 통해서만 외부 API 를 호출 (Service 안에 RestTemplate 직접 사용 ❌)
- 인증 승인 대기 등으로 실제 호출이 어려우면 **인터페이스 + Mock/Real 이원화**(`@Profile`)로 개발
- API 키·URL 은 `application-secret.properties` 로 분리 (§10 참고)

### 네이밍

- **DB**: 테이블·컬럼 `snake_case`
- **Java**: 클래스 `PascalCase` / 변수·메서드 `camelCase` / 상수 `MAX_LOAN_LIMIT`
- MyBatis `mapUnderscoreToCamelCase=true` → `created_date` ↔ `createdDate` 자동 변환

### 코드 스타일

- 재정의 메서드는 `@Override` 표기
- 자기 필드 접근 시 `this.` 명시
- 주입 필드는 `private final` 로 선언 (`@RequiredArgsConstructor` 생성자 주입 + 불변) — 예: `private final MemberService service;`
- 중괄호는 **K&R 스타일**
  ```java
  if (조건) {
      실행;
  } else {
      다른 실행;
  }
  ```
- **import 정렬**: 와일드카드 금지, `java → javax → org → net → com → 프로젝트패키지`
- 한 줄에 한 문장, 한 선언문에 한 변수
- 들여쓰기: IDE 기본값 / 한 줄 최대 **100자**
- `@Builder` 는 생성자 파라미터가 **3개 이상**일 때만 사용
- `ResponseEntity` 생성 시 **상태코드 명시** — `ResponseEntity.status(HttpStatus.CREATED).body(...)`
- **매직넘버 금지**: 반복되거나 의미 있는 숫자·문자열은 상수로 뺀다 — `private static final double ANNUAL_INTEREST_RATE = 0.05;`
- **중복 로직 분리**: 여러 도메인/메서드에 같은 계산·로직이 반복되면 공통 메서드(헬퍼)로 추출한다 (한 곳만 고치도록)
- **날짜 타입**: `java.util.Date` 대신 `LocalDate`/`LocalDateTime`(jsr310) 사용. JSON 응답 포맷은 `@JsonFormat(pattern = "yyyy-MM-dd")` 로 명시
  - (입력 검증 `@Valid` 규칙은 위 "요청 검증(@Valid)" 참고)

### 공통 규칙

- **BaseVO 상속**으로 감사컬럼 5개(`created_date`/`created_nm`/`modified_date`/`modified_nm`/`del_yn`) 처리
- **소프트 삭제**: 물리 삭제 ❌ → `del_yn='Y'` UPDATE (삭제자·일시는 `modified_nm`·`modified_date`)
- 새 도메인 추가 시 `RootConfig` 의 `@MapperScan`·`@ComponentScan` 목록에 등록
- **트랜잭션**: 쓰기 서비스 메서드에 `@Transactional` (INSERT 여러 개 묶는 곳 필수) / **조회 전용 메서드는 `@Transactional(readOnly = true)`**
- **매퍼 SQL `SELECT *` 금지**: 필요한 컬럼을 명시한다 (컬럼 추가 시 매핑 깨짐·불필요 조회 방지)
- **로그인 유저 식별**: 최종적으로 `SecurityContext`(JWT)에서 추출한다. 인증 연동 전까지는 `@RequestParam Long userId` 로 임시 허용하되 `// TODO: JWT 연동 후 교체` 를 남겨, 이후 일괄 교체가 가능하도록 한다

### 공통 응답 포맷 (전 파트 통일)

```json
{
  "success": true,
  "data": {},
  "message": null,
  "timestamp": "2026-07-21T14:30:00"
}
```

에러 시

```json
{
  "success": false,
  "data": null,
  "message": "목표를 찾을 수 없습니다.",
  "code": "RENT_001"
}
```

`common/response/ApiResponse.java` **생성 완료** ✅ — 컨트롤러는 이걸로 감싸서 반환한다.

```java
// 조회
return ResponseEntity.ok(ApiResponse.success(travelService.findGoals(userId)));

// 생성 (id 반환)
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(goalId));

// 수정·삭제 (본문 없음)
return ResponseEntity.ok(ApiResponse.success());

// 실패
return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("목표를 찾을 수 없습니다.", "RENT_001"));
```

### ⚠️ 예외 처리 규칙 (전 파트 통일 — 중요!)

에러는 **Service 에서 `BusinessException` 을 throw** 한다.
**Controller 에서 try-catch / if 문으로 직접 에러 응답을 만들지 않는다.**
→ `ApiExceptionAdvice` 가 자동으로 잡아서 `ApiResponse` 형식으로 변환한다.

```java
// ✅ 권장 — Service 에서 throw (Controller 는 정상 흐름만)
public XxxDTO findXxx(Long id) {
    XxxVO vo = mapper.findXxx(id);
    if (vo == null) {
        throw BusinessException.notFound("대상을 찾을 수 없습니다.", "도메인_001");
    }
    return XxxDTO.from(vo);
}
```

```java
// ❌ 지양 — Controller 에서 직접 에러 응답 (반복·지저분)
if (dto == null) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("...", "도메인_001"));
}
```

**던지는 방법** (`common/exception/BusinessException`)
| 메서드 | HTTP | 언제 |
|---|---|---|
| `BusinessException.badRequest(msg, code)` | 400 | 입력값 오류·규칙 위반 |
| `BusinessException.notFound(msg, code)` | 404 | 대상 없음 |
| `BusinessException.forbidden(msg, code)` | 403 | 권한 없음(남의 데이터 등) |
| `BusinessException.conflict(msg, code)` | 409 | 중복(이미 존재) |

> code 는 선택 — 없으면 `BusinessException.notFound("메시지")` 처럼 생략 가능

### 🚨 에러 코드표

- 형식: **도메인접두어\_번호** (`RENT_001`)
- 접두어는 **대문자 3~6자** 이내 (너무 길면 축약 — `SIMULATOR`❌ → `SIMUL`⭕)
- 번호는 도메인별 **`_001`부터** 순서대로 부여 (건너뛰지 않기, 같은 번호 중복 금지)
- 새 에러 만들 때 → **아래 표(+ 노션)에 추가**하고 다음 번호 사용 (겹침 방지!)
- **도메인 무관 전역 에러**(인증 실패·토큰 오류 등)는 특정 도메인 코드를 쓰지 말고 `AUTH`/`COMMON` 접두어 사용
- 도메인 접두어 통일:
  `MEM`(회원) · `MAIN`(메인) · `DASH`(대시보드) · `SIMUL`(시뮬레이터) · `SAVE`(군적금) · `PROD`(상품)
  · `ROAD`(로드맵) · `TRAVEL`(여행) · `RENT`(자취) · `CAR`(자동차) · `JOB`(진로) · `REGRET`(후회소비)
  · `OPBANK`(오픈뱅킹) · `SOCIAL`(소셜) · `BOOK`(북마크)
  · `AUTH`(인증) · `COMMON`(공통)

**공통·인증 코드 (모든 도메인 공용 — 미리 정의)**
| 코드 | HTTP | 메시지 |
|---|---|---|
| `COMMON_001` | 500 | 서버 내부 오류입니다 |
| `COMMON_002` | 400 | 요청 값 검증에 실패했습니다 |
| `AUTH_001` | 401 | 로그인이 필요합니다 |
| `AUTH_002` | 401 | 액세스 토큰이 유효하지 않습니다 (재발급 필요) |
| `AUTH_003` | 401 | 리프레시 토큰이 만료됐습니다 (재로그인) |
| `AUTH_004` | 403 | 해당 요청에 대한 권한이 없습니다 |

> **도메인별 에러 코드(MEM*·DASH*·RENT\_ …)의 전체 목록은 노션 "에러 코드표" 페이지에서 관리한다.**
> 새 에러를 만들면 → 노션에 한 줄 추가 + 다음 번호 사용 (겹침 방지).

> 📎 **기존 코드 정리**: 이 규칙 이전에 작성된 코드 중 방식이 다른 것(Service 의 `null` 리턴 + Controller 직접 404 처리, JDK 표준 예외 throw 등)은 발견 시 위 방식으로 통일한다. 참고 기준 코드: `member` 도메인.

---

## 5. 🎨 프론트엔드 컨벤션 (Vue 3 + Vite)

### 폴더 구조

```
src/
├── pages/{도메인}/XxxPage.vue     # 화면
├── router/{도메인}.js             # 라우트 → router/index.js 등록
├── api/{도메인}Api.js             # axios 호출 (공통 인스턴스 api/index.js)
├── stores/{도메인}.js             # Pinia 상태 (인증은 stores/auth.js)
├── components/layouts/            # 공통 레이아웃
├── composables/                   # 재사용 로직 (use- 접두사)
└── config/index.js                # 메뉴/타이틀
```

### 라우트 도메인 (13개) · 화면 53개

`member`(10) · `rent`(7) · `travel`(6) · `car`(5) · `job`(5) · `regret`(5) · `main`(3) · `openbanking`(3) · `dashboard`(3) · `simulator`(2) · `product`(2) · `social`(1) · `chat`(1)

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

- JS는 camelCase / 템플릿은 kebab-case / **타입·필수여부·기본값 명시**

```js
defineProps({
  goalId: { type: Number, required: true, default: 0 },
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

**Name 규칙**: `[도메인][페이지역할]` PascalCase — `RentGoalCreate` / `RentListingDetail` / `RegretDashboard` / `Home` / `Login`

### 인증(JWT) 흐름

1. `POST /api/users/login` → JWT 발급
2. `stores/auth.js` 가 토큰을 `localStorage` 저장
3. `api/index.js` 인터셉터가 `Authorization: Bearer {token}` 자동 첨부
4. 401 시 자동 로그아웃 → 로그인 페이지 이동

**인증 필요 화면**은 `meta: { requiresAuth: true }` 만 붙이면 됨
→ `router/index.js` 의 전역 가드가 자동 처리 (비로그인 시 `Login` 이동 + `redirect` 쿼리 보존)

### Prettier 설정

```json
{
  "semi": true,
  "tabWidth": 2,
  "bracketSpacing": true,
  "trailingComma": "all",
  "arrowParens": "always",
  "singleQuote": true
}
```

`frontend/.prettierrc` **생성 완료** ✅ — VSCode의 Prettier 확장을 켜두면 저장 시 자동 정렬된다.

> 스타일 차이로 인한 불필요한 diff·충돌을 막기 위해 **모두 켜고 작업**해주세요.

---

## 6. 🔗 REST API 컨벤션

### Base

- `/api/{도메인}/...` (예: `/api/users`, `/api/travel/goals`, `/api/openbanking/link`)
- 로그인: `POST /api/users/login`

### 표기 규칙

1. 리소스는 **복수형** (`/goals`, `/listings`)
   - ⚠️ **예외 — 불가산 명사는 단수 유지**: `feedback` · `history` · `glossary` 처럼
     영어에서 개수를 세지 않는 명사는 복수형(`feedbacks`)이 어색하므로 **단수로 쓴다**
   - 판단 기준: _"이걸 2개, 3개라고 셀 수 있나?"_ → 셀 수 있으면 복수형
     (`session` → `sessions` ⭕ / `feedback` → `feedbacks` ❌)
   - 같은 리소스는 **메서드가 달라도 경로를 통일**한다
     (`POST /sessions` + `GET /sessions` ⭕ / `POST /session` + `GET /sessions` ❌)
2. 경로 변수는 의미 있게 **camelCase** (`{goalId}`, `{goalid}` ❌)
3. 여러 단어는 **케밥 케이스** (`saving-details`)
4. URL에 동사 금지, 단 **확정 액션은 `/confirm` 허용**
   - `GET /recommend/{messageId}` ❌ → `GET /messages/{messageId}/recommendations` ⭕
5. 하나의 엔드포인트 = 명세서 1행 (내부 로직은 비고에)
6. **부분 수정은 PATCH, 전체 교체는 PUT** (필드 1~2개는 PUT 허용)

> 📌 **컨벤션과 API 명세서(노션)가 다를 때는 → 컨벤션을 기준으로 맞추고, 노션 명세서도 함께 수정한다.**
> (컨벤션이 명세서보다 나중에 정해져서 생긴 불일치가 있을 수 있음)

### 로드맵 4파트 공통 URL 패턴

도메인 = `travel` / `job` / `car` / `rent`

| Method | URL                                    | 기능                         |
| ------ | -------------------------------------- | ---------------------------- |
| POST   | `/api/{도메인}/goals`                  | 목표 등록                    |
| GET    | `/api/{도메인}/goals/current`          | 진행중 목표 조회             |
| GET    | `/api/{도메인}/goals/{goalId}`         | 상세 조회 (게시판 상세 겸용) |
| DELETE | `/api/{도메인}/goals/{goalId}`         | 삭제 (soft)                  |
| POST   | `/api/{도메인}/goals/{goalId}/confirm` | 로드맵 저장 (CONFIRMED 전환) |
| GET    | `/api/{도메인}/boards`                 | 게시판 목록 (필터 지원)      |

> **목표 상세 조회 = 게시판 상세**로 통합 (별도 API 없음)

### 게시판 필터

- **공통**: `from` · `to` (저장일 기간)
- **도메인별**: rent `estateType`·`regionCode` / travel `isDomestic`·`style` / car `carTypeCode`·`isNew` / job `goalType`

### 관심항목(북마크) 공통 API

```
POST   /api/bookmarks              등록 (body: categoryId, goalId)
DELETE /api/bookmarks/{bookmarkId} 해제
GET    /api/bookmarks              내 관심항목 목록
```

> **도메인별 북마크 API 만들지 않기** — 홈에서 4개 API 호출하는 낭비 방지

### 로드맵 목표 상태값 (travel · rent · car · job 공통)

목표(`status`)를 가지는 로드맵 도메인은 아래 상태값을 공통으로 사용한다.

- **DRAFT** — 작성 중 / 임시 저장
- **CONFIRMED** — 저장 완료 / 게시판에 올라감
- **ARCHIVED** — 보관됨 / 옛 목표

> 회원당 DRAFT 1건 · CONFIRMED 1건 규칙은 **애플리케이션에서 검증** (ARCHIVED는 무제한이라 DB UNIQUE 불가)

---

## 7. 🤖 챗봇(FastAPI) 연동 — B안

- 프론트는 챗봇만 **FastAPI(:8000)** 로 직접 호출, 그 외는 **Spring(:8080)**
- 두 서버가 **동일 JWT(HS256)** 검증 → secret key 공유
- `chat_session` / `chat_message` / `chat_feedback` 테이블은 **FastAPI 소유**
- 상세: [chatbot/README.md](chatbot/README.md)

---

## 8. 📝 문서 관리

| 문서                   | 내용                                               |
| ---------------------- | -------------------------------------------------- |
| 요구사항 정의서        | 우선순위·요구사항 ID·담당자·서비스 메뉴            |
| 엔티티 / 테이블 정의서 | Entity·컬럼별 상세(Type·Null·PK/FK·제약)           |
| ERD                    | dbdiagram 코드로 관리 (Ref 표기 통일)              |
| API 명세서             | 노션 DB (기능명·API ID·Method·URL·담당자·상태)     |
| 화면목록·라우트 정의서 | 노션 DB (화면 ID·경로·담당자·도메인·라우트명·인증) |
| WBS                    | 노션 DB (작업명·유형·우선순위·상태·기간)           |

**공통 원칙**

- 컨벤션 변경 시 **모든 문서에 동시 반영** (요구사항·엔티티·테이블·ERD·SQL)
- 남의 파트 테이블·API 를 사용해야 하면 **담당자와 사전 협의 필수** (필드·연동 순서 확정)
- 문서 변경 이력은 슬랙에 간단히 공유

---

## 10. 🔐 API 키 · 비밀값 관리 (전원 필독)

우리 저장소는 **Public(공개)** 입니다. 코드에 API 키를 넣고 커밋하면
**수집 봇이 몇 분 만에 긁어가서 도용**됩니다. 아래 규칙을 반드시 지켜주세요.

### 규칙

- ❌ 코드·`application.properties` 에 **실제 키를 직접 적지 않는다**
- ✅ 키는 **`application-secret.properties`** 에만 넣는다 (`.gitignore` 처리되어 커밋 안 됨)
- ✅ **키 값은 노션 "환경변수" 페이지에서 팀끼리 공유**한다
- ✅ 새 키가 생기면 **`application-secret.properties.example` 에 항목명만** 추가하고 커밋 (값은 비워둠)

### 처음 세팅할 때 (각자 1회)

```
1. backend/src/main/resources/application-secret.properties.example 복사
2. 같은 폴더에 application-secret.properties 로 저장
3. 노션 "환경변수" 페이지의 값을 붙여넣기
```

> 파일이 없어도 서버는 정상 실행됩니다 (`ignoreResourceNotFound = true`)

### 코드에서 쓰는 법

```java
@Value("${키.이름}")
private String someKey;
```

### 키 목록

- 실제 키 이름·값·담당은 **노션 "환경변수" 페이지**에서 관리한다 (여기에 값을 나열하지 않음)
- `jwt.secret` 은 **공용** (Spring · FastAPI 동일 값 사용)
- 새 키가 필요하면 → `application-secret.properties.example` 에 **항목명만** 추가 + 노션에 값 등록

> ⚠️ 실수로 키를 커밋했다면 **즉시 알려주세요.** 커밋을 지워도 히스토리에 남기 때문에 **해당 키를 폐기하고 재발급**해야 합니다.

---

## 9. 🗄 DB 실행

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS scoula_db DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p scoula_db < kb-schema.sql
mysql -u root -p scoula_db < kb-data.sql
```

> 위치: `backend/src/main/resources/sql/` · 자세한 내용은 해당 폴더 README 참고
> **다시 넣을 땐 `kb-schema.sql` 부터** 재실행 (AUTO_INCREMENT 리셋)

**⚠️ 공용 SQL(`kb-schema.sql`·`kb-data.sql`)은 모두가 각자 로컬에 실행하는 파일이다.**

- 변경(테이블 추가·컬럼 변경·데이터 수정) 시 → **슬랙에 공지**하고, 팀 전원이 **`schema` 부터 재실행**해 로컬 DB를 맞춘다
- 테이블 추가·변경 시 → ERD·테이블정의서에도 **동시 반영**(§8)

---

## ✅ 착수 전 체크리스트

- [x] `common/response/ApiResponse.java` 생성 (§4 공통 응답 포맷)
- [x] `frontend/.prettierrc` 생성 (§5 Prettier 설정)
- [x] `frontend/src/composables/` 폴더 생성 (§5 폴더 구조)
- [ ] GitHub `main`·`dev` 브랜치 보호 설정 (§1) — _조수연_
- [ ] 각자 로컬 MySQL 에 `scoula_db` 생성 + 스키마·데이터 실행 (§9)
- [ ] VSCode **Prettier 확장** 설치 및 "저장 시 포맷" 켜기
