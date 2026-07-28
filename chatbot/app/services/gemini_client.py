from typing import Optional

from langchain_core.messages import HumanMessage, SystemMessage
from langchain_google_genai import ChatGoogleGenerativeAI

from app.core.config import GEMINI_API_KEY

# 앞 모델이 실패하면(할당량 초과·서버 과부하 등) 다음 모델로 자동 전환한다.
# LangChain의 with_fallbacks가 기본적으로 모든 예외(Exception)를 폴백 대상으로 처리한다.
# gemini-flash-lite-latest: 할당량 여유가 있어 확인된 기본 모델
# gemini-flash-latest: 별도 할당량을 쓰는 예비 모델(일일 한도가 적어 fallback 용도로만 사용)
MODEL_FALLBACK_CHAIN = [
    "gemini-flash-lite-latest",
    "gemini-flash-latest",
]

_models = [
    ChatGoogleGenerativeAI(model=name, google_api_key=GEMINI_API_KEY, temperature=None)
    for name in MODEL_FALLBACK_CHAIN
]

_llm = _models[0].with_fallbacks(_models[1:])


def _extract_text(content) -> str:
    """AIMessage.content는 모델/버전에 따라 str 또는 {"type": "text", "text": ...} 블록의 list로 온다."""
    if isinstance(content, str):
        return content
    parts = []
    for block in content:
        if isinstance(block, str):
            parts.append(block)
        elif isinstance(block, dict) and block.get("type") == "text":
            parts.append(block.get("text", ""))
    return "".join(parts)


def generate_content(content: str, system_instruction: Optional[str] = None) -> str:
    """MODEL_FALLBACK_CHAIN을 순서대로 시도하고, 마지막 모델까지 실패하면 예외를 그대로 던진다."""
    messages = []
    if system_instruction:
        messages.append(SystemMessage(content=system_instruction))
    messages.append(HumanMessage(content=content))
    response = _llm.invoke(messages)
    return _extract_text(response.content)
