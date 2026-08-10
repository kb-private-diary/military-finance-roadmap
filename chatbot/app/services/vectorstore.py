import hashlib
from collections import defaultdict
from pathlib import Path
from typing import Dict, List, Tuple

import chromadb
from google import genai
from google.genai import types
from langchain_chroma import Chroma
from langchain_google_genai import GoogleGenerativeAIEmbeddings

from app.core.config import GEMINI_API_KEY
from app.services.policy_docs import POLICY_DIR, PolicyChunk, chunk_policy_docs

EMBED_MODEL = "gemini-embedding-001"
EMBED_DIM = 768
COLLECTION_NAME = "policy_docs"

_CHROMA_DIR = Path(__file__).resolve().parent.parent.parent / "chroma_db"

_client = genai.Client(api_key=GEMINI_API_KEY)
_db = chromadb.PersistentClient(path=str(_CHROMA_DIR))
_collection = _db.get_or_create_collection(COLLECTION_NAME)

# 검색(쿼리)은 LangChain 표준 Embeddings 인터페이스로 감싸서 Chroma를 LangChain
# Retriever로 사용한다. 인덱싱(build_index)은 배치성 작업이라 기존 방식(_embed) 그대로 유지한다.
_query_embeddings = GoogleGenerativeAIEmbeddings(
    model=EMBED_MODEL,
    google_api_key=GEMINI_API_KEY,
    task_type="RETRIEVAL_QUERY",
    output_dimensionality=EMBED_DIM,
)
_vectorstore = Chroma(client=_db, collection_name=COLLECTION_NAME, embedding_function=_query_embeddings)


def _embed(text: str, task_type: str) -> List[float]:
    response = _client.models.embed_content(
        model=EMBED_MODEL,
        contents=text,
        config=types.EmbedContentConfig(output_dimensionality=EMBED_DIM, task_type=task_type),
    )
    return response.embeddings[0].values


def _doc_hashes() -> Dict[str, str]:
    """문서(파일)별 내용 해시. 청킹이 파일 내용만으로 결정되는 순수 함수라, 해시가 같으면
    청킹 결과(섹션 개수·내용)도 항상 똑같다는 걸 보장할 수 있다 - 그래서 파일 단위 해시로
    "이 문서가 바뀌었는지"를 판단해도 된다(청크 단위로 따로 비교할 필요 없음)."""
    return {
        path.stem: hashlib.sha256(path.read_text(encoding="utf-8").encode("utf-8")).hexdigest()[:16]
        for path in sorted(POLICY_DIR.glob("*.txt"))
    }


def _chunks_by_doc() -> Dict[str, List[PolicyChunk]]:
    grouped: Dict[str, List[PolicyChunk]] = defaultdict(list)
    for chunk in chunk_policy_docs():
        grouped[chunk["doc_name"]].append(chunk)
    return grouped


def _existing_items_by_doc(collection) -> Dict[str, dict]:
    """기존 컬렉션에 이미 임베딩돼 있는 청크를 문서별로 모아서, 재사용 가능한 형태로 반환한다.
    문서마다 {"hash": 그때 임베딩할 때의 파일 해시, "ids"/"embeddings"/"documents"/"metadatas": 청크들}"""
    if collection is None or collection.count() == 0:
        return {}
    existing = collection.get(include=["embeddings", "documents", "metadatas"])
    grouped: Dict[str, dict] = {}
    for id_, embedding, document, metadata in zip(
        existing["ids"], existing["embeddings"], existing["documents"], existing["metadatas"]
    ):
        entry = grouped.setdefault(
            metadata.get("doc_name"),
            {"hash": metadata.get("doc_hash"), "ids": [], "embeddings": [], "documents": [], "metadatas": []},
        )
        entry["ids"].append(id_)
        # collection.get()이 돌려주는 embedding은 numpy array라, _embed()가 만드는 일반 list와
        # 섞어서 collection.add()에 넘기면 Chroma가 타입 불일치로 거부한다 - list로 통일한다.
        entry["embeddings"].append(list(embedding))
        entry["documents"].append(document)
        entry["metadatas"].append(metadata)
    return grouped


