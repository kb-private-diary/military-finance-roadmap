from pathlib import Path

POLICY_DIR = Path(__file__).resolve().parent.parent.parent / "data" / "policies"


def load_policy_docs() -> str:
    """정책 문서 전문을 하나로 합쳐서 반환한다 (임베딩·벡터검색 없이 프롬프트에 그대로 포함하는 간이 RAG)."""
    docs = []
    for path in sorted(POLICY_DIR.glob("*.txt")):
        docs.append(f"### {path.stem}\n{path.read_text(encoding='utf-8')}")
    return "\n\n".join(docs)
