from datetime import datetime
from typing import Optional

from app.schemas.base import CamelModel


class SessionCreateRequest(CamelModel):
    user_id: int  # TODO: JWT 연동 후 토큰에서 추출하는 방식으로 교체, 그 전까지는 임시로 body에서 받음
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
    is_ai_generated: bool = False
    created_date: datetime


class MessageCreateRequest(CamelModel):
    session_id: int
    content: str


class TopicItem(CamelModel):
    topic_id: str
    label: str


class FaqCategoryItem(CamelModel):
    category_id: str
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
