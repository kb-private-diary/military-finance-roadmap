# 📗 개발 컨벤션: 백엔드 (텅장일병구하기)

30반 4팀 · KB IT's Your Life 7기 · Spring Legacy + MyBatis

> 이 문서 = **백엔드 컨벤션**. 프론트엔드는 [`CONVENTIONS_FRONTEND.md`](CONVENTIONS_FRONTEND.md) 참고.
> §0~3(공통 규칙·Git·커밋·PR)은 양쪽 문서에 동일하게 실려 있습니다. §번호는 기존 참조 유지를 위해 그대로 둡니다.
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

**개인 브랜치 → dev → main** (3단계)

| 브랜치      | 역할                            | 직접 push |
| ----------- | ------------------------------- | --------- |
| `main`      | 배포용. 완성·검증된 것만        | ❌ (PR만) |
| `dev`       | 통합/테스트용. 각자 기능을 모음 | ❌ (PR만) |
| 개인 브랜치 | 각자 개발 공간                  | ⭕ 자유   |

**개인 브랜치**: `dev_seokyun` · `dev_taeseok` · `dev_hobin` · `dev_jiwon` · `dev_suyeon` · `dev_eseudeo`

**흐름**: 개인 브랜치에서 개발 → `개인 → dev` PR(리뷰 후 병합) → 배포 전 `dev → main` PR

**규칙**

- `dev` / `main` 은 PR로만 병합, 리뷰 1명 이상 승인
- 개인 브랜치 작업 전/중 `dev` 를 자주 pull 해서 충돌 예방
- Git 클라이언트는 자유 (Fork / CLI / VSCode 등)

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
❇️[feat] 여행 목표 등록 API #12

- travel domain VO/Mapper 작성
- travel service 등록 로직 구현
```

- `#이슈번호` 는 GitHub Issues 를 쓸 때만 / `Closes #12` 넣으면 병합 시 이슈 자동 종료
- `chore` vs `setting`: 의존성·빌드 설정 = **setting**, 그 외 잡일 = **chore**

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

조회는 **`find` 계열**로 통일 (팀 다수 코드가 `find`)

| 기능      | Controller · Service               | Mapper        | Service 반환값 |
| --------- | ---------------------------------- | ------------- | -------------- |
| 단건 조회 | `find...` (예: `findGoal`)         | `find...`     | DTO            |
| 목록 조회 | `find...List` (예: `findGoalList`) | `find...List` | `List<DTO>`    |
| 등록      | `create...`                        | `insert...`   | **생성된 id**  |
| 수정      | `update...`                        | `update...`   | `void`         |
| 삭제      | `delete...`                        | `delete...`   | `void`         |

> 📌 **Service 는 `create`, Mapper 는 `insert`**: 서비스는 "무엇을", 매퍼는 "무슨 SQL"을 나타냄.
> 참고 코드: `member/mapper/MemberMapper.java`, `member/service/MemberService.java`

### 파라미터 네이밍

- 조건이 붙으면 `By`: `getSavingAccountListByUserId(Long userId)`
- 파라미터 2개 이상은 **DTO 로 묶기** (`@Param` 남발 금지)

### DTO 네이밍

- 요청/응답 **분리**: `TravelGoalCreateRequestDTO` / `TravelGoalCreateResponseDTO`
- 웹 요청과 무관한 DTO: `UserInfoDTO`
- **VO를 컨트롤러 밖으로 노출하지 않는다** (항상 DTO로 변환)

### VO ↔ DTO 변환

- **DTO 안에 정적 팩토리 메서드 `of()`** 로 통일 (팀 다수 방식)
- Service/Controller 에 `new XxxDTO(...)` 변환 로직 흩뿌리지 않기

```java
public static XxxDTO of(XxxVO vo) {
    return XxxDTO.builder().id(vo.getId()).name(vo.getName()).build();
}
// Service 에서
return XxxDTO.of(vo);
// 목록은
return voList.stream().map(XxxDTO::of).toList();
```

### 날짜·시간 타입

