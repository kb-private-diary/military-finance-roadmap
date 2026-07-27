from typing import List, Optional

import requests

from app.core.config import ONTONG_YOUTH_API_KEY

BASE_URL = "https://www.youthcenter.go.kr/go/ythip/getPlcy"

SOURCE_LABEL = "온통청년 청년정책 API"


def search_policies(plcy_nm: str, display: int = 5) -> List[dict]:
    response = requests.get(
        BASE_URL,
        params={"apiKeyNm": ONTONG_YOUTH_API_KEY, "pageIndex": 1, "display": display, "plcyNm": plcy_nm},
        timeout=15,
    )
    response.raise_for_status()
    return response.json()["result"]["youthPolicyList"]


def find_policy(plcy_nm: str) -> Optional[dict]:
    results = search_policies(plcy_nm, display=1)
    return results[0] if results else None