def _populate(collection, reuse_from=None) -> Tuple[int, int]:
    """청크를 임베딩해서 주어진 컬렉션에 채워 넣는다. reuse_from(기존 컬렉션)이 주어지면,
    파일 해시가 안 바뀐 문서는 거기서 이미 계산된 임베딩을 그대로 재사용하고(API 호출 없음),
    새 문서거나 내용이 바뀐 문서만 새로 임베딩한다 - 문서가 많아질수록 재인덱싱 비용이
    문서 수에 비례해 계속 커지는 문제를 완화한다(2026-08-09, 멘토 피드백 반영).
    반환값: (전체 청크 수, 그중 새로 임베딩한 청크 수).
    """
    hashes = _doc_hashes()
    chunks_by_doc = _chunks_by_doc()
    existing_by_doc = _existing_items_by_doc(reuse_from)

    ids, embeddings, documents, metadatas = [], [], [], []
    reembedded = 0
    for doc_name, doc_chunks in chunks_by_doc.items():
        doc_hash = hashes[doc_name]
        existing = existing_by_doc.get(doc_name)
        if existing and existing["hash"] == doc_hash and len(existing["ids"]) == len(doc_chunks):
            # 파일 해시가 그대로라 청킹 결과도 동일 - 기존 임베딩을 그대로 재사용
            ids.extend(existing["ids"])
            embeddings.extend(existing["embeddings"])
            documents.extend(existing["documents"])
            metadatas.extend(existing["metadatas"])
            continue

        # 새 문서거나 내용이 바뀐 문서 - 새로 임베딩
        for i, c in enumerate(doc_chunks):
            ids.append(f"{doc_name}-{i}")
            embeddings.append(_embed(c["text"], "RETRIEVAL_DOCUMENT"))
            documents.append(c["text"])
            metadatas.append({"doc_name": doc_name, "section": c["section"], "doc_hash": doc_hash})
            reembedded += 1

    if ids:
        collection.add(ids=ids, embeddings=embeddings, documents=documents, metadatas=metadatas)
    return len(ids), reembedded


def build_index(force: bool = False) -> Tuple[int, int]:
    """정책문서를 청킹해서 임베딩 생성 후 Chroma에 저장한다. 이미 인덱싱돼 있으면 force=True일 때만 재생성.

    force=True일 때는 기존 컬렉션을 바로 지우지 않는다 — 청킹·임베딩 도중 오류가 나면
    검색 결과가 비거나 일부만 남는 상태가 될 수 있기 때문이다. 대신 임시 컬렉션에 먼저
    새로 채우고, 성공적으로 검증된 뒤에만 기존 것을 지우고 이름을 바꿔치기한다.

    반환값: (전체 청크 수, 그중 새로 임베딩한 청크 수 - 나머지는 기존 임베딩 재사용).
    """
    global _collection, _vectorstore
    if _collection.count() > 0 and not force:
        return _collection.count(), 0

    if not force:
        return _populate(_collection)

    tmp_name = f"{COLLECTION_NAME}_rebuild"
    try:
        _db.delete_collection(tmp_name)
    except Exception:
        pass  # 이전에 실패한 재시도 잔여물이 있으면 정리, 없으면 무시

    tmp_collection = _db.get_or_create_collection(tmp_name)
    try:
        count, reembedded = _populate(tmp_collection, reuse_from=_collection)
    except Exception:
        _db.delete_collection(tmp_name)
        raise

    if count == 0:
        _db.delete_collection(tmp_name)
        raise RuntimeError("재인덱싱 결과가 비어 있어 기존 인덱스를 유지합니다")

    _db.delete_collection(COLLECTION_NAME)
    tmp_collection.modify(name=COLLECTION_NAME)
    _collection = tmp_collection
    _vectorstore = Chroma(client=_db, collection_name=COLLECTION_NAME, embedding_function=_query_embeddings)
    return count, reembedded


def search_with_metadata(query: str, top_k: int = 3) -> List[Tuple[str, dict]]:
    """LangChain Retriever(Chroma)를 통해 의미 기반 검색을 수행하고, 청크 본문과 메타데이터(doc_name, section)를 함께 반환한다."""
    docs = _vectorstore.similarity_search(query, k=top_k)
    return [(doc.page_content, doc.metadata) for doc in docs]


def search(query: str, top_k: int = 3) -> List[str]:
    """LangChain Retriever(Chroma)를 통해 의미 기반 검색을 수행한다."""
    return [text for text, _ in search_with_metadata(query, top_k)]
