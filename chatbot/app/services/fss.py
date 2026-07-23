from typing import Dict, List

import requests

from app.core.config import FSS_API_KEY

BASE_URL = "https://finlife.fss.or.kr/finlifeapi"

# 은행권, 저축은행권
BANK_GROUPS = ["020000", "030300"]

PRODUCT_ENDPOINTS = {
    "savings": "savingProductsSearch.json",
    "deposit": "depositProductsSearch.json",
}

SOURCE_LABEL = "금융감독원 금융상품 한눈에 API"


def _fetch_page(endpoint: str, top_fin_grp_no: str, page_no: int) -> dict:
    response = requests.get(
        f"{BASE_URL}/{endpoint}",
        params={"auth": FSS_API_KEY, "topFinGrpNo": top_fin_grp_no, "pageNo": page_no},
        timeout=20,
    )
    response.raise_for_status()
    return response.json()["result"]


def fetch_products(category: str) -> List[dict]:
    """category: 'savings' 또는 'deposit'. 은행권+저축은행권을 합쳐서 상품코드 기준으로 옵션(금리)을 묶어 반환한다."""
    endpoint = PRODUCT_ENDPOINTS[category]
    products_by_code: Dict[str, dict] = {}

    for top_fin_grp_no in BANK_GROUPS:
        page_no = 1
        while True:
            result = _fetch_page(endpoint, top_fin_grp_no, page_no)

            for base in result["baseList"]:
                code = base["fin_prdt_cd"]
                products_by_code[code] = {
                    "fin_prdt_cd": code,
                    "kor_co_nm": base["kor_co_nm"],
                    "fin_prdt_nm": base["fin_prdt_nm"],
                    "join_way": base["join_way"],
                    "join_member": base["join_member"],
                    "spcl_cnd": base["spcl_cnd"],
                    "etc_note": base["etc_note"],
                    "options": [],
                }

            for option in result["optionList"]:
                code = option["fin_prdt_cd"]
                if code in products_by_code:
                    products_by_code[code]["options"].append(
                        {
                            "save_trm": option["save_trm"],
                            "intr_rate": option.get("intr_rate"),
                            "intr_rate2": option.get("intr_rate2"),
                        }
                    )

            if page_no >= result["max_page_no"]:
                break
            page_no += 1

    return list(products_by_code.values())


def max_rate(product: dict) -> float:
    rates = [
        option["intr_rate2"] or option["intr_rate"] or 0
        for option in product["options"]
    ]
    return max(rates, default=0)
