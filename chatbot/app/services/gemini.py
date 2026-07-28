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

SYSTEM_INSTRUCTION = (
    "너는 군장병을 위한 재무 상담 챗봇이다. 아래 [참고 자료]에 있는 내용만 근거로 답변한다. "
    "자료에 없는 내용은 모른다고 솔직히 답한다. "
    "[참고 자료]에 있는 구체적인 수치·조건·절차는 생략하지 말고 최대한 빠짐없이 답변에 포함한다 — "
    "짧게 요약하기보다는, 관련된 내용을 자료에서 찾을 수 있는 만큼 충분히 설명한다. "
    "군대에서 쓰는 친근하고 예의바른 다나까 말투로 답한다. 아래 종결어미만 사용한다:\n"
    "- '~합니다', '~됩니다', '~입니다' (가장 기본적인 설명)\n"
    "- '~하지 말입니다' (설명을 부드럽게 덧붙일 때, 예: '자세한 조건은 다를 수 있지 말입니다')\n"
    "- '~하십니까?' (질문에 되물을 때)\n"
    "무뚝뚝한 명령조 평서문('~다', '~해라')이나 반말·부드러운 종결어미(~해요, ~예요)는 쓰지 않는다. "
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
    answer: str


def _format_history(history: List[Tuple[str, str]]) -> str:
    if not history:
        return ""
    lines = [f"{'사용자' if role == 'user' else '챗봇'}: {content}" for role, content in history]
    return "[이전 대화]\n" + "\n".join(lines) + "\n\n"


def _classify_intent_node(state: ChatState) -> ChatState:
    return {"intent": classify_intent(state["question"])}


def _classify_category_node(state: ChatState) -> ChatState:
    return {"product_category": classify_product_category(state["question"])}


def _build_context_node(state: ChatState) -> ChatState:
    category = state.get("product_category")
    if category:
        context = _build_product_context(category)
        source = _LIVE_SOURCE_LABELS[category]
    else:
        context_chunks = vectorstore.search(state["question"], top_k=_TOP_K)
        context = "\n\n".join(context_chunks)
        source = RAG_SOURCE_LABEL
    return {"context": context, "source": source}


def _generate_node(state: ChatState) -> ChatState:
    category = state.get("product_category")
    history_block = _format_history(state.get("history") or [])
    if category:
        prompt = f"{history_block}[실시간 상품 데이터 - {category}]\n{state['context']}\n\n[질문]\n{state['question']}"
    else:
        prompt = f"{history_block}[참고 정책 문서]\n{state['context']}\n\n[질문]\n{state['question']}"
    answer = gemini_client.generate_content(prompt, system_instruction=SYSTEM_INSTRUCTION)
    return {"answer": answer}


def _irrelevant_node(state: ChatState) -> ChatState:
    return {"answer": IRRELEVANT_REPLY, "source": "무관련 질문 안내"}


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


def generate_reply(question: str, history: Optional[List[Tuple[str, str]]] = None) -> Tuple[str, str]:
    """반환값: (답변, source 라벨). LangGraph로 의도분류 → (분기) → 컨텍스트 구성 → 답변 생성을 수행한다.

    history: 같은 세션의 이전 메시지들 [(role, content), ...] (오래된 순, 현재 질문은 미포함).
    답변 생성 시 문맥으로 활용해 멀티턴 대화를 지원한다.
    """
    result = _compiled_graph.invoke({"question": question, "history": history or []})
    return result["answer"], result["source"]
