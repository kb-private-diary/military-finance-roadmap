from typing import List, Optional

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
