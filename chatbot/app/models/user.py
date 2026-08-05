from sqlalchemy import BigInteger, Column, String

from app.core.db import Base


class User(Base):
    """`user` 테이블(회원 도메인 소유, Spring 담당)의 읽기 전용 매핑.
    JWT의 username(user_id) -> 숫자 id 조회, 관리자 권한(role) 확인에 사용한다."""

    __tablename__ = "user"

    id = Column(BigInteger, primary_key=True)
    user_id = Column(String(50), nullable=False)  # 로그인 아이디(이메일) - JWT sub 클레임과 매칭
    role = Column(String(20), nullable=False)
    del_yn = Column(String(1), nullable=False)
