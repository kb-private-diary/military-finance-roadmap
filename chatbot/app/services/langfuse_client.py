import logging
from typing import List

from app.core.config import LANGFUSE_BASE_URL, LANGFUSE_PUBLIC_KEY, LANGFUSE_SECRET_KEY

logger = logging.getLogger(__name__)

"""LangGraph 파이프라인 실행을 Langfuse로 자동 트레이싱한다 - 의도분류→카테고리분류→
근거자료구성→답변생성 각 단계의 입출력·소요시간·비용이 Langfuse 대시보드에 자동으로 쌓인다.

키가 없는 팀원 로컬 환경(아직 Langfuse 가입 전)에서도 챗봇 자체는 정상 동작해야 하므로,
LANGFUSE_PUBLIC_KEY/SECRET_KEY가 설정된 경우에만 클라이언트를 만들고, 초기화 자체가
실패해도(키 오류·네트워크 문제 등) 조용히 무시한다 - 트레이싱은 부가 기능이라 실패해도
챗봇 답변에는 영향이 없어야 한다(2026-08-10)."""

_handler = None

if LANGFUSE_PUBLIC_KEY and LANGFUSE_SECRET_KEY:
    try:
        from langfuse import Langfuse
        from langfuse.langchain import CallbackHandler

        # Langfuse()는 내부적으로 싱글턴 클라이언트를 등록해두고, CallbackHandler()가 그걸 그대로 찾아 쓴다.
        Langfuse(
            public_key=LANGFUSE_PUBLIC_KEY,
            secret_key=LANGFUSE_SECRET_KEY,
            base_url=LANGFUSE_BASE_URL or None,
        )
        _handler = CallbackHandler()
        logger.info("Langfuse 트레이싱 활성화됨")
    except Exception:
        logger.exception("Langfuse 초기화 실패 - 트레이싱 없이 계속 진행")
        _handler = None


def get_callbacks() -> List:
    """LangGraph invoke()의 config={"callbacks": [...]}에 그대로 넣을 콜백 리스트.
    Langfuse가 설정 안 됐으면 빈 리스트를 반환해서 아무 영향도 없게 한다."""
    return [_handler] if _handler else []
