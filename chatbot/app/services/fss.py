import time
from typing import Dict, List, Tuple

import requests

from app.core.config import FSS_API_KEY

BASE_URL = "https://finlife.fss.or.kr/finlifeapi"

# 적금은 은행권+저축은행권 전체 상품을 페이지째 다 훑은 뒤에야 KB국민은행 것만 걸러내는 구조라,
# 캐싱 없이 매번 실시간 조회하면 30~50초씩 걸려서 프론트 axios 타임아웃(15초)에 걸려 취소돼버린다
# (2026-08-11, "적금" 카테고리만 매번 서버 오류로 뜨는 문제로 발견). 예금/대출은 그보다는 빠르지만
# 같은 구조라 언젠가 상품이 늘어나면 똑같이 느려질 수 있어 전부 캐싱한다.
# 금리는 하루에도 잘 안 바뀌는 데이터라 10분 캐싱해도 실사용엔 지장 없지만, 캐시가 만료되는
# 순간 "다음 한 번"은 여전히 느린 첫 조회를 그대로 타서 타임아웃이 재현됐다(2026-08-11, 재현 확인
# - 취소된 요청 뒤에 캐시가 채워져서 재시도하면 바로 성공하는 패턴). 이 순간 자체를 줄이려고
# 캐시 유지시간을 1시간으로 늘렸다(프론트 쪽 타임아웃도 별도로 60초로 늘려서 이중 방어).
_CACHE_TTL_SECONDS = 3600
_products_cache: Dict[str, Tuple[float, List[dict]]] = {}
_loan_products_cache: Dict[str, Tuple[float, List[dict]]] = {}

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
    """category: 'savings' 또는 'deposit'. 1시간 이내 재조회면 캐시를 그대로 반환한다."""
    cached = _products_cache.get(category)
    if cached and time.time() - cached[0] < _CACHE_TTL_SECONDS:
        return cached[1]
    result = _fetch_products_live(category)
    _products_cache[category] = (time.time(), result)
    return result


def _fetch_products_live(category: str) -> List[dict]:
    """은행권+저축은행권을 합쳐서 상품코드 기준으로 옵션(금리)을 묶어 반환한다(캐시 없이 실시간 조회)."""
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
    """category: 'mortgage' | 'jeonse' | 'creditLoan'. 1시간 이내 재조회면 캐시를 그대로 반환한다."""
    cached = _loan_products_cache.get(category)
    if cached and time.time() - cached[0] < _CACHE_TTL_SECONDS:
        return cached[1]
    result = _fetch_loan_products_live(category)
    _loan_products_cache[category] = (time.time(), result)
    return result


def _fetch_loan_products_live(category: str) -> List[dict]:
    """KB국민은행 상품만 걸러서 반환한다(캐시 없이 실시간 조회)."""
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


# 캐시가 "비어있는 순간"(서버 첫 기동 직후, 또는 TTL 만료 직후) 자체를 없애기 위한 예열 함수.
# TTL 체크 없이 무조건 실시간(_live) 함수를 호출해서 캐시를 강제로 새로 채운다(2026-08-11 추가).
# main.py의 lifespan에서 (1) 서버 기동 시 1회, (2) TTL(1시간)이 지나기 전에 미리 주기적으로
# 호출해서, 사용자가 캐시 만료 타이밍에 걸려 30~50초 콜드 조회를 직접 겪는 일이 없게 한다.
PRODUCT_CATEGORIES_TO_WARM = ["savings", "deposit"]
LOAN_CATEGORIES_TO_WARM = ["mortgage", "jeonse", "creditLoan"]


def warm_cache() -> None:
    """모든 카테고리를 실시간 조회해서 캐시를 강제로 갱신한다. 동기/블로킹 함수라 호출하는 쪽에서
    asyncio.to_thread 등으로 이벤트 루프를 막지 않게 감싸서 써야 한다."""
    for category in PRODUCT_CATEGORIES_TO_WARM:
        try:
            result = _fetch_products_live(category)
            _products_cache[category] = (time.time(), result)
        except Exception:
            # 예열 실패는 그냥 넘어간다 - 다음 예열 주기 때 다시 시도되고, 그 사이엔 기존
            # 캐시(있다면)나 사용자 요청 시 콜드 조회로 자연스럽게 대체된다.
            pass

    for category in LOAN_CATEGORIES_TO_WARM:
        try:
            result = _fetch_loan_products_live(category)
            _loan_products_cache[category] = (time.time(), result)
        except Exception:
            pass
