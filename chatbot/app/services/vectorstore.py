from pathlib import Path
from typing import List

import chromadb
from google import genai
from google.genai import types

from app.core.config import GEMINI_API_KEY
from app.services.policy_docs import chunk_policy_docs

EMBED_MODEL = "gemini-embedding-001"
EMBED_DIM = 768
COLLECTION_NAME = "policy_docs"

_CHROMA_DIR = Path(__file__).resolve().parent.parent.parent / "chroma_db"

_client = genai.Client(api_key=GEMINI_API_KEY)
_db = chromadb.PersistentClient(path=str(_CHROMA_DIR))
_collection = _db.get_or_create_collection(COLLECTION_NAME)


def _embed(text: str, task_type: str) -> List[float]:
    response = _client.models.embed_content(
        model=EMBED_MODEL,
        contents=text,
        config=types.EmbedContentConfig(output_dimensionality=EMBED_DIM, task_type=task_type),
    )
    return response.embeddings[0].values


def build_index(force: bool = False) -> int:
    """정책문서를 청킹해서 임베딩 생성 후 Chroma에 저장한다. 이미 인덱싱돼 있으면 force=True일 때만 재생성."""
    global _collection
    if _collection.count() > 0 and not force:
        return _collection.count()

    if force:
        _db.delete_collection(COLLECTION_NAME)
        _collection = _db.get_or_create_collection(COLLECTION_NAME)

    chunks = chunk_policy_docs()
    ids = [f"{c['doc_name']}-{i}" for i, c in enumerate(chunks)]
    embeddings = [_embed(c["text"], "RETRIEVAL_DOCUMENT") for c in chunks]
    documents = [c["text"] for c in chunks]
    metadatas = [{"doc_name": c["doc_name"], "section": c["section"]} for c in chunks]

    _collection.add(ids=ids, embeddings=embeddings, documents=documents, metadatas=metadatas)
    return len(chunks)


def search(query: str, top_k: int = 3) -> List[str]:
    query_embedding = _embed(query, "RETRIEVAL_QUERY")
    results = _collection.query(query_embeddings=[query_embedding], n_results=top_k)
    return results["documents"][0]
