from sqlalchemy import BigInteger, Column, String

from app.core.db import Base


class User(Base):
    """`user` 테이블(회원 도메인 소유, Spring 담당)의 읽기 전용 매핑. 관리자 권한(role) 확인용으로만 사용한다."""

    __tablename__ = "user"

    id = Column(BigInteger, primary_key=True)
    role = Column(String(20), nullable=False)
