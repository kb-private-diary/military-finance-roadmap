from google import genai
from google.genai import types

from app.core.config import GEMINI_API_KEY
from app.services.policy_docs import load_policy_docs

_client = genai.Client(api_key=GEMINI_API_KEY)

_MODEL = "gemini-flash-latest"

SYSTEM_INSTRUCTION = (
    "너는 군장병을 위한 재무 상담 챗봇이야. 아래 [참고 정책 문서]에 있는 내용만 근거로 답변해. "
    "문서에 없는 내용은 모른다고 솔직히 말해. 답변은 3~5문장 이내로 간결하게 작성해."
)


def generate_reply(question: str) -> str:
    docs = load_policy_docs()
    prompt = f"[참고 정책 문서]\n{docs}\n\n[질문]\n{question}"
    response = _client.models.generate_content(
        model=_MODEL,
        contents=prompt,
        config=types.GenerateContentConfig(system_instruction=SYSTEM_INSTRUCTION),
    )
    return response.text
