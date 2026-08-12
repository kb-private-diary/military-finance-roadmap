import time
from typing import List, Optional, Tuple

import requests

from app.core.config import FUND_API_KEY

BASE_URL = "https://apis.data.go.kr/1160100/service/GetFundProductInfoService/getStandardCodeInfo"

SOURCE_LABEL = "금융위원회 펀드상품기본정보 (금융투자협회 펀드표준코드)"


def _get_items(params: dict) -> List[dict]:
    response = requests.get(
        BASE_URL, params={**params, "serviceKey": FUND_API_KEY, "resultType": "json"}, timeout=30
    )
    response.raise_for_status()
    body = response.json()["response"]["body"]
    item = body.get("items", {}).get("item", [])
    if isinstance(item, dict):
        return [item]
    return item


def fetch_funds(num_of_rows: int = 20) -> List[dict]:
    return _get_items({"numOfRows": num_of_rows, "pageNo": 1})


def find_fund(fnd_nm: str) -> Optional[dict]:
    items = _get_items({"numOfRows": 1, "pageNo": 1, "fndNm": fnd_nm})
    return items[0] if items else None


# 서비스가 KB 상품 전용인데, 이 API(금융투자협회 펀드표준코드)엔 자산운용사 이름 필드가 아예 없다
# (fndNm/ctg/setpDt/fndTp/prdClsfCd/asoStdCd 뿐). 대신 KB자산운용 펀드는 상품명이 전부 "KB "로
# 시작하는 걸 실제 데이터로 확인해서(2026-08-12, 5000건 스캔 - 555건 매치, 오탐 없음) 이름 접두사로
# 거른다. fndNm 파라미터는 부분일치 검색이 안 되고(정확히 일치하는 것만 반환) API에 totalCount가
# 18만건대라 전량 조회는 못 하고, 5000건이면 1초 안팎으로 빠르면서 위험성향(FUND_RISK_TYPE) 7종류
# 전부 충분히 커버돼서 이 크기로 스캔한다.
_KB_NAME_PREFIX = "KB "
_SCAN_ROWS = 5000
_CACHE_TTL_SECONDS = 3600
_kb_funds_cache: Optional[Tuple[float, List[dict]]] = None


def fetch_kb_funds() -> List[dict]:
    global _kb_funds_cache
    if _kb_funds_cache and time.time() - _kb_funds_cache[0] < _CACHE_TTL_SECONDS:
        return _kb_funds_cache[1]
    result = _fetch_kb_funds_live()
    _kb_funds_cache = (time.time(), result)
    return result


def _fetch_kb_funds_live() -> List[dict]:
    items = _get_items({"numOfRows": _SCAN_ROWS, "pageNo": 1})
    return [f for f in items if f.get("fndNm", "").startswith(_KB_NAME_PREFIX)]


def warm_cache() -> None:
    """fss.warm_cache와 같은 목적 - 캐시가 비어있는 순간(첫 조회) 콜드 스캔을 겪지 않도록 미리 채운다."""
    global _kb_funds_cache
    try:
        _kb_funds_cache = (time.time(), _fetch_kb_funds_live())
    except Exception:
        pass
