from typing import Optional

from google import genai

from app.core.config import GEMINI_API_KEY

_client = genai.Client(api_key=GEMINI_API_KEY)
_MODEL = "gemini-flash-latest"

VALID_INTENTS = ("info", "counsel", "irrelevant")

_PROMPT_TEMPLATE = (
    "다음 질문을 아래 세 카테고리 중 하나로만 분류해라. 다른 말은 붙이지 말고 카테고리 이름만 정확히 출력해라.\n"
    "- info: 특정 금융 상품·정책의 정보를 묻는 질문 (예: 금리, 가입조건, 혜택, 신청방법)\n"
    "- counsel: 본인 상황에 맞는 재무 계획·상담·추천을 요청하는 질문 (예: 목돈을 어떻게 굴려야 하는지, 얼마를 모아야 하는지)\n"
    "- irrelevant: 군 재무·금융 상품과 관련 없는 질문\n\n"
    "질문: {question}\n"
    "분류:"
)


def classify_intent(question: str) -> str:
    response = _client.models.generate_content(
        model=_MODEL,
        contents=_PROMPT_TEMPLATE.format(question=question),
    )
    label = response.text.strip().lower()
    return label if label in VALID_INTENTS else "info"


PRODUCT_CATEGORIES = ("savings", "deposit", "subscription", "investment")

_CATEGORY_PROMPT_TEMPLATE = (
    "다음 질문이 실시간 금융상품 데이터 조회가 필요한 질문이면 아래 카테고리 중 하나만, "
    "그게 아니라 특정 정책(장병내일준비적금·청년미래적금·청년주택드림청약통장)이나 용어 설명을 묻는 질문이면 "
    "'none'을 출력해라. 다른 말은 붙이지 말고 정확히 이 중 하나만 출력해라.\n"
    "- savings: 적금 상품·금리 비교\n"
    "- deposit: 예금 상품·금리 비교\n"
    "- subscription: 아파트 청약·분양 정보\n"
    "- investment: 펀드 상품\n"
    "- none: 그 외 (정책 설명, 용어 설명 등)\n\n"
    "질문: {question}\n"
    "분류:"
)


def classify_product_category(question: str) -> Optional[str]:
    response = _client.models.generate_content(
        model=_MODEL,
        contents=_CATEGORY_PROMPT_TEMPLATE.format(question=question),
    )
    label = response.text.strip().lower()
    return label if label in PRODUCT_CATEGORIES else None
