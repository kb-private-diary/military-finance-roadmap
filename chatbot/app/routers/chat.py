from datetime import date, datetime

from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Path, Query
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.core.db import get_db
from app.models.chat import ChatMessage, ChatSession
from app.schemas.chat import (
    FaqCategoryItem,
    GlossaryDetail,
    GlossaryItem,
    MessageCreateRequest,
    MessageItem,
    SessionCreateRequest,
    SessionListItem,
    SessionResponse,
    TopicItem,
)
from app.schemas.product import (
    FundDetail,
    FundItem,
    ProductDetail,
    ProductItem,
    SubscriptionDetail,
    SubscriptionItem,
)
from app.services import cheongyakhome, fss, gemini, policy_docs
from app.services import fund as fund_service

router = APIRouter(prefix="/api/chat", tags=["chat"])

MESSAGE_MAX_LENGTH = 500

TOPICS = [
    TopicItem(topic_id="fund_consult", label="목돈상담"),
    TopicItem(topic_id="savings_subscription", label="적금청약질문"),
    TopicItem(topic_id="policy_terms", label="정책용어"),
    TopicItem(topic_id="free_input", label="자유입력"),
]

FAQ_CATEGORIES = [
    FaqCategoryItem(category_id="savings", label="적금"),
    FaqCategoryItem(category_id="subscription", label="청약"),
    FaqCategoryItem(category_id="deposit", label="예금"),
    FaqCategoryItem(category_id="investment", label="투자"),
]


# CHAT-002: 대화 세션 관리 API
@router.post("/sessions", response_model=SessionResponse)
def create_session(payload: SessionCreateRequest, db: Session = Depends(get_db)):
    today_session = (
        db.query(ChatSession)
        .filter(
            ChatSession.user_id == payload.user_id,
            ChatSession.del_yn == "N",
            func.date(ChatSession.created_date) == date.today(),
        )
        .order_by(ChatSession.created_date.desc())
        .first()
    )
    if today_session:
        return SessionResponse(
            session_id=today_session.session_id,
            user_id=today_session.user_id,
            title=today_session.title,
            created_date=today_session.created_date,
            is_new=False,
        )

    new_session = ChatSession(
        user_id=payload.user_id,
        title=payload.title,
        created_date=datetime.now(),
        created_nm=str(payload.user_id),
    )
    db.add(new_session)
    db.commit()
    db.refresh(new_session)
    return SessionResponse(
        session_id=new_session.session_id,
        user_id=new_session.user_id,
        title=new_session.title,
        created_date=new_session.created_date,
        is_new=True,
    )


# CHAT-003: 대화 히스토리 저장/조회 API
@router.get("/sessions", response_model=List[SessionListItem])
def list_sessions(user_id: int, db: Session = Depends(get_db)):  # TODO: JWT 연동 후 query param 대신 토큰에서 추출
    sessions = (
        db.query(ChatSession)
        .filter(ChatSession.user_id == user_id, ChatSession.del_yn == "N")
        .order_by(ChatSession.created_date.desc())
        .all()
    )
    return sessions


@router.get("/history/{sessionId}", response_model=List[MessageItem])
def get_history(session_id: int = Path(..., alias="sessionId"), db: Session = Depends(get_db)):
    session = (
        db.query(ChatSession)
        .filter(ChatSession.session_id == session_id, ChatSession.del_yn == "N")
        .first()
    )
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")

    messages = (
        db.query(ChatMessage)
        .filter(ChatMessage.session_id == session_id, ChatMessage.del_yn == "N")
        .order_by(ChatMessage.created_date.asc())
        .all()
    )
    return messages


# CHAT-004: 사용자 질문 입력 처리
@router.post("/messages", response_model=MessageItem)
def send_message(payload: MessageCreateRequest, db: Session = Depends(get_db)):
    content = payload.content.strip()
    if not content:
        raise HTTPException(status_code=400, detail="질문을 입력해주세요")
    if len(content) > MESSAGE_MAX_LENGTH:
        raise HTTPException(status_code=400, detail=f"질문은 {MESSAGE_MAX_LENGTH}자 이내로 입력해주세요")

    session = (
        db.query(ChatSession)
        .filter(ChatSession.session_id == payload.session_id, ChatSession.del_yn == "N")
        .first()
    )
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")

    user_message = ChatMessage(
        session_id=payload.session_id,
        role="user",
        content=content,
        created_date=datetime.now(),
        created_nm=str(session.user_id),
    )
    db.add(user_message)
    db.commit()

    reply, source = gemini.generate_reply(content)

    bot_message = ChatMessage(
        session_id=payload.session_id,
        role="bot",
        content=reply,
        source=source,
        created_date=datetime.now(),
        created_nm=str(session.user_id),
    )
    db.add(bot_message)
    db.commit()
    db.refresh(bot_message)
    return bot_message


# CHAT-005: 초기 카테고리 메뉴 및 FAQ/상품/용어
@router.get("/topics", response_model=List[TopicItem])
def get_topics():
    return TOPICS


@router.get("/faq-categories", response_model=List[FaqCategoryItem])
def get_faq_categories():
    return FAQ_CATEGORIES


ALL_CATEGORIES = ["savings", "deposit", "subscription", "investment"]


