import jwt
from fastapi import Depends, Header
from sqlalchemy.orm import Session

from app.core.config import JWT_ALGORITHM, JWT_SECRET
from app.core.db import get_db
from app.core.exceptions import BusinessException
from app.models.user import User

_AUTH_ERROR = BusinessException("로그인이 필요한 서비스입니다", 401, "AUTH_001")


def _extract_username(authorization: str) -> str:
    if not authorization or not authorization.startswith("Bearer "):
        raise _AUTH_ERROR
    token = authorization[len("Bearer "):]
    try:
        claims = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
    except jwt.PyJWTError:
        raise _AUTH_ERROR
    if claims.get("type") == "refresh":
        raise _AUTH_ERROR
    username = claims.get("sub")
    if not username:
        raise _AUTH_ERROR
    return username


def get_current_user_id(authorization: str = Header(default=None), db: Session = Depends(get_db)) -> int:
    """Authorization: Bearer {token} 을 검증해 로그인한 회원의 숫자 id를 반환한다.
    토큰이 없거나 유효하지 않으면(만료·위조·refresh 토큰 오용 등) 401을 던진다."""
    username = _extract_username(authorization)
    user = db.query(User).filter(User.user_id == username, User.del_yn == "N").first()
    if not user:
        raise _AUTH_ERROR
    return user.id


def get_current_admin_user_id(authorization: str = Header(default=None), db: Session = Depends(get_db)) -> int:
    """관리자 전용 API에 사용 - 로그인 + role=ADMIN 까지 확인한다."""
    username = _extract_username(authorization)
    user = db.query(User).filter(User.user_id == username, User.del_yn == "N").first()
    if not user:
        raise _AUTH_ERROR
    if user.role != "ADMIN":
        raise BusinessException("관리자만 접근할 수 있습니다", 403, "CHAT_010")
    return user.id