- **`java.util.Date` ❌ → `java.time`** (팀 다수 LocalDate)
- DB `DATE` → `LocalDate` / DB `DATETIME` → `LocalDateTime`
- JSON 응답 포맷은 `@JsonFormat(pattern = "yyyy-MM-dd")` 로 명시

### 요청 검증 (@Valid)

- `@RequestBody` 요청 DTO 에는 **`@Valid`** + 필드에 `@NotNull`·`@NotBlank`·`@Positive` 등
- 검증 실패는 `ApiExceptionAdvice` 가 400 으로 자동 처리

```java
@PostMapping("/goals")
public ResponseEntity<...> createGoal(@Valid @RequestBody XxxCreateRequestDTO request) { ... }
```

> ⚠️ **현재 이슈**: `build.gradle` 에 validation provider(hibernate-validator) 미탑재라 `@Valid` 애노테이션이 실제로 동작하지 않음 → **당분간 검증은 Service 에서 `BusinessException.badRequest` 로** 수행. (validator 의존성 추가는 팀 [setting] 결정 대기)

### 외부 API 연동

- 외부 API 호출 도메인은 **`{도메인}.client` 패키지로 분리** (`org.scoula.{도메인}.client.XxxClient`)
- Service 는 client 를 통해서만 호출 (Service 안 RestTemplate 직접 ❌)
- 인증 대기 등으로 실제 호출 어려우면 **인터페이스 + Mock/Real 이원화(`@Profile`)**
- API 키·URL 은 `application-secret.properties` (§10)

### 네이밍

- **DB**: 테이블·컬럼 `snake_case`
- **Java**: 클래스 `PascalCase` / 변수·메서드 `camelCase` / 상수 `MAX_LOAN_LIMIT`
- MyBatis `mapUnderscoreToCamelCase=true` → `created_date` ↔ `createdDate` 자동 변환

### 코드 스타일

- 재정의 메서드 `@Override` / 자기 필드 접근 `this.` 명시
- 주입 필드 `private final` + `@RequiredArgsConstructor` (생성자 주입 + 불변)
- 중괄호 **K&R 스타일**
- **import 정렬**: 와일드카드 금지, `java → javax → org → net → com → 프로젝트패키지`
- 한 줄에 한 문장 / 한 선언문에 한 변수 / 한 줄 최대 **100자**
- `@Builder` 는 생성자 파라미터 **3개 이상**일 때만
- `ResponseEntity` 생성 시 **상태코드 명시**
- **매직넘버 금지**: `private static final double ANNUAL_INTEREST_RATE = 0.05;`
- **중복 로직 분리**: 반복 계산·로직은 공통 헬퍼로

### 공통 규칙

- **BaseVO 상속**으로 감사컬럼 5개(`created_date`/`created_nm`/`modified_date`/`modified_nm`/`del_yn`) 처리
- **소프트 삭제**: 물리 삭제 ❌ → `del_yn='Y'` UPDATE (삭제자·일시는 `modified_nm`·`modified_date`)
- 새 도메인 추가 시 `RootConfig` 의 `@MapperScan`·`@ComponentScan` 목록에 등록
- **트랜잭션**: 쓰기 메서드 `@Transactional` / **조회 전용 `@Transactional(readOnly = true)`**
- **매퍼 SQL `SELECT *` 금지**: 필요한 컬럼 명시
- **로그인 유저 식별**: 최종은 `SecurityContext`(JWT). 인증 연동 전까지 `@RequestParam Long userId` 임시 허용 + `// TODO: JWT 연동 후 교체`

### 공통 응답 포맷 (전 파트 통일)

```json
{ "success": true, "data": {}, "message": null, "timestamp": "2026-07-21T14:30:00" }
```

에러 시 `{ "success": false, "data": null, "message": "...", "code": "RENT_001" }`

`common/response/ApiResponse.java` ✅: 컨트롤러는 이걸로 감싸서 반환

