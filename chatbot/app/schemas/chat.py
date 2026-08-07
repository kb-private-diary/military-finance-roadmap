from datetime import datetime
from typing import Optional

from app.schemas.base import CamelModel


class SessionCreateRequest(CamelModel):
    title: Optional[str] = None


class SessionResponse(CamelModel):
    session_id: int
    user_id: int
    title: Optional[str]
    created_date: datetime
    is_new: bool  # 오늘자 세션을 새로 만들었는지, 기존 세션을 재사용했는지


class SessionListItem(CamelModel):
    session_id: int
    title: Optional[str]
    created_date: datetime


class MessageItem(CamelModel):
    message_id: int
    role: str
    content: str
    source: Optional[str]
    source_detail: Optional[str] = None
    source_url: Optional[str] = None  # 출처를 클릭해서 실제 상품 페이지로 이동 (DB엔 저장 안 함, 응답 전용)
    is_ai_generated: bool = False
    intent: Optional[str] = None
    created_date: datetime


class MessageCreateRequest(CamelModel):
    session_id: int
    content: str
    # true면 의도분류(classify_intent)를 건너뛰고 무조건 정보성 질문으로 처리한다.
    # 자주 묻는 질문처럼 미리 큐레이션된 질문이 상담 등으로 잘못 튀지 않게 할 때 사용.
    force_info: bool = False
    # 실시간 상품(적금/예금/투자/청약/대출) 상세를 보고 나서 그 상품에 대해 후속 질문을 할 때만 채운다.
    # 프론트가 이미 그 상품의 상세 정보(가입방법·조건 등)를 화면에 표시하려고 받아둔 텍스트를 그대로
    # 실어 보내서, 백엔드가 카테고리 전체가 아니라 "이 상품 하나"만 근거로 답하게 한다(2026-08-07).
    product_context: Optional[str] = None


class LogMessageRequest(CamelModel):
    """버튼으로 진행하는 되묻기·상품 목록 등 - AI 호출 없이 화면에 이미 정해진 문구를 그대로 기록만 할 때 사용.
    (자유 질문은 /messages가 Gemini 응답까지 함께 처리하므로 여긴 안 씀)"""
    session_id: int
    role: str  # 'user' | 'bot'
    content: str


class TopicItem(CamelModel):
    topic_id: str
    label: str


class GlossaryItem(CamelModel):
    term: str


class GlossaryDetail(CamelModel):
    term: str
    definition: str


class FeedbackCreateRequest(CamelModel):
    session_id: int
    message_id: Optional[int] = None  # 특정 메시지에 대한 피드백이면 지정, 세션 단위면 생략
    feedback: str  # "like" 또는 "dislike"
    reason: Optional[str] = None  # dislike 사유 등 (선택)


class FeedbackItem(CamelModel):
    feedback_id: int
    session_id: int
    message_id: Optional[int]
    feedback: str
    reason: Optional[str]
    created_date: datetime


class RecommendationItem(CamelModel):
    label: str
    page_link: str


class ReindexResponse(CamelModel):
    reindexed_chunks: int
