from typing import List, Optional, Tuple, TypedDict

from langgraph.graph import END, START, StateGraph

from app.services import cheongyakhome, fss, gemini_client, langfuse_client, vectorstore
from app.services import fund as fund_service
from app.services.intent import classify_intent, classify_product_category, named_categories_mentioned

_TOP_K = 3
_LIVE_DATA_LIMIT = 5

# "뭐가 좋아?", "어떤 게 나아?"처럼 상품명을 직접 언급하지 않는 비교/추천형 질문. mentioned_docs가
# 비어서 예전엔 is_comparison이 절대 안 켜졌고, RAG가 여러 상품 문서를 끌어와도 "비교해서 답하라"는
# 지시 없이 넘겨서 상품마다 비슷비슷한 답만 반복하는 문제가 있었다(2026-08-13 피드백).
_COMPARISON_INTENT_KEYWORDS = ("뭐가 좋", "뭐가 나", "어떤 게 좋", "어느 게 좋", "어떤 게 나", "차이", "비교")

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
    "mortgage": "Gemini AI (금감원 실시간 주택담보대출 데이터 기반 생성)",
    "jeonse": "Gemini AI (금감원 실시간 전세자금대출 데이터 기반 생성)",
    "creditLoan": "Gemini AI (금감원 실시간 개인신용대출 데이터 기반 생성)",
}

# 실시간(API) 상품 상세를 보고 그 상품 하나에 대해 후속 질문할 때 쓰는 출처 라벨.
# 카테고리 전체 요약이 아니라 프론트가 이미 화면에 표시한 그 상품 하나의 정보만 근거로 생성한다(2026-08-07).
LIVE_PRODUCT_QA_SOURCE_LABEL = "Gemini AI (선택한 상품 정보 기반 생성)"

# RAG 답변의 출처 캡션용 — 문서(doc_name)별 기관명 표기.
_DOC_SOURCE_ORG = {
    "장병내일준비적금": "KB국민은행 상품안내",
    "청년미래적금": "KB국민은행 상품안내",
    "청년주택드림청약통장": "KB국민은행 상품안내",
    "KB손해보험 자동차보험(개인)": "KB손해보험 상품안내",
    "KB손해보험 이륜차보험(개인)": "KB손해보험 상품안내",
    "KB손해보험 운전자보험(3년 이상)": "KB손해보험 상품안내",
    "KB손해보험 오토바이 운전자보험": "KB손해보험 상품안내",
    "KB손해보험 운전자보험(1~3년)": "KB손해보험 상품안내",
    "KB손해보험 하루운전자보험(1~7일)": "KB손해보험 상품안내",
    "KB손해보험 실손의료비보장보험(본인)": "KB손해보험 상품안내",
    "정책용어사전": "정책용어사전",
}
_DOC_AS_OF = "2026년 3월 기준"

# 3개 고정 상품의 실제 상세 페이지 링크 (에스더 직접 확인 요청 → 실제 링크 받아서 반영, 2026-08-06).
# 장병내일준비적금은 국방부·은행 공동 제도라 KB 온라인뱅킹이 아니라 국방부 공식 안내 페이지로 연결한다.
# 아래 7개 KB손해보험 상품은 에스더가 보내준 링크를 직접 열어서 각 상품 페이지(타이틀·심의필 번호까지)
# 확인한 딥링크다(2026-08-08). 자동차/이륜차는 그대로 받은 링크, 운전자보험 4종·실손은 받은 링크가
# 상품 그룹 진입점(실손 페이지)이라 왼쪽 "생활보험" 메뉴에서 각 상품의 실제 hash 라우트를 찾아서 반영함.
_DOC_SOURCE_URL = {
    "장병내일준비적금": "https://mnd.go.kr/mnd/288/subview.do",
    "청년미래적금": "https://obank.kbstar.com/quics?page=C020722&boardId=669&compId=b058336&articleId=145082&bbsMode=view&viewPage=1&articleClass=2&searchCondition=title&searchStr=",
    "청년주택드림청약통장": "https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000935",
    "KB손해보험 자동차보험(개인)": "https://direct.kbinsure.co.kr/home/#/CAR_INDV_IS001M/",
    "KB손해보험 이륜차보험(개인)": "https://direct.kbinsure.co.kr/home/#/CAR_TWLVHL_W001M",
    "KB손해보험 운전자보험(3년 이상)": "https://direct.kbinsure.co.kr/home/#/GL/DR/LT_CM0101M/",
    "KB손해보험 오토바이 운전자보험": "https://direct.kbinsure.co.kr/home/#/GL/LMD/LT_CM0101M/",
    "KB손해보험 운전자보험(1~3년)": "https://direct.kbinsure.co.kr/home/#/GL/SD/GN_CM0101M//01",
    "KB손해보험 하루운전자보험(1~7일)": "https://direct.kbinsure.co.kr/home/#/GL/OSD/GN_CM0101M/",
    "KB손해보험 실손의료비보장보험(본인)": "https://direct.kbinsure.co.kr/home/#/GL/RD/LT_CM0101M/",
}