```java
return ResponseEntity.ok(ApiResponse.success(travelService.findGoals(userId)));       // 조회
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(goalId));    // 생성
return ResponseEntity.ok(ApiResponse.success());                                       // 수정·삭제
```

### ⚠️ 예외 처리 규칙 (전 파트 통일: 중요!)

에러는 **Service 에서 `BusinessException` throw**. **Controller 에서 try-catch/if 로 직접 에러 응답 만들지 않기.** → `ApiExceptionAdvice` 가 자동으로 `ApiResponse` 로 변환.

```java
// ✅ Service 에서 throw (Controller 는 정상 흐름만)
public XxxDTO findXxx(Long id) {
    XxxVO vo = mapper.findXxx(id);
    if (vo == null) throw BusinessException.notFound("대상을 찾을 수 없습니다.", "도메인_001");
    return XxxDTO.of(vo);
}
```

**던지는 방법** (`common/exception/BusinessException`)
| 메서드 | HTTP | 언제 |
|---|---|---|
| `BusinessException.badRequest(msg, code)` | 400 | 입력값 오류·규칙 위반 |
| `BusinessException.notFound(msg, code)` | 404 | 대상 없음 |
| `BusinessException.forbidden(msg, code)` | 403 | 권한 없음 |
| `BusinessException.conflict(msg, code)` | 409 | 중복 |

### 🚨 에러 코드표

- 형식: **도메인접두어\_번호** (`RENT_001`)
- 접두어는 **대문자 3~6자** 이내 (`SIMULATOR`❌ → `SIMUL`⭕)
- 번호는 도메인별 **`_001`부터** 순서대로 (건너뛰기·중복 금지)
- 전역 에러(인증·토큰)는 `AUTH`/`COMMON` 접두어
- 접두어: `MEM`·`MAIN`·`DASH`·`SIMUL`·`SAVE`·`PROD`·`ROAD`·`TRAVEL`·`RENT`·`CAR`·`JOB`·`REGRET`·`OPBANK`·`SOCIAL`·`BOOK`·`AUTH`·`COMMON`

**공통·인증 코드 (미리 정의)**
| 코드 | HTTP | 메시지 |
|---|---|---|
| `COMMON_001` | 500 | 서버 내부 오류입니다 |
| `COMMON_002` | 400 | 요청 값 검증에 실패했습니다 |
| `AUTH_001` | 401 | 로그인이 필요합니다 |
| `AUTH_002` | 401 | 액세스 토큰이 유효하지 않습니다 (재발급 필요) |
| `AUTH_003` | 401 | 리프레시 토큰이 만료됐습니다 (재로그인) |
| `AUTH_004` | 403 | 해당 요청에 대한 권한이 없습니다 |

> 도메인별 에러 코드 전체 목록은 노션 "에러 코드표" 페이지에서 관리. 새 에러 → 노션에 추가 + 다음 번호.

---

## 6. 🔗 REST API 컨벤션

### Base

- `/api/{도메인}/...` (예: `/api/users`, `/api/travel/goals`, `/api/openbanking/link`)
- 로그인: `POST /api/users/login`

### 표기 규칙

1. 리소스는 **복수형** (`/goals`, `/listings`)
   - ⚠️ 예외: 불가산 명사(`feedback`·`history`·`glossary`)는 **단수 유지**
   - 판단: _"2개·3개로 셀 수 있나?"_ → 셀 수 있으면 복수형
2. 경로 변수는 의미 있게 **camelCase** (`{goalId}`)
3. 여러 단어는 **케밥 케이스** (`saving-details`)
4. URL에 동사 금지, 단 **확정 액션 `/confirm` 허용**
5. 하나의 엔드포인트 = 명세서 1행
6. **부분 수정 PATCH, 전체 교체 PUT** (필드 1~2개는 PUT 허용)

> 📌 컨벤션과 API 명세서(노션)가 다르면 → 컨벤션 기준으로 맞추고 노션도 수정

### 로드맵 4파트 공통 URL 패턴 (`travel`/`job`/`car`/`rent`)

