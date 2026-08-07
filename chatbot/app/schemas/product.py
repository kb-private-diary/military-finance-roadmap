from typing import List, Optional

from app.schemas.base import CamelModel


class ProductOption(CamelModel):
    save_trm: str
    intr_rate: Optional[float]
    intr_rate2: Optional[float]


class ProductItem(CamelModel):
    fin_prdt_cd: str
    kor_co_nm: str
    fin_prdt_nm: str
    max_rate: Optional[float]


class ProductDetail(CamelModel):
    fin_prdt_cd: str
    kor_co_nm: str
    fin_prdt_nm: str
    join_way: str
    join_member: str
    spcl_cnd: str
    etc_note: str
    options: List[ProductOption]
    source: str
    source_url: Optional[str] = None  # 은행별 개별 상품 페이지는 없어서, 이 데이터가 나온 비교 페이지로 연결


class SubscriptionItem(CamelModel):
    house_manage_no: str
    house_nm: str
    rcept_bgnde: Optional[str]
    rcept_endde: Optional[str]
    mvn_prearnge_ym: Optional[str]


class SubscriptionDetail(CamelModel):
    house_manage_no: str
    house_nm: str
    hssply_adres: Optional[str]
    rcept_bgnde: Optional[str]
    rcept_endde: Optional[str]
    przwner_presnatn_de: Optional[str]
    mvn_prearnge_ym: Optional[str]
    pblanc_url: Optional[str]
    source: str


class FundItem(CamelModel):
    srtn_cd: str
    fnd_nm: str
    fnd_tp: Optional[str]


class FundDetail(CamelModel):
    srtn_cd: str
    fnd_nm: str
    ctg: Optional[str]
    setp_dt: Optional[str]
    fnd_tp: Optional[str]
    source: str


# 대출(주택담보대출/전세자금대출/개인신용대출) - 적금/예금과 달리 상품마다 금리가 "범위"(최저~최고)로
# 나오거나(주담대/전세자금), 신용등급 구간별로 나온다(개인신용대출 - crdt_grad_1~13). 개인신용대출은
# min/max가 없어서 fss.py에서 crdt_grad_avg를 lend_rate_avg 자리에 매핑해 하나의 스키마로 통일한다.
class LoanOption(CamelModel):
    lend_rate_type_nm: Optional[str] = None
    lend_rate_min: Optional[float] = None
    lend_rate_max: Optional[float] = None
    lend_rate_avg: Optional[float] = None


class LoanItem(CamelModel):
    fin_prdt_cd: str
    kor_co_nm: str
    fin_prdt_nm: str
    rate: Optional[float] = None  # 최저금리(대표) - 대출은 낮을수록 유리해서 적금/예금(최고금리)과 반대


class LoanDetail(CamelModel):
    fin_prdt_cd: str
    kor_co_nm: str
    fin_prdt_nm: str
    join_way: str
    loan_lmt: Optional[str] = None
    erly_rpay_fee: Optional[str] = None  # 중도상환수수료
    cb_name: Optional[str] = None  # 신용평가사 (개인신용대출만 있음)
    options: List[LoanOption]
    source: str
    source_url: Optional[str] = None
