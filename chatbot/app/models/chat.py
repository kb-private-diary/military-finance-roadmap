from sqlalchemy import BigInteger, Boolean, Column, String, Text, DateTime, CHAR, ForeignKey
from sqlalchemy.sql import func

from app.core.db import Base


class ChatSession(Base):
    __tablename__ = "chat_session"

    session_id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, nullable=False)
    title = Column(String(200), nullable=True)
    created_date = Column(DateTime, nullable=False, server_default=func.now())
    created_nm = Column(String(50), nullable=False)
    modified_date = Column(DateTime, nullable=True, onupdate=func.now())
    modified_nm = Column(String(50), nullable=True)
    del_yn = Column(CHAR(1), nullable=False, default="N")


class ChatMessage(Base):
    __tablename__ = "chat_message"

    message_id = Column(BigInteger, primary_key=True, autoincrement=True)
    session_id = Column(BigInteger, ForeignKey("chat_session.session_id"), nullable=False)
    role = Column(String(10), nullable=False)
    content = Column(Text, nullable=False)
    source = Column(String(100), nullable=True)
    source_detail = Column(String(200), nullable=True)  # 사람이 읽는 출처 캡션 (RAG 답변만, 예: "KB국민은행 상품안내 · 장병내일준비적금 (2026년 3월 기준)")
    is_ai_generated = Column(Boolean, nullable=False, default=False)  # 프론트에 "AI가 생성한 답변입니다" 문구 표시 여부
    langfuse_trace_id = Column(String(100), nullable=True)  # Langfuse 트레이스 id (봇 답변만, 나중에 피드백을 같은 trace에 점수로 연결할 때 씀)
    created_date = Column(DateTime, nullable=False, server_default=func.now())
    created_nm = Column(String(50), nullable=False)
    modified_date = Column(DateTime, nullable=True, onupdate=func.now())
    modified_nm = Column(String(50), nullable=True)
    del_yn = Column(CHAR(1), nullable=False, default="N")


class ChatFeedback(Base):
    __tablename__ = "chat_feedback"

    feedback_id = Column(BigInteger, primary_key=True, autoincrement=True)
    session_id = Column(BigInteger, ForeignKey("chat_session.session_id"), nullable=False)
    message_id = Column(BigInteger, ForeignKey("chat_message.message_id"), nullable=True)
    feedback = Column(String(10), nullable=False)
    reason = Column(String(100), nullable=True)
    created_date = Column(DateTime, nullable=False, server_default=func.now())
    created_nm = Column(String(50), nullable=False)
    modified_date = Column(DateTime, nullable=True, onupdate=func.now())
    modified_nm = Column(String(50), nullable=True)
    del_yn = Column(CHAR(1), nullable=False, default="N")
