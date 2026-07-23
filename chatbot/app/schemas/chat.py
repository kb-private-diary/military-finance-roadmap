from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel


class CamelModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True, from_attributes=True)


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
