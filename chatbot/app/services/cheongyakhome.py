from typing import List, Optional

import requests

from app.core.config import CHEONGYAKHOME_API_KEY

BASE_URL = "https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getAPTLttotPblancDetail"

SOURCE_LABEL = "한국부동산원 청약홈 분양정보 조회 서비스"


def fetch_listings(per_page: int = 20) -> List[dict]:
    response = requests.get(
        BASE_URL,
        params={"page": 1, "perPage": per_page, "serviceKey": CHEONGYAKHOME_API_KEY},
        timeout=15,
    )
    response.raise_for_status()
    return response.json()["data"]


def find_listing(house_nm: str) -> Optional[dict]:
    response = requests.get(
        BASE_URL,
        params={
            "page": 1,
            "perPage": 1,
            "serviceKey": CHEONGYAKHOME_API_KEY,
            "cond[HOUSE_NM::EQ]": house_nm,
        },
        timeout=15,
    )
    response.raise_for_status()
    data = response.json()["data"]
    return data[0] if data else None
