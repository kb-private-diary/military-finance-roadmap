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

SOURCE_LABEL = "금융감독원 금융상품한눈에"

# 카테고리별 비교 페이지 링크 - 개별 상품(은행마다 다름)까지는 못 짚어줘도, 이 데이터가
# 나온 비교 페이지로는 보낼 수 있다. 둘 다 직접 열어서 정상 동작 확인함(2026-08-06).
# 대출 3종도 실제로 열어서 확인한 링크(2026-08-07).
SOURCE_URL = {
    "savings": "https://finlife.fss.or.kr/finlife/svings/fdrmEnty/list.do?menuNo=700003",
    "deposit": "https://finlife.fss.or.kr/finlife/svings/fdrmDpst/list.do?menuNo=700002",
    "mortgage": "https://finlife.fss.or.kr/finlife/ldng/houseMrtg/list.do?menuNo=700007",
    "jeonse": "https://finlife.fss.or.kr/finlife/ldng/lfstsFunds/list.do?menuNo=700008",
    "creditLoan": "https://finlife.fss.or.kr/finlife/ldng/indvlCrdt/list.do?menuNo=700009",
}

# 서비스가 KB국민은행 전용이라, 적금/예금/대출 모두 이 은행 상품만 걸러서 보여준다.
# 은행명은 API에서 "국민은행"으로 내려오지 "KB국민은행"이 아니다 - 부분일치 말고 정확히 비교해야
# "국민은행" 문자열이 우연히 들어간 다른 은행(있다면)까지 잘못 걸리는 걸 막는다.
KB_BANK_NAME = "국민은행"


def _filter_kb(products: List[dict]) -> List[dict]:
    return [p for p in products if p["kor_co_nm"] == KB_BANK_NAME]


# 대출은 상품 스키마가 적금/예금과 달라서(금리가 min/max 범위거나 신용등급 구간) 별도 함수로 뺐다.
# 개인신용대출(creditLoan)은 optionList에 lend_rate_min/max가 아예 없고 crdt_grad_1~13(신용등급
# 구간별 금리)/crdt_grad_avg만 있어서, 프론트/스키마를 하나로 통일하려고 crdt_grad_avg를
# lend_rate_avg 자리에 매핑해서 넣는다(2026-08-07, 실제 API 응답으로 필드명 확인함).
LOAN_ENDPOINTS = {
    "mortgage": "mortgageLoanProductsSearch.json",
    "jeonse": "rentHouseLoanProductsSearch.json",
}
CREDIT_LOAN_ENDPOINT = "creditLoanProductsSearch.json"


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

    return _filter_kb(list(products_by_code.values()))


def max_rate(product: dict) -> float:
    rates = [
        option["intr_rate2"] or option["intr_rate"] or 0
        for option in product["options"]
    ]
    return max(rates, default=0)


def fetch_loan_products(category: str) -> List[dict]:
    """category: 'mortgage' | 'jeonse' | 'creditLoan'. KB국민은행 상품만 걸러서 반환한다."""
    endpoint = LOAN_ENDPOINTS.get(category, CREDIT_LOAN_ENDPOINT)
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
                    "loan_lmt": base.get("loan_lmt"),
                    "erly_rpay_fee": base.get("erly_rpay_fee"),
                    "cb_name": base.get("cb_name"),
                    "options": [],
                }

            for option in result["optionList"]:
                code = option["fin_prdt_cd"]
                if code not in products_by_code:
                    continue
                if category == "creditLoan":
                    products_by_code[code]["options"].append(
                        {
                            "lend_rate_type_nm": option.get("crdt_lend_rate_type_nm"),
                            "lend_rate_min": None,
                            "lend_rate_max": None,
                            "lend_rate_avg": option.get("crdt_grad_avg"),
                        }
                    )
                else:
                    products_by_code[code]["options"].append(
                        {
                            "lend_rate_type_nm": option.get("lend_rate_type_nm"),
                            "lend_rate_min": option.get("lend_rate_min"),
                            "lend_rate_max": option.get("lend_rate_max"),
                            "lend_rate_avg": option.get("lend_rate_avg"),
                        }
                    )

            if page_no >= result["max_page_no"]:
                break
            page_no += 1

    return _filter_kb(list(products_by_code.values()))


def representative_loan_rate(product: dict) -> float:
    """대표 금리(최저금리) - 대출은 낮을수록 유리해서 적금/예금(max_rate)과 반대로 최저값을 쓴다."""
    rates = [
        option["lend_rate_min"] if option["lend_rate_min"] is not None else option["lend_rate_avg"]
        for option in product["options"]
        if option["lend_rate_min"] is not None or option["lend_rate_avg"] is not None
    ]
    return min(rates, default=0)