def _build_source_detail(doc_name: Optional[str]) -> Optional[str]:
    """특정 상품 문서(장병내일준비적금 등)에서 나온 답변만 출처 캡션을 붙인다.
    정책용어사전은 특정 상품 설명이 아니라 일반 용어 정의라, "출처 · 정책용어사전"처럼
    표시해봤자 의미 있는 출처가 아니라서 아예 안 붙인다(프론트에서 출처 줄 자체가 숨겨짐)."""
    if not doc_name or doc_name == "정책용어사전":
        return None
    org = _DOC_SOURCE_ORG.get(doc_name, doc_name)
    return f"{org} · {doc_name} ({_DOC_AS_OF})"


# CHAT-API-10: 답변의 source 라벨 기준 랜딩 추천. 청약/펀드는 아직 프론트에 전용 목록
# 페이지가 없어 추천하지 않는다 (적금/예금만 /simulator 페이지에 실제 비교 UI가 있음).
_LANDING_RECOMMENDATIONS = {
    _LIVE_SOURCE_LABELS["savings"]: {"label": "적금 상품 더 보기", "page_link": "/simulator"},
    _LIVE_SOURCE_LABELS["deposit"]: {"label": "예금 상품 더 보기", "page_link": "/simulator"},
}


def get_recommendation(source: Optional[str]) -> List[dict]:
    """메시지의 source 라벨을 보고 랜딩 추천 목록을 반환한다. 해당 없으면 빈 리스트."""
    recommendation = _LANDING_RECOMMENDATIONS.get(source)
    return [recommendation] if recommendation else []