| Method | URL                                    | 기능                     |
| ------ | -------------------------------------- | ------------------------ |
| POST   | `/api/{도메인}/goals`                  | 목표 등록                |
| GET    | `/api/{도메인}/goals/current`          | 진행중 목표 조회         |
| GET    | `/api/{도메인}/goals/{goalId}`         | 상세 (게시판 상세 겸용)  |
| DELETE | `/api/{도메인}/goals/{goalId}`         | 삭제 (soft)              |
| POST   | `/api/{도메인}/goals/{goalId}/confirm` | 로드맵 저장 (CONFIRMED)  |
| GET    | `/api/{도메인}/boards`                 | 게시판 목록 (필터)       |

### 게시판 필터

- **공통**: `from` · `to` (저장일 기간)
- **도메인별**: rent `estateType`·`regionCode` / travel `isDomestic`·`style` / car `carTypeCode`·`isNew` / job `goalType`

### 관심항목(북마크) 공통 API

```
POST /api/bookmarks (categoryId, goalId) · DELETE /api/bookmarks/{bookmarkId} · GET /api/bookmarks
```

> **도메인별 북마크 API 만들지 않기**

### 로드맵 목표 상태값 (travel·rent·car·job 공통)

- **DRAFT**(작성 중) → **CONFIRMED**(저장 완료·게시판) → **ARCHIVED**(보관)
- 회원당 DRAFT 1건·CONFIRMED 1건은 **애플리케이션에서 검증**

---

## 8. 📝 문서 관리

| 문서                   | 내용                                           |
| ---------------------- | ---------------------------------------------- |
| 요구사항 정의서        | 우선순위·요구사항 ID·담당자·서비스 메뉴        |
| 엔티티 / 테이블 정의서 | Entity·컬럼별 상세(Type·Null·PK/FK·제약)       |
| ERD                    | dbdiagram 코드로 관리                          |
| API 명세서             | 노션 DB (기능명·API ID·Method·URL·담당자·상태) |
| WBS                    | 노션 DB (작업명·유형·우선순위·상태·기간)       |

- 컨벤션 변경 시 모든 문서에 동시 반영 / 남의 파트 테이블·API 사용은 담당자와 사전 협의 / 변경 이력 슬랙 공유

---

## 9. 🗄 DB 실행

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS scoula_db DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p scoula_db < kb-schema.sql
mysql -u root -p scoula_db < kb-data.sql
```

> 위치: `backend/src/main/resources/sql/` · **다시 넣을 땐 `kb-schema.sql` 부터** (AUTO_INCREMENT 리셋)

**⚠️ 공용 SQL(`kb-schema.sql`·`kb-data.sql`)은 각자 로컬에 실행하는 파일**
- 변경 시 → **슬랙 공지** + 전원 **`schema` 부터 재실행** / ERD·테이블정의서에도 동시 반영(§8)

---

## 10. 🔐 API 키 · 비밀값 관리 (전원 필독)

저장소가 **Public** 이라 코드에 키를 커밋하면 수집 봇이 도용함.

- ❌ 코드·`application.properties` 에 실제 키 직접 적기 금지
- ✅ 키는 **`application-secret.properties`** 에만 (`.gitignore` 처리)
- ✅ 키 값은 **노션 "환경변수" 페이지**에서 공유
- ✅ 새 키는 `application-secret.properties.example` 에 **항목명만** 추가하고 커밋

```java
@Value("${키.이름}")
private String someKey;
```

- `jwt.secret` 은 **공용** (Spring · FastAPI 동일 값)
- ⚠️ 실수로 키 커밋하면 **즉시 공유** → 키 폐기·재발급 (히스토리에 남음)

---

## ✅ 백엔드 착수 체크리스트

- [x] `common/response/ApiResponse.java` 생성 (§4 공통 응답)
- [ ] 각자 로컬 MySQL 에 `scoula_db` 생성 + 스키마·데이터 실행 (§9)
- [ ] GitHub `main`·`dev` 브랜치 보호 설정 (§1): _조수연_
