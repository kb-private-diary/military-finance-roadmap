from typing import List, Optional, Tuple, TypedDict

from langgraph.graph import END, START, StateGraph

from app.services import cheongyakhome, fss, gemini_client, vectorstore
from app.services import fund as fund_service
from app.services.intent import classify_intent, classify_product_category

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

# RAG 답변의 출처 캡션용 — 문서(doc_name)별 기관명 표기. 정확한 상품 페이지 연결(랜딩)은
# policy_product 데이터 정리 후 별도 처리하고, 우선 텍스트 출처 표시만 제공한다.
_DOC_SOURCE_ORG = {
    "장병내일준비적금": "KB국민은행 상품안내",
    "청년미래적금": "KB국민은행 상품안내",
    "청년주택드림청약통장": "KB국민은행 상품안내",
    "정책용어사전": "정책용어사전",
}
_DOC_AS_OF = "2026년 3월 기준"


def _build_source_detail(doc_name: Optional[str]) -> Optional[str]:
    if not doc_name:
        return None
    org = _DOC_SOURCE_ORG.get(doc_name, doc_name)
    if doc_name == "정책용어사전":
        return org
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
    "(예: '그거 얼마야?'처럼 이전 답변을 가리키는 질문이면 무엇을 가리키는지 이전 대화에서 찾아 답한다)."
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
    context: str
    source: str
    doc_names: List[str]  # RAG 검색결과 top_k의 doc_name (유사도 순), 실시간 데이터면 빈 리스트
    source_detail: Optional[str]  # 사람이 읽는 출처 캡션 (RAG 답변만 해당, 없으면 None)
    is_ai_generated: bool  # 프론트에 "AI가 생성한 답변입니다" 문구를 보여줄지 여부
    answer: str


def _format_history(history: List[Tuple[str, str]]) -> str:
    if not history:
        return ""
    lines = [f"{'사용자' if role == 'user' else '챗봇'}: {content}" for role, content in history]
    return "[이전 대화]\n" + "\n".join(lines) + "\n\n"


def _classify_intent_node(state: ChatState) -> ChatState:
    history_block = _format_history(state.get("history") or [])
    return {"intent": classify_intent(state["question"], history_block=history_block)}


def _classify_category_node(state: ChatState) -> ChatState:
    return {"product_category": classify_product_category(state["question"])}


_SPECIFIC_DOC_NAMES = ("장병내일준비적금", "청년미래적금", "청년주택드림청약통장")


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

    results = vectorstore.search_with_metadata(search_query, top_k=_TOP_K)
    context = "\n\n".join(text for text, _ in results)
    source = RAG_SOURCE_LABEL
    doc_names = [meta.get("doc_name") for _, meta in results if meta.get("doc_name")]
    return {"context": context, "source": source, "doc_names": doc_names, "is_ai_generated": True}


def _generate_node(state: ChatState) -> ChatState:
    category = state.get("product_category")
    history_block = _format_history(state.get("history") or [])
    if category:
        prompt = f"{history_block}[실시간 상품 데이터 - {category}]\n{state['context']}\n\n[질문]\n{state['question']}"
    else:
        prompt = f"{history_block}[참고 정책 문서]\n{state['context']}\n\n[질문]\n{state['question']}"
    answer = gemini_client.generate_content(prompt, system_instruction=SYSTEM_INSTRUCTION)

    source_detail = None
    if not category:
        doc_name = _pick_doc_name(answer, state.get("doc_names") or [])
        source_detail = _build_source_detail(doc_name)

    return {"answer": answer, "source_detail": source_detail}


def _irrelevant_node(state: ChatState) -> ChatState:
    return {
        "answer": IRRELEVANT_REPLY,
        "source": "무관련 질문 안내",
        "source_detail": None,
        "is_ai_generated": False,
    }


def _route_after_intent(state: ChatState) -> str:
    if state["intent"] == "irrelevant":
        return "irrelevant"
    if state["intent"] == "info":
        return "classify_category"
    return "build_context"  # counsel


_graph = StateGraph(ChatState)
_graph.add_node("classify_intent", _classify_intent_node)
_graph.add_node("classify_category", _classify_category_node)
_graph.add_node("build_context", _build_context_node)
_graph.add_node("generate", _generate_node)
_graph.add_node("irrelevant", _irrelevant_node)

_graph.add_edge(START, "classify_intent")
_graph.add_conditional_edges(
    "classify_intent",
    _route_after_intent,
    {"irrelevant": "irrelevant", "classify_category": "classify_category", "build_context": "build_context"},
)
_graph.add_edge("classify_category", "build_context")
_graph.add_edge("build_context", "generate")
_graph.add_edge("generate", END)
_graph.add_edge("irrelevant", END)

_compiled_graph = _graph.compile()


def generate_reply(
    question: str, history: Optional[List[Tuple[str, str]]] = None
) -> Tuple[str, str, Optional[str], bool]:
    """반환값: (답변, source 라벨, source_detail 캡션, is_ai_generated).
    LangGraph로 의도분류 → (분기) → 컨텍스트 구성 → 답변 생성을 수행한다.

    history: 같은 세션의 이전 메시지들 [(role, content), ...] (오래된 순, 현재 질문은 미포함).
    답변 생성 시 문맥으로 활용해 멀티턴 대화를 지원한다.
    """
    result = _compiled_graph.invoke({"question": question, "history": history or []})
    return (
        result["answer"],
        result["source"],
        result.get("source_detail"),
        result.get("is_ai_generated", False),
    )
