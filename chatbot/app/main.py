from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers import chat

app = FastAPI(title="KB 챗봇 API")

# CORS: credentials=True 일 때는 origin 을 "*" 로 둘 수 없어 프론트 주소를 명시한다.
# 배포 시 실제 프론트 도메인을 ALLOWED_ORIGINS 에 추가할 것.
ALLOWED_ORIGINS = [
    "http://localhost:5173",  # Vite dev 서버 (프론트)
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat.router)


@app.get("/health")
def health_check():
    return {"status": "ok"}
