import logging

from google import genai
from google.genai import errors as genai_errors

from app.core.config import GEMINI_API_KEY

logger = logging.getLogger(__name__)

_client = genai.Client(api_key=GEMINI_API_KEY)

# 앞 모델이 무료 할당량 초과(429)나 서버 과부하(503)로 실패하면 다음 모델로 자동 전환한다.
# gemini-flash-lite-latest: 할당량 여유가 있어 확인된 기본 모델
# gemini-flash-latest: 별도 할당량을 쓰는 예비 모델(일일 한도가 적어 fallback 용도로만 사용)
MODEL_FALLBACK_CHAIN = [
    "gemini-flash-lite-latest",
    "gemini-flash-latest",
]

_FALLBACK_STATUS_CODES = {429, 503}


def generate_content(contents, config=None):
    """MODEL_FALLBACK_CHAIN을 순서대로 시도하고, 마지막 모델까지 실패하면 예외를 그대로 던진다."""
    for index, model in enumerate(MODEL_FALLBACK_CHAIN):
        is_last = index == len(MODEL_FALLBACK_CHAIN) - 1
        try:
            return _client.models.generate_content(model=model, contents=contents, config=config)
        except genai_errors.APIError as e:
            if e.code in _FALLBACK_STATUS_CODES and not is_last:
                logger.warning("Gemini 모델 %s 실패(code=%s) — 다음 모델로 전환", model, e.code)
                continue
            raise
