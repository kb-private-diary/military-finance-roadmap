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
