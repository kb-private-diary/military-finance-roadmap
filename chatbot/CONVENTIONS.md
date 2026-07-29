# 챗봇(FastAPI) 컨벤션

> 이 문서는 챗봇 서버(FastAPI, `:8000`)에만 적용되는 컨벤션입니다.
> 팀 전체 컨벤션은 [`docs/CONVENTIONS.md`](../docs/CONVENTIONS.md)를 참고하세요 — 여긴 그중 챗봇 파트를 더 구체화한 것입니다.

---

## 1. REST API 컨벤션

- Base: `/api/chat/...`
- 리소스는 **복수형** (`/sessions`, `/messages`)
  - 예외 — 불가산 명사는 단수 유지: `feedback` · `history` · `glossary`
- 경로 변수는 camelCase로 노출 (`Path(..., alias="sessionId")`), 내부 파라미터명은 snake_case
- URL에 동사 금지, 리소스 중첩으로 표현 (`/messages/{messageId}/recommendations`)

| Method | Path | 기능 |
|---|---|---|
| POST | `/sessions` | 대화 세션 생성/재사용 |
| GET | `/sessions` | 내 세션 목록 |
| GET | `/history/{sessionId}` | 대화 히스토리 |
| POST | `/messages` | 질문 전송 + 챗봇 응답 |
| POST | `/feedback` | 만족도 피드백 |
| GET | `/messages/{messageId}/recommendations` | 관련 콘텐츠 추천 |
| GET | `/topics` · `/faq-categories` | 초기 카테고리 메뉴 |
| GET | `/products` · `/products/{name}` | 상품 목록·상세 |
| GET | `/glossary` · `/glossary/{term}` | 정책 용어 목록·상세 |

## 2. 응답 포맷

- **성공**: FastAPI가 Pydantic `response_model`을 그대로 직렬화해서 반환한다 (별도 wrapper 없음). 프론트는 성공 시 바로 필드에 접근한다.
- **실패**: Spring(`ApiResponse`)과 동일한 형태로 통일한다.
  ```json
  { "success": false, "data": null, "message": "...", "code": "CHAT_001", "timestamp": "..." }
  ```
  라우터에서 직접 에러 응답을 만들지 않고, `BusinessException(message, status_code, code)`을 던지면 `app/core/exceptions.py`의 전역 핸들러가 자동으로 위 형태로 변환한다.

## 3. 에러 코드

- 형식: `CHAT_번호` (팀 전체 규칙과 동일: 도메인접두어_번호)
- 새 에러 만들 때 → 노션 에러코드표에 추가하고 다음 번호 사용

| 코드 | HTTP | 상황 |
|---|---|---|
| CHAT_001 | 404 | 세션을 찾을 수 없음 |
| CHAT_002 | 400 | 질문 미입력 |
| CHAT_003 | 400 | 질문 500자 초과 |
| CHAT_004 | 400 | 지원하지 않는 카테고리 |
| CHAT_005 | 404 | 상품을 찾을 수 없음 |
| CHAT_006 | 404 | 용어를 찾을 수 없음 |
| CHAT_007 | 400 | feedback 값이 잘못됨(like/dislike 외) |
| CHAT_008 | 404 | 세션에 속하지 않는 메시지 ID |

## 4. 스키마(Pydantic) 컨벤션

- 모든 요청/응답 스키마는 `CamelModel`(`app/schemas/base.py`)을 상속 — snake_case ↔ camelCase 자동 변환
- 요청/응답을 분리해서 정의: `XxxCreateRequest` (요청) / `XxxItem` · `XxxDetail` (응답)
- 서버가 자동으로 채우는 값(id, 생성일시 등)은 요청 스키마엔 없고 응답 스키마에만 존재

## 5. 폴더 구조 · 네이밍

```
app/
├── core/       # 설정, DB 세션, 공통 예외
├── models/     # SQLAlchemy ORM 모델
├── schemas/    # Pydantic 요청·응답 스키마
├── routers/    # 엔드포인트 (얇게 — 검증 후 서비스 호출)
└── services/   # 비즈니스 로직, 외부 API, RAG/LLM 파이프라인
```

서비스 함수 네이밍:
| 접두사 | 용도 | 예시 |
|---|---|---|
| `find_` | 단건 조회 | `find_glossary_term`, `find_fund` |
| `fetch_` | 외부 API 호출 | `fetch_products`, `fetch_listings` |
| `search_` | 검색성 조회 | `search_policies` |

## 6. RAG / AI 파이프라인 원칙

- **청킹**: 정책 문서가 `[섹션제목]` 형식으로 작성돼 있어, 이 형식에 맞춘 정규식 기반 커스텀 청킹을 사용한다. 랭체인의 범용 텍스트 분할기보다 의미 단위 보존에 더 적합하다고 판단해 유지한다.
- **임베딩·벡터검색**: `langchain_chroma.Chroma`로 감싸서 LangChain 표준 Retriever로 사용한다.
- **LLM 호출·모델 폴백**: `ChatGoogleGenerativeAI.with_fallbacks([...])`로 기본 모델 실패(할당량 초과·과부하) 시 예비 모델로 자동 전환한다.
- **응답 생성 흐름**: 의도분류 → 조건부 분기 → 컨텍스트 조합 → 답변 생성 과정을 LangGraph `StateGraph`로 명시적인 그래프로 관리한다.
- **재인덱싱**: `build_index(force=True)`는 기존 인덱스를 바로 지우지 않고, 임시 컬렉션에 새로 채운 뒤 검증하고 성공했을 때만 교체한다 (build-then-swap). 청킹·임베딩 도중 오류가 나도 기존 검색 데이터는 보존된다.

## 7. 검증 습관

- 새 엔드포인트를 추가하면 실제로 서버를 띄워서 성공·실패 케이스를 curl로 직접 호출해 확인한 뒤 커밋한다. 스키마·타입 체크만으로는 실제 동작을 보장하지 못한다.
