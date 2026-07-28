from pathlib import Path
from typing import List

import chromadb
from google import genai
from google.genai import types
from langchain_chroma import Chroma
from langchain_google_genai import GoogleGenerativeAIEmbeddings

from app.core.config import GEMINI_API_KEY
from app.services.policy_docs import chunk_policy_docs

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


def _populate(collection) -> int:
    """청크를 임베딩해서 주어진 컬렉션에 채워 넣는다. 실패하면 예외를 그대로 던진다."""
    chunks = chunk_policy_docs()
    ids = [f"{c['doc_name']}-{i}" for i, c in enumerate(chunks)]
    embeddings = [_embed(c["text"], "RETRIEVAL_DOCUMENT") for c in chunks]
    documents = [c["text"] for c in chunks]
    metadatas = [{"doc_name": c["doc_name"], "section": c["section"]} for c in chunks]

    collection.add(ids=ids, embeddings=embeddings, documents=documents, metadatas=metadatas)
    return len(chunks)


def build_index(force: bool = False) -> int:
    """정책문서를 청킹해서 임베딩 생성 후 Chroma에 저장한다. 이미 인덱싱돼 있으면 force=True일 때만 재생성.

    force=True일 때는 기존 컬렉션을 바로 지우지 않는다 — 청킹·임베딩 도중 오류가 나면
    검색 결과가 비거나 일부만 남는 상태가 될 수 있기 때문이다. 대신 임시 컬렉션에 먼저
    새로 채우고, 성공적으로 검증된 뒤에만 기존 것을 지우고 이름을 바꿔치기한다.
    """
    global _collection, _vectorstore
    if _collection.count() > 0 and not force:
        return _collection.count()

    if not force:
        return _populate(_collection)

    tmp_name = f"{COLLECTION_NAME}_rebuild"
    try:
        _db.delete_collection(tmp_name)
    except Exception:
        pass  # 이전에 실패한 재시도 잔여물이 있으면 정리, 없으면 무시

    tmp_collection = _db.get_or_create_collection(tmp_name)
    try:
        count = _populate(tmp_collection)
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
    return count


def search(query: str, top_k: int = 3) -> List[str]:
    """LangChain Retriever(Chroma)를 통해 의미 기반 검색을 수행한다."""
    docs = _vectorstore.similarity_search(query, k=top_k)
    return [doc.page_content for doc in docs]
