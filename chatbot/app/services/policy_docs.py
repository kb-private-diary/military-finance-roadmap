import re
from pathlib import Path
from typing import List, TypedDict

POLICY_DIR = Path(__file__).resolve().parent.parent.parent / "data" / "policies"

_SECTION_HEADER = re.compile(r"^\[(.+)\]$", re.MULTILINE)


class PolicyChunk(TypedDict):
    doc_name: str
    section: str
    text: str


def load_policy_docs() -> str:
    """정책 문서 전문을 하나로 합쳐서 반환한다 (청킹 없이 전체를 프롬프트에 넣는 용도)."""
    docs = []
    for path in sorted(POLICY_DIR.glob("*.txt")):
        docs.append(f"### {path.stem}\n{path.read_text(encoding='utf-8')}")
    return "\n\n".join(docs)


def chunk_policy_docs() -> List[PolicyChunk]:
    """정책 문서를 `[섹션제목]` 단위로 청킹한다. 문서가 전부 이 형식으로 작성돼 있어서 문단 청킹보다 의미 단위가 뚜렷하다."""
    chunks: List[PolicyChunk] = []
    for path in sorted(POLICY_DIR.glob("*.txt")):
        text = path.read_text(encoding="utf-8")
        headers = list(_SECTION_HEADER.finditer(text))
        for i, match in enumerate(headers):
            section = match.group(1)
            start = match.end()
            end = headers[i + 1].start() if i + 1 < len(headers) else len(text)
            body = text[start:end].strip()
            if body:
                chunks.append({"doc_name": path.stem, "section": section, "text": f"[{section}]\n{body}"})
    return chunks