def _fss_items(category: str) -> List[dict]:
    return [
        ProductItem(
            fin_prdt_cd=product["fin_prdt_cd"],
            kor_co_nm=product["kor_co_nm"],
            fin_prdt_nm=product["fin_prdt_nm"],
            max_rate=fss.max_rate(product),
        ).model_dump(by_alias=True)
        for product in fss.fetch_products(category)
    ]


def _subscription_items() -> List[dict]:
    return [
        SubscriptionItem(
            house_manage_no=listing["HOUSE_MANAGE_NO"],
            house_nm=listing["HOUSE_NM"],
            rcept_bgnde=listing.get("RCEPT_BGNDE"),
            rcept_endde=listing.get("RCEPT_ENDDE"),
            mvn_prearnge_ym=listing.get("MVN_PREARNGE_YM"),
        ).model_dump(by_alias=True)
        for listing in cheongyakhome.fetch_listings()
    ]


def _fund_items() -> List[dict]:
    return [
        FundItem(
            srtn_cd=fund["srtnCd"],
            fnd_nm=fund["fndNm"],
            fnd_tp=fund.get("fndTp"),
        ).model_dump(by_alias=True)
        for fund in fund_service.fetch_funds()
    ]


@router.get("/products")
def list_products(category: Optional[str] = Query(default=None)):
    categories = [category] if category else ALL_CATEGORIES
    for c in categories:
        if c not in ALL_CATEGORIES:
            raise NotImplementedError

    items: List[dict] = []
    for c in categories:
        if c in fss.PRODUCT_ENDPOINTS:
            items.extend(_fss_items(c))
        elif c == "subscription":
            items.extend(_subscription_items())
        elif c == "investment":
            items.extend(_fund_items())
    return items


def _find_fss_detail(name: str, categories: List[str]):
    for category in categories:
        for product in fss.fetch_products(category):
            if product["fin_prdt_nm"] == name:
                return ProductDetail(
                    fin_prdt_cd=product["fin_prdt_cd"],
                    kor_co_nm=product["kor_co_nm"],
                    fin_prdt_nm=product["fin_prdt_nm"],
                    join_way=product["join_way"],
                    join_member=product["join_member"],
                    spcl_cnd=product["spcl_cnd"],
                    etc_note=product["etc_note"],
                    options=product["options"],
                    source=fss.SOURCE_LABEL,
                ).model_dump(by_alias=True)
    return None


@router.get("/products/{name}")
def get_product(name: str, category: Optional[str] = Query(default=None)):
    """category를 알고 있으면 반드시 넘길 것 — 생략하면 전체 카테고리를 순차 조회해서 훨씬 느려짐."""
    if category is not None and category not in ALL_CATEGORIES:
        raise NotImplementedError

    fss_categories = [category] if category in fss.PRODUCT_ENDPOINTS else (
        list(fss.PRODUCT_ENDPOINTS) if category is None else []
    )
    if fss_categories:
        result = _find_fss_detail(name, fss_categories)
        if result:
            return result

    if category in (None, "subscription"):
        listing = cheongyakhome.find_listing(name)
        if listing:
            return SubscriptionDetail(
                house_manage_no=listing["HOUSE_MANAGE_NO"],
                house_nm=listing["HOUSE_NM"],
                hssply_adres=listing.get("HSSPLY_ADRES"),
                rcept_bgnde=listing.get("RCEPT_BGNDE"),
                rcept_endde=listing.get("RCEPT_ENDDE"),
                przwner_presnatn_de=listing.get("PRZWNER_PRESNATN_DE"),
                mvn_prearnge_ym=listing.get("MVN_PREARNGE_YM"),
                pblanc_url=listing.get("PBLANC_URL"),
                source=cheongyakhome.SOURCE_LABEL,
            ).model_dump(by_alias=True)

    fund = fund_service.find_fund(name) if category in (None, "investment") else None
    if fund:
        return FundDetail(
            srtn_cd=fund["srtnCd"],
            fnd_nm=fund["fndNm"],
            ctg=fund.get("ctg"),
            setp_dt=fund.get("setpDt"),
            fnd_tp=fund.get("fndTp"),
            source=fund_service.SOURCE_LABEL,
        ).model_dump(by_alias=True)

    raise HTTPException(status_code=404, detail="Product not found")


@router.get("/glossary", response_model=List[GlossaryItem])
def list_glossary():
    return [GlossaryItem(term=entry["section"]) for entry in policy_docs.list_glossary_terms()]


@router.get("/glossary/{term}", response_model=GlossaryDetail)
def get_glossary_term(term: str):
    entry = policy_docs.find_glossary_term(term)
    if not entry:
        raise HTTPException(status_code=404, detail="Term not found")
    return GlossaryDetail(term=entry["section"], definition=entry["text"].split("\n", 1)[1])


# CHAT-006: 답변 만족도 피드백 저장 API
@router.post("/feedback")
def create_feedback():
    raise NotImplementedError


# RAG-009: 답변 관련 콘텐츠 추천
@router.get("/messages/{messageId}/recommendations")
def get_recommendation(message_id: int = Path(..., alias="messageId")):
    raise NotImplementedError
