from typing import Tuple

from google import genai
from google.genai import types

from app.core.config import GEMINI_API_KEY
from app.services import cheongyakhome, fss, vectorstore
from app.services import fund as fund_service
from app.services.intent import classify_intent, classify_product_category

_client = genai.Client(api_key=GEMINI_API_KEY)

_MODEL = "gemini-flash-latest"
_TOP_K = 3
_LIVE_DATA_LIMIT = 5

IRRELEVANT_REPLY = (
    "죄송합니다, 본 챗봇은 군 재무·금융 상품 관련 질문만 답변 가능합니다. "
    "다른 질문으로 다시 문의해 주시기 바랍니다."
)

RAG_SOURCE_LABEL = "Gemini AI (정책 문서 기반 생성)"

_LIVE_SOURCE_LABELS = {
    "savings": "Gemini AI (금감원 실시간 적금 데이터 기반 생성)",
    "deposit": "Gemini AI (금감원 실시간 예금 데이터 기반 생성)",
    "subscription": "Gemini AI (청약홈 실시간 분양정보 기반 생성)",
    "investment": "Gemini AI (펀드 실시간 데이터 기반 생성)",
}

SYSTEM_INSTRUCTION = (
    "너는 군장병을 위한 재무 상담 챗봇이다. 아래 [참고 자료]에 있는 내용만 근거로 답변한다. "
    "자료에 없는 내용은 모른다고 솔직히 답한다. 답변은 3~5문장 이내로 간결하게 작성한다. "
    "군대 다나까 말투로만 답한다 — 평서문은 '~다'로 끝내고(예: '지급됩니다' 대신 '지급됩니다' 그대로, "
    "'~습니다'), 의문형은 '~까'로 끝낸다(예: '필요하십니까?'). 반말이나 부드러운 종결어미(~해요, ~예요)는 쓰지 않는다."
)


def _build_product_context(category: str) -> str:
    if category in fss.PRODUCT_ENDPOINTS:
        products = fss.fetch_products(category)
        top = sorted(products, key=fss.max_rate, reverse=True)[:_LIVE_DATA_LIMIT]
        return "\n".join(
            f"- {p['kor_co_nm']} {p['fin_prdt_nm']} (최고금리 {fss.max_rate(p)}%)" for p in top
        )
    if category == "subscription":
        listings = cheongyakhome.fetch_listings(per_page=_LIVE_DATA_LIMIT)
        return "\n".join(
            f"- {listing['HOUSE_NM']} (청약접수 {listing.get('RCEPT_BGNDE')}~{listing.get('RCEPT_ENDDE')})"
            for listing in listings
        )
    if category == "investment":
        funds = fund_service.fetch_funds(num_of_rows=_LIVE_DATA_LIMIT)
        return "\n".join(f"- {fund['fndNm']} ({fund.get('fndTp')})" for fund in funds)
    return ""


def generate_reply(question: str) -> Tuple[str, str]:
    """반환값: (답변, source 라벨)"""
    intent = classify_intent(question)
    if intent == "irrelevant":
        return IRRELEVANT_REPLY, "무관련 질문 안내"

    product_category = classify_product_category(question) if intent == "info" else None

    if product_category:
        context = _build_product_context(product_category)
        prompt = f"[실시간 상품 데이터 - {product_category}]\n{context}\n\n[질문]\n{question}"
        source = _LIVE_SOURCE_LABELS[product_category]
    else:
        context_chunks = vectorstore.search(question, top_k=_TOP_K)
        context = "\n\n".join(context_chunks)
        prompt = f"[참고 정책 문서]\n{context}\n\n[질문]\n{question}"
        source = RAG_SOURCE_LABEL

    response = _client.models.generate_content(
        model=_MODEL,
        contents=prompt,
        config=types.GenerateContentConfig(system_instruction=SYSTEM_INSTRUCTION),
    )
    return response.text, source
