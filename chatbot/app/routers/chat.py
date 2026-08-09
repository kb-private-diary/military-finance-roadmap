import logging

from datetime import datetime

from typing import List, Optional

from fastapi import APIRouter, Depends, Path, Query
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
    LogMessageRequest,
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
    LoanDetail,
    LoanItem,
    ProductDetail,
    ProductItem,
    SubscriptionDetail,
    SubscriptionItem,
)
from app.services import cheongyakhome, fss, gemini, pii_filter, policy_docs, vectorstore
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
# 유저당 세션을 하루에 하나씩 새로 만들지 않고 계속 재사용한다 - 대화가 날짜로 안 끊기고
# 하나로 이어지는 게 맞다는 팀 결정(2026-08-06)에 따름. "이전 기록"은 세션을 여러 개 골라보는
# 기능이 아니라, 이미 하나로 합쳐진 대화 안에서 날짜로 스크롤 이동하는 용도로 바뀜(프론트 담당).
@router.post("/sessions", response_model=SessionResponse)
def create_session(
    payload: SessionCreateRequest,
    current_user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    existing_session = (
        db.query(ChatSession)
        .filter(ChatSession.user_id == current_user_id, ChatSession.del_yn == "N")
        .order_by(ChatSession.created_date.desc())
        .first()
    )
    if existing_session:
        return SessionResponse(
            session_id=existing_session.session_id,
            user_id=existing_session.user_id,
            title=existing_session.title,
            created_date=existing_session.created_date,
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


# 유저가 그동안 나눈 대화 전체를 세션 구분 없이 다 합쳐서 날짜순으로 돌려준다.
# 지금은 세션을 유저당 하나만 재사용하지만, 예전에(하루 단위로 세션을 나누던 시절에) 생긴
# 계정은 세션이 여러 개로 흩어져 있을 수 있어서 - 그것까지 다 합쳐야 대화가 하나로 이어져 보인다.
# 정적 경로(/history)라 동적 경로(/history/{sessionId})보다 먼저 선언해야 라우팅이 꼬이지 않는다.
@router.get("/history", response_model=List[MessageItem])
def get_all_history(current_user_id: int = Depends(get_current_user_id), db: Session = Depends(get_db)):
    session_ids = [
        row.session_id
        for row in db.query(ChatSession.session_id)
        .filter(ChatSession.user_id == current_user_id, ChatSession.del_yn == "N")
        .all()
    ]
    if not session_ids:
        return []
    messages = (
        db.query(ChatMessage)
        .filter(ChatMessage.session_id.in_(session_ids), ChatMessage.del_yn == "N")
        .order_by(ChatMessage.created_date.asc())
        .all()
    )
    return messages


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
    # 주민번호·카드번호·전화번호·계좌번호로 보이는 패턴은 마스킹한 뒤 저장 및 Gemini 전달에 쓴다 -
    # 사용자가 실수로 자기 정보를 그대로 입력해도 DB에 원문 그대로 남거나 외부 API로 넘어가지 않게
    # 여기서 한 번만 치환해두면 아래 모든 흐름(저장·히스토리·AI 호출)에 자동으로 적용된다(2026-08-09).
    content = pii_filter.mask_pii(content)

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
        reply, source, source_detail, is_ai_generated, intent, source_url = gemini.generate_reply(
            content,
            history=history,
            force_intent="info" if payload.force_info else None,
            product_context=payload.product_context,
        )
    except Exception:
        logger.exception("Gemini 응답 생성 실패 (session_id=%s)", payload.session_id)
        reply, source, source_detail, is_ai_generated, intent, source_url = (
            GEMINI_FAILURE_MESSAGE, "오류 안내", None, False, "info", None,
        )

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
    # intent·source_url은 DB에 저장하지 않는 응답 전용 값 - intent는 상담형 되묻기 분기용,
    # source_url은 그 자리에서만 링크를 붙여주면 되고 히스토리 조회 시엔 다시 안 씀
    bot_message.intent = intent
    bot_message.source_url = source_url
    return bot_message


# 버튼으로 진행하는 되묻기·상품 목록 등 화면 전용 대화 턴을 그대로 기록한다.
# AI 호출이 없어서(Gemini 미사용) /messages보다 훨씬 가볍고, 새로고침·재로그인 후에도
# 대화 내용이 안 사라지게 하려는 용도 - 응답 생성 없이 그냥 저장만 한다.
@router.post("/messages/log", response_model=MessageItem)
def log_message(
    payload: LogMessageRequest,
    current_user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    if payload.role not in ("user", "bot"):
        raise BusinessException("role은 user 또는 bot이어야 합니다", 400, "CHAT_006")
    content = payload.content.strip()
    if not content:
        raise BusinessException("내용이 비어 있습니다", 400, "CHAT_007")

    session = (
        db.query(ChatSession)
        .filter(ChatSession.session_id == payload.session_id, ChatSession.del_yn == "N")
        .first()
    )
    if not session:
        raise BusinessException("세션을 찾을 수 없습니다", 404, "CHAT_001")
    if session.user_id != current_user_id:
        raise BusinessException("본인의 세션에만 기록할 수 있습니다", 403, "AUTH_004")

    message = ChatMessage(
        session_id=payload.session_id,
        role=payload.role,
        content=content,
        created_date=datetime.now(),
        created_nm=str(current_user_id),
    )
    db.add(message)
    db.commit()
    db.refresh(message)
    return message


# CHAT-005: 초기 카테고리 메뉴 및 FAQ/상품/용어
@router.get("/topics", response_model=List[TopicItem])
def get_topics():
    return TOPICS


# insurance는 실시간 API가 없는 고정(RAG 문서) 카테고리라 여기 목록 API에는 안 걸림 -
# 프론트가 전부 openDoc/RAG 검색으로만 처리한다. 그래도 /messages의 product_context 흐름과
# 카테고리 검증(ALL_CATEGORIES)에는 걸려야 해서 이름만 포함시켜둔다.
LOAN_CATEGORIES = ["mortgage", "jeonse", "creditLoan"]
ALL_CATEGORIES = ["savings", "deposit", "subscription", "investment", *LOAN_CATEGORIES, "insurance"]


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


def _loan_items(category: str) -> List[dict]:
    return [
        LoanItem(
            fin_prdt_cd=product["fin_prdt_cd"],
            kor_co_nm=product["kor_co_nm"],
            fin_prdt_nm=product["fin_prdt_nm"],
            rate=fss.representative_loan_rate(product),
        ).model_dump(by_alias=True)
        for product in fss.fetch_loan_products(category)
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
        elif c in LOAN_CATEGORIES:
            items.extend(_loan_items(c))
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
                    source_url=fss.SOURCE_URL.get(category),
                ).model_dump(by_alias=True)
    return None


def _find_loan_detail(name: str, categories: List[str]):
    for category in categories:
        for product in fss.fetch_loan_products(category):
            if product["fin_prdt_nm"] == name:
                return LoanDetail(
                    fin_prdt_cd=product["fin_prdt_cd"],
                    kor_co_nm=product["kor_co_nm"],
                    fin_prdt_nm=product["fin_prdt_nm"],
                    join_way=product["join_way"],
                    loan_lmt=product.get("loan_lmt"),
                    erly_rpay_fee=product.get("erly_rpay_fee"),
                    cb_name=product.get("cb_name"),
                    options=product["options"],
                    source=fss.SOURCE_LABEL,
                    source_url=fss.SOURCE_URL.get(category),
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

    loan_categories = [category] if category in LOAN_CATEGORIES else (LOAN_CATEGORIES if category is None else [])
    if loan_categories:
        result = _find_loan_detail(name, loan_categories)
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
