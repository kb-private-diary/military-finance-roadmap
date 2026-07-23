from google import genai
from google.genai import types

from app.core.config import GEMINI_API_KEY
from app.services import vectorstore

_client = genai.Client(api_key=GEMINI_API_KEY)

_MODEL = "gemini-flash-latest"
_TOP_K = 3

SYSTEM_INSTRUCTION = (
    "너는 군장병을 위한 재무 상담 챗봇이다. 아래 [참고 정책 문서]에 있는 내용만 근거로 답변한다. "
    "문서에 없는 내용은 모른다고 솔직히 답한다. 답변은 3~5문장 이내로 간결하게 작성한다. "
    "군대 다나까 말투로만 답한다 — 평서문은 '~다'로 끝내고(예: '지급됩니다' 대신 '지급됩니다' 그대로, "
    "'~습니다'), 의문형은 '~까'로 끝낸다(예: '필요하십니까?'). 반말이나 부드러운 종결어미(~해요, ~예요)는 쓰지 않는다."
)


def generate_reply(question: str) -> str:
    context_chunks = vectorstore.search(question, top_k=_TOP_K)
    context = "\n\n".join(context_chunks)
    prompt = f"[참고 정책 문서]\n{context}\n\n[질문]\n{question}"
    response = _client.models.generate_content(
        model=_MODEL,
        contents=prompt,
        config=types.GenerateContentConfig(system_instruction=SYSTEM_INSTRUCTION),
    )
    return response.text
