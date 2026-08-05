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
    is_ai_generated: bool = False
    intent: Optional[str] = None
    created_date: datetime


class MessageCreateRequest(CamelModel):
    session_id: int
    content: str
    # true면 의도분류(classify_intent)를 건너뛰고 무조건 정보성 질문으로 처리한다.
    # 자주 묻는 질문처럼 미리 큐레이션된 질문이 상담 등으로 잘못 튀지 않게 할 때 사용.
    force_info: bool = False


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
