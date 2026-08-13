import asyncio
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.exceptions import BusinessException, business_exception_handler
from app.routers import chat
from app.services import fss, fund, vectorstore

# fss/fund 캐시 TTL(1시간)이 지나기 전에 미리 갱신해서, 사용자가 캐시 만료 순간의 콜드 조회를
# 직접 겪지 않게 한다(2026-08-11 fss, 2026-08-12 fund 추가). TTL보다 여유 있게 짧은 주기로 돈다.
_CACHE_WARM_INTERVAL_SECONDS = 3000  # 50분


def _warm_all_caches():
    fss.warm_cache()
    fund.warm_cache()


async def _warm_cache_periodically():
    while True:
        await asyncio.sleep(_CACHE_WARM_INTERVAL_SECONDS)
        await asyncio.to_thread(_warm_all_caches)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 이미 인덱싱돼 있으면 건너뛴다 (vectorstore.build_index 내부에서 처리).
    vectorstore.build_index()

    # 서버 기동 시 캐시를 미리 채워둔다 - 카테고리 5개를 순서대로 실시간 조회하면 최대 몇 분까지
    # 걸릴 수 있어서, await로 기다리면 그동안 서버가 요청을 하나도 못 받는다(기동 자체가 막힘).
    # create_task로 백그라운드에 던져두고 기동은 바로 끝낸다 - 이 예열이 끝나기 전에 들어오는
    # 요청만 기존처럼 콜드 조회를 겪고, 그 뒤로는(재기동 전까지) 항상 캐시가 채워져 있다.
    asyncio.create_task(asyncio.to_thread(_warm_all_caches))
    warm_task = asyncio.create_task(_warm_cache_periodically())

    yield

    warm_task.cancel()


app = FastAPI(title="KB 챗봇 API", lifespan=lifespan)

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

app.add_exception_handler(BusinessException, business_exception_handler)

app.include_router(chat.router)


@app.get("/health")
def health_check():
    return {"status": "ok"}
