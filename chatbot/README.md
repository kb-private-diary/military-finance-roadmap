# 🤖 AI 재무 상담 챗봇 (FastAPI)

> **텅장일병구하기** — KB 군장병 리텐션 서비스의 AI 챗봇 서버
> 생성형 AI + RAG 기반으로 전역 후 목돈 활용 상담 및 정책·상품 안내를 제공합니다.

<p>
  <img src="https://img.shields.io/badge/FastAPI-009688?style=flat-square&logo=fastapi&logoColor=white" />
  <img src="https://img.shields.io/badge/Python-3776AB?style=flat-square&logo=python&logoColor=white" />
  <img src="https://img.shields.io/badge/SQLAlchemy-D71F00?style=flat-square" />
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Chroma-vectorDB-FF6F61?style=flat-square" />
</p>

담당: **송에스더**

---

## 🏗 아키텍처 (B안 — 프론트 직접 호출)

메인 서비스(Spring)와 **분리된 서버**로 동작하며, 프론트엔드가 챗봇 요청만 이쪽으로 직접 보냅니다.

```
┌─────────────┐   /api/**       ┌──────────────────┐     ┌───────────┐
│  Vue 3 (FE) │ ──────────────▶ │ Spring Legacy(BE) │ ──▶ │           │
│   :5173     │                 │      :8080        │     │  MySQL    │
└──────┬──────┘                 └──────────────────┘     │ scoula_db │
       │  /api/chat/**  (동일 JWT)                        │           │
       └───────────────────────▶ ┌──────────────────┐    │           │
                                  │  FastAPI (챗봇)   │ ──▶│           │
                                  │      :8000       │     └───────────┘
                                  │  LLM · RAG       │
                                  └──────────────────┘
```

- 프론트는 일반 기능은 Spring(:8080), **챗봇만 이 서버(:8000)** 로 직접 호출
- **DB는 메인과 공유**(`scoula_db`) — 이 서버는 `chat_session` / `chat_message` / `chat_feedback` 3개 테이블을 담당
- 인증은 **Spring이 발급한 JWT를 그대로 검증**(동일 secret) — 자세한 규칙은 아래 참고

## 🛠 기술 스택

FastAPI · SQLAlchemy · PyMySQL · Chroma(벡터DB) · `gemini-embedding-001`(768차원) · RAG

## 📁 폴더 구조

```
chatbot/
├── app/
│   ├── main.py              # FastAPI 진입점 · CORS 설정
│   ├── core/
│   │   ├── config.py        # 환경변수 로드 (.env)
│   │   └── db.py            # DB 세션 · Base
│   ├── models/chat.py       # SQLAlchemy 모델 (chat_session/message/feedback)
│   ├── schemas/chat.py      # Pydantic 요청·응답 스키마
│   └── routers/chat.py      # /api/chat 엔드포인트
├── .env.example             # 환경변수 템플릿
└── requirements.txt
```

## 🚀 실행 방법

```bash
# 1) 가상환경
python -m venv venv
venv\Scripts\activate        # Windows
# source venv/bin/activate   # macOS/Linux

# 2) 패키지 설치
pip install -r requirements.txt

# 3) 환경변수 설정 (.env.example 복사 후 값 채우기)
copy .env.example .env       # Windows

# 4) 서버 실행 (포트 8000)
uvicorn app.main:app --reload --port 8000
```

접속 확인: <http://localhost:8000/health> · API 문서: <http://localhost:8000/docs>

### `.env` 설정

```
DB_HOST=localhost
DB_PORT=3306
DB_USER=scoula
DB_PASSWORD=1234
DB_NAME=scoula_db        # ※ 메인 서비스와 같은 DB
```

> DB 테이블은 메인 레포의 `backend/src/main/resources/sql/kb-schema.sql` 로 생성됩니다. 이 서버는 테이블을 직접 만들지 않습니다.

## 🔗 API 목록 (base `/api/chat`)

| Method | Path | 기능 | 상태 |
|---|---|---|---|
| POST | `/sessions` | 대화 세션 생성/재사용 | ✅ |
| GET | `/sessions` | 내 세션 목록 | ✅ |
| GET | `/history/{sessionId}` | 대화 히스토리 | ✅ |
| POST | `/messages` | 질문 전송 + 챗봇 응답 | ✅ |
| POST | `/feedback` | 만족도 피드백 | ⬜ |
| GET | `/messages/{messageId}/recommendations` | 관련 콘텐츠 추천 | ⬜ |
| GET | `/topics` | 초기 카테고리 메뉴 | ✅ |
| GET | `/faq-categories` | FAQ 카테고리 | ✅ |
| GET | `/products` · `/products/{name}` | 상품 목록·상세 | ✅ |
| GET | `/glossary` · `/glossary/{term}` | 정책 용어 목록·상세 | ✅ |

## 🔐 메인 서비스와의 연동 규칙

1. **JWT 검증** — 헤더 `Authorization: Bearer <token>`
   - 알고리즘 `HS256`, claim `sub` = username(로그인 아이디)
   - Spring과 **동일한 secret key**(UTF-8)로 검증 → `.env` 로 주입
   - 회원 PK 필요 시: `SELECT id FROM user WHERE user_id = :sub`
   - ⬜ *현재 미구현 — 임시로 `user_id` 를 요청에서 받고 있음 (`TODO` 주석 참고)*
2. **감사 컬럼** — 모든 INSERT 시 `created_date`, `created_nm`, `del_yn='N'` 채우기
   - 삭제는 물리 삭제 금지, `del_yn='Y'` 로 소프트 삭제
3. **CORS** — 프론트 origin(`http://localhost:5173`) 허용
4. **응답 JSON 키** — 메인 서비스(Spring)와 동일하게 camelCase(`sessionId`)로 직렬화됨 (Pydantic `alias_generator=to_camel`, 경로 변수도 동일 적용). 내부 파이썬 코드는 snake_case 그대로 사용

## ✅ TODO

- [ ] JWT 검증 미들웨어 구현 (secret 공유 필요)
- [x] LLM API 연동 · 프롬프트 설계 (Gemini, 다나까 말투)
- [x] 정책 문서 수집 · 청킹 · 임베딩 (Chroma) — 문서 4종(장병내일준비적금·청년미래적금·청년주택드림청약통장·정책용어사전)
- [x] RAG 파이프라인 (질의 임베딩 → 유사문서 검색 → 답변)
- [x] 질문 의도 분류(정보성/상담성/무관련) — counsel(상담성) 전용 상담 흐름(자금·기간 되묻기)은 WBS-6에서 별도 진행 예정
- [ ] 피드백 저장 · 관련 콘텐츠 추천
- [ ] 외부 API: 온통청년(청년미래적금·청년주택드림청약통장) — 서버 자체 장애로 보류 중, 재시도 필요
