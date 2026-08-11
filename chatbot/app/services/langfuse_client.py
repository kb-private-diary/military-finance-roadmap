import logging
from typing import List, Optional

from app.core.config import LANGFUSE_BASE_URL, LANGFUSE_PUBLIC_KEY, LANGFUSE_SECRET_KEY

logger = logging.getLogger(__name__)

"""LangGraph 파이프라인 실행을 Langfuse로 자동 트레이싱한다 - 의도분류→카테고리분류→
근거자료구성→답변생성 각 단계의 입출력·소요시간·비용이 Langfuse 대시보드에 자동으로 쌓인다.
또한 답변마다 trace_id를 미리 만들어서 넘겨두면(new_trace_id), 나중에 사용자가 그 답변에
피드백(좋아요/별로+사유)을 남길 때 같은 trace에 점수로 연결할 수 있다(score_trace) -
관리자가 Langfuse 대시보드에서 "왜 별로라고 했는지"를 그 대화의 전체 맥락과 함께 볼 수 있다
(2026-08-10, 멘토 피드백 2번).

키가 없는 팀원 로컬 환경(아직 Langfuse 가입 전)에서도 챗봇 자체는 정상 동작해야 하므로,
LANGFUSE_PUBLIC_KEY/SECRET_KEY가 설정된 경우에만 클라이언트를 만들고, 초기화 자체가
실패해도(키 오류·네트워크 문제 등) 조용히 무시한다 - 트레이싱은 부가 기능이라 실패해도
챗봇 답변에는 영향이 없어야 한다."""

_enabled = False

if LANGFUSE_PUBLIC_KEY and LANGFUSE_SECRET_KEY:
    try:
        from langfuse import Langfuse

        # Langfuse()는 내부적으로 싱글턴 클라이언트를 등록해두고, get_client()/CallbackHandler()가
        # 그걸 그대로 찾아 쓴다.
        Langfuse(
            public_key=LANGFUSE_PUBLIC_KEY,
            secret_key=LANGFUSE_SECRET_KEY,
            base_url=LANGFUSE_BASE_URL or None,
        )
        _enabled = True
        logger.info("Langfuse 트레이싱 활성화됨")
    except Exception:
        logger.exception("Langfuse 초기화 실패 - 트레이싱 없이 계속 진행")
        _enabled = False


def new_trace_id() -> Optional[str]:
    """답변 생성 시작 전에 미리 trace_id를 만들어둔다 - 나중에 그 답변에 피드백이 달리면
    이 id로 같은 trace에 점수를 연결할 수 있다. Langfuse 비활성화 상태면 None."""
    if not _enabled:
        return None
    try:
        from langfuse import get_client

        return get_client().create_trace_id()
    except Exception:
        logger.exception("Langfuse trace_id 생성 실패")
        return None


def get_callbacks(trace_id: Optional[str] = None) -> List:
    """LangGraph invoke()의 config={"callbacks": [...]}에 그대로 넣을 콜백 리스트.
    trace_id를 주면 그 id로 트레이스가 기록되고(요청마다 새 핸들러를 만들어야
    trace_context를 다르게 지정할 수 있음), Langfuse가 설정 안 됐으면 빈 리스트를
    반환해서 아무 영향도 없게 한다."""
    if not _enabled:
        return []
    try:
        from langfuse.langchain import CallbackHandler

        trace_context = {"trace_id": trace_id} if trace_id else None
        return [CallbackHandler(trace_context=trace_context)]
    except Exception:
        logger.exception("Langfuse 콜백 생성 실패")
        return []


def score_trace(trace_id: str, feedback: str, reason: Optional[str] = None) -> None:
    """사용자가 남긴 피드백(좋아요/보통/별로 + 사유)을 그 답변을 만든 trace에 점수로 붙인다.
    실패해도(네트워크 문제 등) 조용히 무시 - 피드백 저장 자체(DB)는 이미 끝난 뒤라
    이 연결이 실패해도 사용자 경험에는 영향 없어야 한다."""
    if not _enabled or not trace_id:
        return
    try:
        from langfuse import get_client

        get_client().create_score(
            trace_id=trace_id,
            name="user_feedback",
            value=feedback,
            data_type="CATEGORICAL",
            comment=reason,
        )
    except Exception:
        logger.exception("Langfuse 피드백 점수 연결 실패 (trace_id=%s)", trace_id)
