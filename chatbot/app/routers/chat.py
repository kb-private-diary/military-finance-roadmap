import logging

from datetime import date, datetime

from typing import List, Optional

from fastapi import APIRouter, Depends, Path, Query
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.core.auth import get_current_admin_user_id, get_current_user_id
from app.core.db import get_db
from app.core.exceptions import BusinessException
from app.models.chat import ChatFeedback, ChatMessage, ChatSession
from app.schemas.chat import (
    FeedbackCreateRequest,
    FeedbackItem,
    GlossaryDetail,
    GlossaryItem,
    MessageCreateRequest,
    MessageItem,
    RecommendationItem,
    ReindexResponse,
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
from app.services import cheongyakhome, fss, gemini, policy_docs, vectorstore
from app.services import fund as fund_service

router = APIRouter(prefix="/api/chat", tags=["chat"])
logger = logging.getLogger(__name__)

MESSAGE_MAX_LENGTH = 500
GEMINI_FAILURE_MESSAGE = "서버에 문제가 발생했습니다. 잠시 후에 다시 시도해 주세요."
FEEDBACK_VALUES = ("like", "neutral", "dislike")
HISTORY_LIMIT = 6  # 최근 메시지 몇 개까지 멀티턴 문맥으로 넘길지 (3턴치)

TOPICS = [
    TopicItem(topic_id="fund_consult", label="목돈상담"),
    TopicItem(topic_id="savings_subscription", label="적금청약질문"),
    TopicItem(topic_id="policy_terms", label="정책용어"),
    TopicItem(topic_id="free_input", label="자유입력"),
]

# CHAT-002: 대화 세션 관리 API
@router.post("/sessions", response_model=SessionResponse)
def create_session(
    payload: SessionCreateRequest,
    current_user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    today_session = (
        db.query(ChatSession)
        .filter(
            ChatSession.user_id == current_user_id,
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
        user_id=current_user_id,
        title=payload.title,
        created_date=datetime.now(),
        created_nm=str(current_user_id),
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
def list_sessions(current_user_id: int = Depends(get_current_user_id), db: Session = Depends(get_db)):
    sessions = (
        db.query(ChatSession)
        .filter(ChatSession.user_id == current_user_id, ChatSession.del_yn == "N")
        .order_by(ChatSession.created_date.desc())
        .all()
    )
    return sessions


@router.get("/history/{sessionId}", response_model=List[MessageItem])
def get_history(
    session_id: int = Path(..., alias="sessionId"),
    current_user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    session = (
        db.query(ChatSession)
        .filter(ChatSession.session_id == session_id, ChatSession.del_yn == "N")
        .first()
    )
    if not session:
        raise BusinessException("세션을 찾을 수 없습니다", 404, "CHAT_001")
    if session.user_id != current_user_id:
        raise BusinessException("본인의 세션만 조회할 수 있습니다", 403, "AUTH_004")

    messages = (
        db.query(ChatMessage)
        .filter(ChatMessage.session_id == session_id, ChatMessage.del_yn == "N")
        .order_by(ChatMessage.created_date.asc())
        .all()
    )
    return messages


# CHAT-004: 사용자 질문 입력 처리
@router.post("/messages", response_model=MessageItem)
def send_message(
    payload: MessageCreateRequest,
    current_user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    content = payload.content.strip()
    if not content:
        raise BusinessException("질문을 입력해주세요", 400, "CHAT_002")
    if len(content) > MESSAGE_MAX_LENGTH:
        raise BusinessException(f"질문은 {MESSAGE_MAX_LENGTH}자 이내로 입력해주세요", 400, "CHAT_003")

    session = (
        db.query(ChatSession)
        .filter(ChatSession.session_id == payload.session_id, ChatSession.del_yn == "N")
        .first()
    )
    if not session:
        raise BusinessException("세션을 찾을 수 없습니다", 404, "CHAT_001")
    if session.user_id != current_user_id:
        raise BusinessException("본인의 세션에만 질문할 수 있습니다", 403, "AUTH_004")

    recent_messages = (
        db.query(ChatMessage)
        .filter(ChatMessage.session_id == payload.session_id, ChatMessage.del_yn == "N")
        .order_by(ChatMessage.created_date.desc())
        .limit(HISTORY_LIMIT)
        .all()
    )
    history = [(m.role, m.content) for m in reversed(recent_messages)]

    user_message = ChatMessage(
        session_id=payload.session_id,
        role="user",
        content=content,
        created_date=datetime.now(),
        created_nm=str(session.user_id),
    )
    db.add(user_message)
    db.commit()

    try:
        reply, source, source_detail, is_ai_generated, intent = gemini.generate_reply(
            content, history=history, force_intent="info" if payload.force_info else None
        )
    except Exception:
        logger.exception("Gemini 응답 생성 실패 (session_id=%s)", payload.session_id)
        reply, source, source_detail, is_ai_generated, intent = GEMINI_FAILURE_MESSAGE, "오류 안내", None, False, "info"

    bot_message = ChatMessage(
        session_id=payload.session_id,
        role="bot",
        content=reply,
        source=source,
        source_detail=source_detail,
        is_ai_generated=is_ai_generated,
        created_date=datetime.now(),
        created_nm=str(session.user_id),
    )
    db.add(bot_message)
    db.commit()
    db.refresh(bot_message)
    # intent는 DB에 저장하지 않는 응답 전용 값 - 프론트가 상담형 되묻기로 분기할지 판단하는 용도
    bot_message.intent = intent
    return bot_message


# CHAT-005: 초기 카테고리 메뉴 및 FAQ/상품/용어
@router.get("/topics", response_model=List[TopicItem])
def get_topics():
    return TOPICS


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
            raise BusinessException("지원하지 않는 카테고리입니다", 400, "CHAT_004")

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
        raise BusinessException("지원하지 않는 카테고리입니다", 400, "CHAT_004")

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

    raise BusinessException("상품을 찾을 수 없습니다", 404, "CHAT_005")


@router.get("/glossary", response_model=List[GlossaryItem])
def list_glossary():
    return [GlossaryItem(term=entry["section"]) for entry in policy_docs.list_glossary_terms()]


@router.get("/glossary/{term}", response_model=GlossaryDetail)
def get_glossary_term(term: str):
    entry = policy_docs.find_glossary_term(term)
    if not entry:
        raise BusinessException("용어를 찾을 수 없습니다", 404, "CHAT_006")
    return GlossaryDetail(term=entry["section"], definition=entry["text"].split("\n", 1)[1])


# CHAT-006: 답변 만족도 피드백 저장 API
@router.post("/feedback", response_model=FeedbackItem)
def create_feedback(
    payload: FeedbackCreateRequest,
    current_user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    session = (
        db.query(ChatSession)
        .filter(ChatSession.session_id == payload.session_id, ChatSession.del_yn == "N")
        .first()
    )
    if not session:
        raise BusinessException("세션을 찾을 수 없습니다", 404, "CHAT_001")
    if session.user_id != current_user_id:
        raise BusinessException("본인의 세션에만 피드백을 남길 수 있습니다", 403, "AUTH_004")

    if payload.feedback not in FEEDBACK_VALUES:
        raise BusinessException("feedback 값은 like, neutral, dislike 중 하나여야 합니다", 400, "CHAT_007")

    if payload.message_id is not None:
        message = (
            db.query(ChatMessage)
            # session_id가 사용자가 보낸 값이랑 같고, 삭제 여부가 N인 것을 필터링
            .filter(
                ChatMessage.message_id == payload.message_id,
                ChatMessage.session_id == payload.session_id,
                ChatMessage.del_yn == "N",
            )
            .first()
        )
        if not message:
            raise BusinessException("해당 세션의 메시지를 찾을 수 없습니다", 404, "CHAT_008")

    feedback = ChatFeedback(
        session_id=payload.session_id,
        message_id=payload.message_id,
        feedback=payload.feedback,
        reason=payload.reason,
        created_date=datetime.now(),
        created_nm=str(session.user_id),
    )
    db.add(feedback)
    db.commit()
    db.refresh(feedback)
    return feedback


# RAG-009: 답변 관련 콘텐츠 추천
@router.get("/messages/{messageId}/recommendations", response_model=List[RecommendationItem])
def get_recommendation(
    message_id: int = Path(..., alias="messageId"),
    current_user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    message = (
        db.query(ChatMessage)
        .filter(ChatMessage.message_id == message_id, ChatMessage.del_yn == "N")
        .first()
    )
    if not message:
        raise BusinessException("메시지를 찾을 수 없습니다", 404, "CHAT_009")

    session = (
        db.query(ChatSession)
        .filter(ChatSession.session_id == message.session_id, ChatSession.del_yn == "N")
        .first()
    )
    if not session or session.user_id != current_user_id:
        raise BusinessException("본인의 세션 메시지만 조회할 수 있습니다", 403, "AUTH_004")

    return gemini.get_recommendation(message.source)


# 관리자 전용: 정책 문서 재인덱싱 트리거
@router.post("/admin/reindex", response_model=ReindexResponse)
def reindex_policy_docs(current_admin_user_id: int = Depends(get_current_admin_user_id)):
    count = vectorstore.build_index(force=True)
    return ReindexResponse(reindexed_chunks=count)
