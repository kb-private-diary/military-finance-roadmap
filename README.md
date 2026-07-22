# 💰 텅장일병구하기

> **MZ세대를 위한 자산관리 서비스** — 군적금 목돈 형성·운용 설계를 위한 KB 군장병 리텐션 서비스

<p>
  <img src="https://img.shields.io/badge/KB_IT's_Your_Life-7기-FFB300?style=flat-square" />
  <img src="https://img.shields.io/badge/30반_4팀-텅장일병구하기-60584C?style=flat-square" />
  <img src="https://img.shields.io/badge/KB_국민은행_미니앱-FFCC00?style=flat-square&logo=kakaotalk&logoColor=black" />
</p>

KB IT's Your Life 7기 종합실무 프로젝트 · 5개 주제 중 **‘MZ세대를 위한 자산관리 서비스’** · **KB 국민은행 미니앱**으로 접근

---

## 📖 목차
- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [팀원 소개](#-팀원-소개)
- [폴더 구조](#-폴더-구조)
- [시작하기](#-시작하기)
- [컨벤션](#-컨벤션)
- [산출물 & 링크](#-산출물--링크)

## 🎯 프로젝트 소개

장병 적금 혜택 확대로 전역 시 **약 2,015만 원**의 목돈을 수령하는 장병이 급증하고 있으나, 이를 체계적으로 운용할 서비스는 부족합니다.
전역 직후 KB국민은행 이탈을 막기 위해, **개인 맞춤형 자금 운용 로드맵 + 정책상품 매칭**을 제공하는 금융 플랫폼을 구축합니다.

- **누구를 위해**: 현역 장병 · 전역 예비역 (MZ세대)
- **무엇을**: 군적금 만기금을 여행 / 자취 / 자동차 / 진로 목표에 맞춰 설계
- **어떻게**: 전역 D-Day 대시보드 · 적금 시뮬레이터 · 로드맵 자동 설계 · AI 재무 상담 챗봇

## ✨ 주요 기능

| 기능 | 설명 |
|---|---|
| 🎖 **전역 D-Day 대시보드** | 전역 카운트다운, 복무 달성률, 적금 현황, 휴가 관리(CRUD), 일급-지출 통계 |
| 🧮 **군적금 만기 시뮬레이터** | 예상 만기 수령액·중도해지 손실 계산, KB 예적금·청년정책 상품 추천 |
| 🗺 **전역 로드맵** | **여행 / 자취(월세) / 자동차 / 진로** 4개 카테고리별 목표 설계 + 금융상품·정책 매칭 |
| 🤖 **AI 재무 상담 챗봇** | 생성형 AI + RAG 기반 전역 후 목돈 활용 상담 (정책문서 출처 표기) |
| 🏅 **소셜 페이지** | 저축률·부대별 랭킹을 익명으로 비교해 경쟁 심리 부여 |
| 🧾 **후회 소비 회고** | 오픈뱅킹 소비내역 분석, 만족/후회 태깅, 월간 절감 목표를 KB 적금으로 연결 |

## 🛠 기술 스택

**Front-End**
<br>
![Vue.js](https://img.shields.io/badge/Vue.js_3-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white)
![Pinia](https://img.shields.io/badge/Pinia-FFD859?style=flat-square&logo=vue.js&logoColor=black)
![Bootstrap](https://img.shields.io/badge/Bootstrap_5-7952B3?style=flat-square&logo=bootstrap&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5A29E4?style=flat-square&logo=axios&logoColor=white)

**Back-End**
<br>
![Java](https://img.shields.io/badge/Java_17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/Spring_Legacy_5-6DB33F?style=flat-square&logo=spring&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-DC382D?style=flat-square)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)

**AI 챗봇** (별도 서버)
<br>
![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=flat-square&logo=fastapi&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=flat-square&logo=python&logoColor=white)
![Chroma](https://img.shields.io/badge/Chroma-vectorDB-FF6F61?style=flat-square)

**DB & 협업**
<br>
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=flat-square&logo=notion&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=flat-square&logo=discord&logoColor=white)

> ⚙️ 제약: Front-End는 **Vue.js**(React 불가), Back-End는 **Spring Legacy + MyBatis**(Spring Boot·JPA 불가)

## 🏗 시스템 아키텍처

```
┌─────────────┐      /api/**       ┌──────────────────┐      ┌─────────┐
│  Vue 3 (FE) │ ─────────────────▶ │ Spring Legacy(BE) │ ───▶ │  MySQL  │
│   미니앱     │                    │  JWT · MyBatis    │      └─────────┘
└──────┬──────┘                    └──────────────────┘           ▲
       │  /api/chat/**  (JWT 공유)                                 │
       └────────────────────────▶ ┌──────────────────┐  chat 3종  │
                                   │  FastAPI (챗봇)   │ ───────────┘
                                   │  LLM · RAG(Chroma)│
                                   └──────────────────┘
```
- 프론트는 일반 기능은 **Spring(8080)**, 챗봇만 **FastAPI(8000)** 로 직접 호출
- 두 서버가 **동일한 JWT(HS256)** 로 인증을 공유

## 👥 팀원 소개

<table>
  <tr>
    <td align="center"><a href="https://github.com/seokyunlee-dev"><img src="https://github.com/seokyunlee-dev.png?size=100" width="80" /><br/><b>이석윤</b></a><br/>팀장</td>
    <td align="center"><a href="https://github.com/hobin1"><img src="https://github.com/hobin1.png?size=100" width="80" /><br/><b>김호빈</b></a><br/>서기</td>
    <td align="center"><a href="https://github.com/jiwon415"><img src="https://github.com/jiwon415.png?size=100" width="80" /><br/><b>박지원</b></a><br/>수행일지</td>
    <td align="center"><a href="https://github.com/songeseudeo"><img src="https://github.com/songeseudeo.png?size=100" width="80" /><br/><b>송에스더</b></a><br/>Notion</td>
    <td align="center"><a href="https://github.com/sinsia27"><img src="https://github.com/sinsia27.png?size=100" width="80" /><br/><b>조수연</b></a><br/>Git</td>
    <td align="center"><a href="https://github.com/C7266"><img src="https://github.com/C7266.png?size=100" width="80" /><br/><b>조태석</b></a><br/>멘토링</td>
  </tr>
  <tr>
    <td align="center">D-Day 대시보드<br/>적금 시뮬레이터</td>
    <td align="center">회원정보<br/>로드맵·자동차</td>
    <td align="center">메인페이지<br/>로드맵·진로</td>
    <td align="center">AI 재무상담<br/>챗봇</td>
    <td align="center">로드맵·자취(월세)<br/>후회소비 회고</td>
    <td align="center">로드맵·여행<br/>소셜 페이지</td>
  </tr>
</table>

## 📁 폴더 구조

```
├── backend/    # Spring Legacy + MyBatis (Gradle 8.8) — :8080
│   └── src/main/
│       ├── java/org/scoula/
│       │   ├── config/     # Root/Servlet/Web/Swagger 설정
│       │   ├── security/   # JWT 인증 (로그인)
│       │   ├── common/     # BaseVO(감사컬럼)·pagination·util
│       │   ├── exception/  # 공통 예외 처리
│       │   ├── member/     # 회원/인증 (/api/users)
│       │   └── <도메인>/   # main·dashboard·simulator·saving·product·roadmap·travel·rent·car·job
│       │                   #  ·regret·openbanking·social·bookmark  (15개)
│       └── resources/sql/  # kb-schema.sql · kb-data.sql (DB 스크립트)
├── frontend/   # Vue 3 + Vite — :5173
│   └── src/{pages,router,api,stores,components,config}/
└── chatbot/    # FastAPI (AI 챗봇, Python) — :8000
    └── app/{core,models,schemas,routers}/
```
> - 각 도메인은 `{controller,service,mapper,domain,dto}` 골격 상태 → 담당자가 구현
> - **챗봇은 같은 저장소의 별도 폴더**이지만 **독립 서버**로 실행됩니다 (실행법은 [chatbot/README.md](chatbot/README.md))

## 🚀 시작하기

### 1️⃣ DB (최초 1회)
```bash
# DB 생성
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS scoula_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 스키마 → 데이터 순서로 실행 (backend/src/main/resources/sql/ 에서)
mysql -u root -p scoula_db < kb-schema.sql
mysql -u root -p scoula_db < kb-data.sql
```
> 계정 설정·Workbench 사용법·확인 쿼리는 [sql/README.md](backend/src/main/resources/sql/README.md) 참고

### 2️⃣ Back-End
```bash
# DB 접속 정보 확인: backend/src/main/resources/application.properties
# IntelliJ 외장 Tomcat(Local) 으로 backend:war exploded 실행 (포트 8080)
gradlew.bat compileJava   # 컴파일 확인 (Windows)
```

### 3️⃣ Front-End
```bash
cd frontend
npm install
npm run dev     # /api → :8080(Spring), /api/chat → :8000(FastAPI) 프록시
npm run build   # 배포 빌드 → backend/src/main/webapp/resources
```

### 4️⃣ Chatbot (선택 — 챗봇 기능 사용 시)
```bash
cd chatbot
python -m venv venv && venv\Scripts\activate   # Windows
pip install -r requirements.txt
copy .env.example .env                          # DB 정보 입력
uvicorn app.main:app --reload --port 8000
```
> 자세한 설정·API 목록은 [chatbot/README.md](chatbot/README.md) 참고

## 📐 컨벤션

개발 규칙은 **[CONVENTIONS.md](CONVENTIONS.md)** 참고 (팀 합의 최종본)
- 브랜치: **개인 브랜치(`dev_이름`) → dev → main** · PR 리뷰 1명 이상
- 커밋: `❇️[feat]` `🐞[fix]` `💄[style]` `🔨[refactor]` `🔧[chore]` `📝[docs]` `🔍[test]` `🚧[setting]`
- 라우트는 **name 방식**, 인증 화면은 `meta: { requiresAuth: true }`

## 📚 산출물 & 링크

- 📔 **Notion (팀 페이지 · 기획안 · ERD · API 명세서)**: https://app.notion.com/p/KB-IT-s-Your-Life-7-39968f2586e8819bb300ecdc8beef069
- 🗄 DB 스크립트: `backend/src/main/resources/sql/` — `kb-schema.sql`(52개 테이블) + `kb-data.sql`(테스트 데이터). 실행법은 [sql/README.md](backend/src/main/resources/sql/README.md) 참고

---

<p align="center">📅 개발 기간: 2026.07.09 ~ 2026.08.25 · KB IT's Your Life 7기 30반 4팀</p>
