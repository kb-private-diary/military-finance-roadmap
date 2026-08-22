import re
from typing import Optional

from app.services import gemini_client

VALID_INTENTS = ("info", "counsel", "irrelevant")

# "적금이랑 예금 중에 뭐가 좋아?"를 LLM 분류에만 맡겼더니, 같은 질문인데도 어떨 때는 info로
# 어떨 때는 counsel로 갈렸다(2026-08-21 발견 - "뭐가 좋아?"라는 말투가 개인 상담 요청처럼도,
# 이미 밝힌 두 유형을 비교해달라는 정보성 질문처럼도 읽혀서 LLM이 매번 다르게 판단함).
# 서로 다른 상품 유형 이름이 문장에 2개 이상 직접 나오면, 그건 애매한 상담이 아니라 두 유형을
# 비교해달라는 게 명백하므로 LLM 호출 없이 여기서 결정적으로 info로 확정한다
# (개인정보 감지 시 LLM을 아예 안 거치는 것과 같은 원칙 - pii_filter.py 참고).
_NAMED_CATEGORY_KEYWORDS = ("적금", "예금", "청약", "펀드", "보험")


def _standalone_word_in(text: str, word: str) -> bool:
    return re.search(rf"(^|\s){re.escape(word)}", text) is not None


def named_categories_mentioned(question: str) -> set:
    return {kw for kw in _NAMED_CATEGORY_KEYWORDS if _standalone_word_in(question, kw)}

_PROMPT_TEMPLATE = (
    "{history_block}"
    "다음 질문을 아래 세 카테고리 중 하나로만 분류해라. 다른 말은 붙이지 말고 카테고리 이름만 정확히 출력해라. "
    "[이전 대화]가 있으면 그 문맥을 반드시 참고해라 — 질문만 보면 뜬금없어도, 직전 대화의 후속 질문이면 "
    "그 맥락에 맞는 카테고리로 분류해라 (예: 상품 설명을 들은 직후 '더 자세히 알려줘'는 info다).\n"
    "- info: 특정 금융 상품·정책의 정보를 묻거나, 이미 이름을 밝힌 상품·상품유형끼리 비교해달라는 질문 "
    "(예: 금리, 가입조건, 혜택, 신청방법, '적금이랑 예금 중에 뭐가 좋아?'처럼 비교 대상이 질문에 이미 드러난 경우)\n"
    "- counsel: 비교할 대상을 스스로 정하지 못한 채 본인 상황에 맞는 재무 계획·추천을 요청하는 질문 "
    "(예: 목돈을 어떻게 굴려야 하는지, 얼마를 모아야 하는지 — 비교 대상 상품·유형이 질문에 안 나와 있는 경우)\n"
    "- irrelevant: 군 재무·금융 상품과 관련 없는 질문. 전세·부동산·날씨처럼 키워드만 비슷하고 "
    "실제로는 특정 금융 상품·정책 정보를 묻는 게 아닌 질문도 포함한다 "
    "(예: '전세 사기 조심하는 법', '오늘 날씨 어때')\n\n"
    "질문: {question}\n"
    "분류:"
)


def classify_intent(question: str, history_block: str = "") -> str:
    if len(named_categories_mentioned(question)) >= 2:
        return "info"
    prompt = _PROMPT_TEMPLATE.format(history_block=history_block, question=question)
    response = gemini_client.generate_content(prompt)
    label = response.strip().lower()
    # 분류 결과가 애매하면 안전하게 무관련 처리(fail-closed) — 잘못된 출처를 붙여 답하는 것보다 낫다
    return label if label in VALID_INTENTS else "irrelevant"


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
    response = gemini_client.generate_content(_CATEGORY_PROMPT_TEMPLATE.format(question=question))
    label = response.strip().lower()
    return label if label in PRODUCT_CATEGORIES else None