SYSTEM_INSTRUCTION = (
    "너는 군장병을 위한 재무 상담 챗봇이다. 아래 [참고 자료]에 있는 내용만 근거로 답변한다. "
    "자료에 없는 내용은 모른다고 솔직히 답한다. "
    "답변은 짧고 간결하게, 질문에 직접 관련된 핵심 수치·조건 위주로만 3~5문장 이내로 답한다 — "
    "자료에 있는 내용을 전부 나열하지 않는다. 더 자세한 내용은 되물어보면 그때 추가로 안내한다. "
    "군대에서 쓰는 친근하고 예의바른 다나까 말투로 답한다. 아래 종결어미만 사용한다:\n"
    "- '~합니다', '~됩니다', '~입니다' (가장 기본적인 설명)\n"
    "- '~하지 말입니다' (설명을 부드럽게 덧붙일 때, 예: '자세한 조건은 다를 수 있지 말입니다')\n"
    "- '~하십니까?' (질문에 되물을 때)\n"
    "무뚝뚝한 명령조 평서문('~다', '~해라')이나 반말·부드러운 종결어미는 쓰지 않는다 "
    "(예: ~해요, ~예요, ~까요, ~드릴까요, ~나요, ~죠 모두 금지 — '더 안내해 드릴까요?'가 아니라 '더 안내해 드리겠습니까?'로 쓴다). "
    "은행 상담원처럼 예의 있고 친절하되, 다나까 말투를 유지한다. "
    "[이전 대화]가 주어지면 그 문맥을 참고해서 자연스럽게 이어서 답한다 "
    "(예: '그거 얼마야?'처럼 이전 답변을 가리키는 질문이면 무엇을 가리키는지 이전 대화에서 찾아 답한다). "
    "[이전 대화]에서 이미 답한 내용을 이번 답변에서 그대로 반복하지 않는다 — 새로 묻는 부분 위주로 답하되, "
    "꼭 필요하면 이미 말한 내용은 짧게만 다시 언급한다."
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


class ChatState(TypedDict, total=False):
    question: str
    history: List[Tuple[str, str]]  # [(role, content), ...] 오래된 순 - 현재 질문은 미포함
    intent: str
    product_category: Optional[str]
    # 실시간(API) 상품 상세를 이미 화면에 보여준 상태에서 그 상품 하나에 대해 후속 질문할 때만 채워짐.
    # 채워지면 의도분류·카테고리분류를 다 건너뛰고 이 텍스트 하나만 근거로 답한다(2026-08-07).
    product_context: Optional[str]
    context: str
    source: str
    doc_names: List[str]  # RAG 검색결과 top_k의 doc_name (유사도 순), 실시간 데이터면 빈 리스트
    mentioned_docs: List[str]  # 질문 문장 자체에서 정확히 언급된 상품명 (RAG 검색 타겟팅 + 출처 판정 우선순위용)
    source_detail: Optional[str]  # 사람이 읽는 출처 캡션 (RAG 답변만 해당, 없으면 None)
    source_url: Optional[str]  # 출처를 클릭해서 실제 상품 페이지로 이동할 수 있는 링크 (있는 문서만)
    is_ai_generated: bool  # 프론트에 "AI가 생성한 답변입니다" 문구를 보여줄지 여부
    is_comparison: bool  # 상품 2개 이상을 비교하는 질문인지 (프롬프트에서 비교 지시 추가용)
    answer: str


def _format_history(history: List[Tuple[str, str]]) -> str:
    if not history:
        return ""
    lines = [f"{'사용자' if role == 'user' else '챗봇'}: {content}" for role, content in history]
    return "[이전 대화]\n" + "\n".join(lines) + "\n\n"


def _classify_intent_node(state: ChatState) -> ChatState:
    # intent가 이미 정해져 있으면(자주 묻는 질문처럼 미리 정보성으로 확정된 질문) 재분류하지 않는다.
    # "청약통장은 꼭 만들어야 해요?" 같은 문장은 LLM이 상담(counsel)으로 분류할 때도 있어서,
    # 큐레이션된 FAQ 질문이 매번 다르게(때로는 상담 흐름으로 튀어) 나오는 걸 막기 위함이다.
    if state.get("intent"):
        return {}
    history_block = _format_history(state.get("history") or [])
    return {"intent": classify_intent(state["question"], history_block=history_block)}


def _classify_category_node(state: ChatState) -> ChatState:
    # product_context가 있으면(=이미 화면에 보여준 실시간 상품 하나에 대한 후속 질문) 카테고리 분류
    # 자체가 무의미하다 - 어차피 이 상품 하나만 근거로 답할 거라 LLM 호출만 낭비된다(2026-08-07).
    if state.get("product_context"):
        return {}
    return {"product_category": classify_product_category(state["question"])}


_SPECIFIC_DOC_NAMES = (
    "장병내일준비적금",
    "청년미래적금",
    "청년주택드림청약통장",
    "KB손해보험 자동차보험(개인)",
    "KB손해보험 이륜차보험(개인)",
    "KB손해보험 운전자보험(3년 이상)",
    "KB손해보험 오토바이 운전자보험",
    "KB손해보험 운전자보험(1~3년)",
    "KB손해보험 하루운전자보험(1~7일)",
    "KB손해보험 실손의료비보장보험(본인)",
)

# 상품명을 정확히 안 쓰고 "적금이 나아요, 청약이 나아요?"처럼 일반 단어로만 물어봤을 때 쓸 대표 상품.
# 카테고리(적금)에 해당 상품이 여러 개(장병내일준비적금·청년미래적금)라도 대표로 1개만 써서,
# 검색·프롬프트 크기가 상품 수만큼 불어나 응답이 느려지는(실측 3개 비교 시 50초+) 걸 막는다.
# 예금은 RAG 정책 문서가 아니라 실시간 API로만 다뤄서(_LIVE_SOURCE_LABELS 참고) 대표 문서가 없다 -
# 그래도 "몇 개 카테고리가 같이 언급됐는지" 셀 땐 예금도 포함해야 한다(바로 아래 주석 참고), 그래서
# 매핑용 딕셔너리와는 별도로 감지용 키워드 목록을 둔다.
_GENERIC_CATEGORY_DOC = {
    "적금": "장병내일준비적금",
    "청약": "청년주택드림청약통장",
    "보험": "KB손해보험 자동차보험(개인)",
}
_GENERIC_CATEGORY_KEYWORDS = ("적금", "예금", "청약", "보험")


def _mentioned_doc_names(question: str) -> List[str]:
    # 상품명을 정확히 썼으면 그것만 본다. "장병내일준비적금 가입 조건"처럼 특정 상품 하나를
    # 콕 집어 물었는데도, 그 이름 안에 "적금"이 들어있다는 이유로 청년미래적금까지 딸려오면 안 된다.
    # 상품명을 정확히 하나도 안 썼을 때만("적금이 나아요, 청약이 나아요?") 일반 단어로 넓혀서 본다.
    exact = [name for name in _SPECIFIC_DOC_NAMES if name in question]
    if exact:
        return exact
    generic_hits = [keyword for keyword in _GENERIC_CATEGORY_KEYWORDS if keyword in question]
    # "적금이랑 예금 중에 뭐가 좋아?"처럼 일반 카테고리 키워드가 2개 이상 같이 나오면, 그건 한
    # 카테고리의 대표 상품으로 검색을 좁혀도 되는 질문이 아니라 카테고리끼리 개념을 비교해달라는
    # 질문이다. 여기서 대표 상품(예: 장병내일준비적금) 하나로 확정해버리면 검색도 그 상품 쪽으로만
    # 쏠리고, 출처도 실제로 언급조차 안 한 그 상품으로 잘못 붙어버린다(2026-08-21 발견 - "예금"이
    # _GENERIC_CATEGORY_DOC에 아예 없어서 "적금"만 매칭되는 바람에 이 분기 자체가 한 번 더 새는
    # 버그도 같이 있었음) - 이 경우엔 아무 것도 확정하지 않고 빈 리스트를 반환해 일반 검색(비교
    # 개념이 담긴 용어사전 등)으로 넘긴다.
    if len(generic_hits) >= 2:
        return []
    if len(generic_hits) == 1:
        doc = _GENERIC_CATEGORY_DOC.get(generic_hits[0])
        return [doc] if doc else []
    return []


def _pick_doc_name(answer: str, doc_names: List[str]) -> Optional[str]:
    """생성된 답변 본문과 검색된 문서 목록을 보고 어느 문서가 출처인지 고른다.

    검색 1위 청크만 보면 부정확할 수 있다 — 예를 들어 "장병내일준비적금 가입 조건"을
    물으면 정책용어사전의 "가입자격확인서" 항목이 임베딩 유사도로는 1위인데,
    실제 답변 본문은 2·3위인 장병내일준비적금 청크 내용으로 채워지는 경우가 있다.
    그래서 답변에 특정 상품명이 하나만 명시적으로 언급되면 그걸 우선하고,
    "비과세가 뭐야?"처럼 여러 상품에 걸치거나 상품명이 안 나오는 일반 용어 설명이면
    검색 1위 문서(보통 정책용어사전)를 기본값으로 쓴다.
    """
    mentioned = [name for name in _SPECIFIC_DOC_NAMES if name in answer]
    if len(mentioned) == 1:
        return mentioned[0]
    return doc_names[0] if doc_names else None


def _build_context_node(state: ChatState) -> ChatState:
    product_context = state.get("product_context")
    if product_context:
        # 프론트가 이미 화면에 보여준 그 상품 하나의 정보를 그대로 근거로 쓴다 - RAG 검색도,
        # 카테고리 top-5 요약도 안 거친다(2026-08-07).
        return {
            "context": product_context,
            "source": LIVE_PRODUCT_QA_SOURCE_LABEL,
            "doc_names": [],
            "is_ai_generated": True,
        }

    category = state.get("product_category")
    if category:
        context = _build_product_context(category)
        source = _LIVE_SOURCE_LABELS[category]
        return {"context": context, "source": source, "doc_names": [], "is_ai_generated": True}

    search_query = state["question"]
    history = state.get("history") or []
    if history:
        # "더 자세히 알려줘"처럼 그 자체로는 검색이 안 되는 후속 질문은, 직전 사용자 질문을 검색어에
        # 같이 섞어야 원래 주제(예: 장병내일준비적금)로 다시 검색된다.
        last_user_question = next((content for role, content in reversed(history) if role == "user"), None)
        if last_user_question:
            search_query = f"{last_user_question} {search_query}"

    # 비교형 질문("장병내일준비적금이랑 청년미래적금 차이가 뭐예요?")은 검색어 하나로 top_k만 뽑으면
    # 의미상 가장 비슷한 한쪽 상품으로 결과가 쏠려서 다른 쪽 자료가 아예 안 딸려온다.
    # 상품이 1개라도 정확히 감지되면 그 상품명으로 검색을 타겟팅한다 - 예전엔 2개 이상일 때만
    # 타겟팅해서, "운전자보험(3년 이상)"처럼 이름이 거의 똑같은 문서가 여러 개 있으면 1개만 언급된
    # 질문에서도 임베딩 유사도로 엉뚱한 문서(예: 운전자보험(1~3년))가 섞여 들어왔다(2026-08-07 버그 수정).
    mentioned_docs = _mentioned_doc_names(state["question"])
    if mentioned_docs:
        results = []
        seen_texts = set()
        for name in mentioned_docs:
            for text, meta in vectorstore.search_with_metadata(f"{name} {search_query}", top_k=4):
                if text not in seen_texts:
                    seen_texts.add(text)
                    results.append((text, meta))
    else:
        results = vectorstore.search_with_metadata(search_query, top_k=_TOP_K)

    context = "\n\n".join(text for text, _ in results)
    source = RAG_SOURCE_LABEL
    doc_names = [meta.get("doc_name") for _, meta in results if meta.get("doc_name")]
    # 상품명을 직접 안 짚어도(mentioned_docs가 비어도), 질문에 비교/추천 의도가 보이고 RAG가
    # 실제로 서로 다른 문서 2개 이상을 끌어왔다면 비교형으로 취급한다.
    has_comparison_intent = any(kw in state["question"] for kw in _COMPARISON_INTENT_KEYWORDS)
    is_comparison = len(mentioned_docs) >= 2 or (has_comparison_intent and len(set(doc_names)) >= 2)
    return {
        "context": context,
        "source": source,
        "doc_names": doc_names,
        "mentioned_docs": mentioned_docs,
        "is_ai_generated": True,
        "is_comparison": is_comparison,
    }


# 비교형 질문은 [참고 정책 문서]에 "차이는 무엇이다"라는 문장이 그대로 있는 경우가 거의 없다.
# 기본 SYSTEM_INSTRUCTION("자료에 없으면 모른다고 답한다")만 따르면, 각 상품 설명이 따로
# 있어도 "비교한 문장이 없다"며 답을 거부해버린다. 그래서 비교 질문일 때만, 자료에 있는
# 개별 사실들을 직접 견주어 답하라고 명시적으로 지시를 추가한다.
_COMPARISON_INSTRUCTION = (
    "\n\n[안내] 위 자료에 여러 상품 각각에 대한 정보가 들어있습니다. "
    "자료에 '차이는 무엇이다'처럼 비교를 직접 서술한 문장이 없더라도, "
    "각 상품 설명에 나온 개별 사실(가입조건·금리·한도 등)을 근거로 직접 비교해서 답하십시오. "
    "다만 비교에 필요한 항목 자체가 자료에 전혀 없으면 그 부분만 모른다고 솔직히 답하십시오."
)


def _generate_node(state: ChatState) -> ChatState:
    category = state.get("product_category")
    product_context = state.get("product_context")
    history_block = _format_history(state.get("history") or [])
    if category:
        prompt = f"{history_block}[실시간 상품 데이터 - {category}]\n{state['context']}\n\n[질문]\n{state['question']}"
    elif product_context:
        # 카테고리 전체가 아니라 프론트가 이미 보여준 그 상품 하나의 정보만 근거로 답한다는 걸
        # 프롬프트 헤더로도 명확히 구분해서, 실시간 데이터/정책 문서 프롬프트와 안 섞이게 한다.
        prompt = f"{history_block}[선택한 상품 정보]\n{state['context']}\n\n[질문]\n{state['question']}"
    else:
        comparison_note = _COMPARISON_INSTRUCTION if state.get("is_comparison") else ""
        # 상품 유형 2개 이상을 직접 비교하는 질문(예: "적금이랑 예금 중에 뭐가 좋아?")은 프론트가
        # 곧이어 "적금 상품 보러가기"/"예금 상품 보러가기" 버튼을 바로 붙여준다. Gemini가 평소 습관대로
        # "더 자세한 상품 안내를 해드릴까요?"처럼 답변 끝에 되묻는 문장을 또 붙이면 버튼과 내용이
        # 중복돼 어색하다(2026-08-21 피드백) - 이 경우엔 되묻지 말고 비교·추천으로 문장을 마무리하게 한다.
        if len(named_categories_mentioned(state["question"])) >= 2:
            comparison_note += (
                "\n\n[안내] 답변 마지막에 '더 안내해 드릴까요?', '더 자세히 알려드릴까요?' 같은 "
                "추가 안내를 제안하는 문장은 붙이지 말고, 비교와 추천 내용으로 답변을 마무리하십시오."
            )
        prompt = f"{history_block}[참고 정책 문서]\n{state['context']}{comparison_note}\n\n[질문]\n{state['question']}"
    answer = gemini_client.generate_content(prompt, system_instruction=SYSTEM_INSTRUCTION)

    source_detail = None
    source_url = None
    hide_source = False
    if not category and not product_context:
        # 질문 문장에서 상품명이 정확히 1개만 언급됐으면, 그게 답변 본문에 그대로 재등장하는지에
        # 기대는 _pick_doc_name(답변 텍스트 매칭)보다 더 믿을 만한 신호라 우선한다 - Gemini가
        # 답변에서 상품명 전체를 문자 그대로 안 반복하면 _pick_doc_name이 못 찾는 경우가 있었다(2026-08-07).
        question_docs = state.get("mentioned_docs") or []
        doc_name = question_docs[0] if len(question_docs) == 1 else _pick_doc_name(answer, state.get("doc_names") or [])
        source_detail = _build_source_detail(doc_name)
        source_url = _DOC_SOURCE_URL.get(doc_name)
        hide_source = source_detail is None

    result = {"answer": answer, "source_detail": source_detail, "source_url": source_url}
    if hide_source:
        # 특정 상품 문서가 아니면(정책용어사전 등) 출처 자체를 안 보여준다 -
        # _build_context_node가 채워둔 일반 RAG_SOURCE_LABEL도 같이 지워야
        # 프론트가 그걸로 대신 "출처 · Gemini AI (정책 문서 기반 생성)"를 보여주지 않는다.
        result["source"] = None
    return result


def _irrelevant_node(state: ChatState) -> ChatState:
    return {
        "answer": IRRELEVANT_REPLY,
        "source": "무관련 질문 안내",
        "source_detail": None,
        "is_ai_generated": False,
    }


# 상담(counsel)으로 분류되면 RAG로 답을 만들지 않고, 프론트가 되묻기 플로우를
# 시작할 수 있도록 intent만 그대로 신호로 전달한다 (answer는 프론트가 되묻기로
# 대체 표시하므로 실제로는 거의 노출되지 않는 안내 문구).
COUNSEL_INTRO_REPLY = "자세한 상담을 위해 몇 가지 더 여쭤보겠습니다."


def _counsel_node(state: ChatState) -> ChatState:
    return {
        "answer": COUNSEL_INTRO_REPLY,
        "source": "상담형 질문 안내",
        "source_detail": None,
        "is_ai_generated": False,
    }


def _route_after_intent(state: ChatState) -> str:
    if state["intent"] == "irrelevant":
        return "irrelevant"
    if state["intent"] == "info":
        return "classify_category"
    return "counsel"


_graph = StateGraph(ChatState)
_graph.add_node("classify_intent", _classify_intent_node)
_graph.add_node("classify_category", _classify_category_node)
_graph.add_node("build_context", _build_context_node)
_graph.add_node("generate", _generate_node)
_graph.add_node("irrelevant", _irrelevant_node)
_graph.add_node("counsel", _counsel_node)

_graph.add_edge(START, "classify_intent")
_graph.add_conditional_edges(
    "classify_intent",
    _route_after_intent,
    {"irrelevant": "irrelevant", "classify_category": "classify_category", "counsel": "counsel"},
)
_graph.add_edge("classify_category", "build_context")
_graph.add_edge("build_context", "generate")
_graph.add_edge("generate", END)
_graph.add_edge("irrelevant", END)
_graph.add_edge("counsel", END)

_compiled_graph = _graph.compile()


def generate_reply(
    question: str,
    history: Optional[List[Tuple[str, str]]] = None,
    force_intent: Optional[str] = None,
    product_context: Optional[str] = None,
) -> Tuple[str, str, Optional[str], bool, str, Optional[str], Optional[str]]:
    """반환값: (답변, source 라벨, source_detail 캡션, is_ai_generated, intent, source_url, langfuse_trace_id).
    LangGraph로 의도분류 → (분기) → 컨텍스트 구성 → 답변 생성을 수행한다.

    langfuse_trace_id: Langfuse가 이 답변 생성을 기록한 trace의 id (Langfuse 비활성화 상태면 None).
    나중에 사용자가 이 답변에 피드백을 남기면, 저장해둔 이 id로 같은 trace에 점수를 연결한다(2026-08-10).

    history: 같은 세션의 이전 메시지들 [(role, content), ...] (오래된 순, 현재 질문은 미포함).
    답변 생성 시 문맥으로 활용해 멀티턴 대화를 지원한다.
    intent는 프론트가 "counsel"일 때 되묻기 플로우로 분기할 수 있도록 그대로 반환한다.

    force_intent: 지정하면 classify_intent를 건너뛰고 이 값을 그대로 쓴다. 자주 묻는 질문처럼
    미리 정보성으로 큐레이션된 질문이 LLM 의도분류에 따라 매번 다르게(상담 등으로) 튀지 않게 한다.

    product_context: 실시간(API) 상품 상세를 이미 화면에 보여준 뒤 그 상품 하나에 대해 후속
    질문할 때만 채운다. 채워지면 의도는 무조건 정보성(info)으로 취급하고(force_intent보다 우선),
    카테고리 분류·RAG 검색을 다 건너뛰고 이 텍스트 하나만 근거로 답한다(2026-08-07).
    """
    initial_state = {"question": question, "history": history or []}
    if force_intent:
        initial_state["intent"] = force_intent
    if product_context:
        initial_state["product_context"] = product_context
        initial_state["intent"] = "info"
    trace_id = langfuse_client.new_trace_id()
    result = _compiled_graph.invoke(initial_state, config={"callbacks": langfuse_client.get_callbacks(trace_id)})
    return (
        result["answer"],
        result["source"],
        result.get("source_detail"),
        result.get("is_ai_generated", False),
        result["intent"],
        result.get("source_url"),
        trace_id,
    )
