-- --------------------------------------------------------------------
--  [석윤] 공통/ KB 예적금상품(saving_product)
--  테이블: saving_product
-- --------------------------------------------------------------------
INSERT INTO `saving_product` 
(`saving_id`, `product_type`, `is_active`, `fin_prdt_cd`, `kor_co_nm`, `product_name`, `join_member`, `min_limit`, `max_limit`, `etc_note`, `intr_rate_type`, `rsrv_type`, `save_trm`, `spcl_cnd`, `basic_rate`, `max_rate`, `is_tax_exempt`, `gov_match_rate`, `product_link`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES 
(1, 'DEPOSIT', TRUE, '010300100335', '국민은행', 'KB Star 정기예금', '실명의 개인 또는 개인사업자', 1000000, NULL, '- 가입금액 : 1백만원 이상', 'S', 'NONE', 1, '해당무', 1.8, 2.45, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000938', NOW(), 'seokyun', NULL, NULL, 'N'),
(2, 'DEPOSIT', TRUE, '010300100335', '국민은행', 'KB Star 정기예금', '실명의 개인 또는 개인사업자', 1000000, NULL, '- 가입금액 : 1백만원 이상', 'S', 'NONE', 3, '해당무', 2.0, 2.75, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000938', NOW(), 'seokyun', NULL, NULL, 'N'),
(3, 'DEPOSIT', TRUE, '010300100335', '국민은행', 'KB Star 정기예금', '실명의 개인 또는 개인사업자', 1000000, NULL, '- 가입금액 : 1백만원 이상', 'S', 'NONE', 6, '해당무', 2.1, 2.85, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000938', NOW(), 'seokyun', NULL, NULL, 'N'),
(4, 'DEPOSIT', TRUE, '010300100335', '국민은행', 'KB Star 정기예금', '실명의 개인 또는 개인사업자', 1000000, NULL, '- 가입금액 : 1백만원 이상', 'S', 'NONE', 12, '해당무', 2.15, 2.9, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000938', NOW(), 'seokyun', NULL, NULL, 'N'),
(5, 'DEPOSIT', TRUE, '010300100335', '국민은행', 'KB Star 정기예금', '실명의 개인 또는 개인사업자', 1000000, NULL, '- 가입금액 : 1백만원 이상', 'S', 'NONE', 24, '해당무', 2.2, 2.4, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000938', NOW(), 'seokyun', NULL, NULL, 'N'),
(6, 'DEPOSIT', TRUE, '010300100335', '국민은행', 'KB Star 정기예금', '실명의 개인 또는 개인사업자', 1000000, NULL, '- 가입금액 : 1백만원 이상', 'S', 'NONE', 36, '해당무', 2.2, 2.4, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000938', NOW(), 'seokyun', NULL, NULL, 'N'),
(7, 'SAVING', TRUE, '010200100051', '국민은행', 'KB국민프리미엄적금(정액)', '실명의 개인', 10000, 3000000, '1인 1계좌', 'S', 'S', 36, '① 단체가입/나라사랑/쿠폰 우대이율: \n    1년: 연 0.6%p, 2년: 연 0.7%p,\n    3년: 연 0.9%p, 5년: 연 1.0%p \n   (중복적용되지 않음, 계약기간별차등적용)\n② 교차거래 우대이율: 연 0.3%p', 2.8, 4.0, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000428', NOW(), 'seokyun', NULL, NULL, 'N'),
(8, 'SAVING', TRUE, '010200100051', '국민은행', 'KB국민프리미엄적금(정액)', '실명의 개인', 10000, 3000000, '1인 1계좌', 'S', 'S', 24, '① 단체가입/나라사랑/쿠폰 우대이율: \n    1년: 연 0.6%p, 2년: 연 0.7%p,\n    3년: 연 0.9%p, 5년: 연 1.0%p \n   (중복적용되지 않음, 계약기간별차등적용)\n② 교차거래 우대이율: 연 0.3%p', 2.7, 3.7, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000428', NOW(), 'seokyun', NULL, NULL, 'N'),
(9, 'SAVING', TRUE, '010200100051', '국민은행', 'KB국민프리미엄적금(정액)', '실명의 개인', 10000, 3000000, '1인 1계좌', 'S', 'S', 12, '① 단체가입/나라사랑/쿠폰 우대이율: \n    1년: 연 0.6%p, 2년: 연 0.7%p,\n    3년: 연 0.9%p, 5년: 연 1.0%p \n   (중복적용되지 않음, 계약기간별차등적용)\n② 교차거래 우대이율: 연 0.3%p', 2.5, 3.4, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000428', NOW(), 'seokyun', NULL, NULL, 'N'),
(10, 'SAVING', TRUE, '010200100070', '국민은행', 'KB내맘대로적금', '실명의 개인 또는 개인사업자', 10000, 3000000, '인터넷뱅킹/KB스타뱅킹 전용상품', 'S', 'S', 12, '신규 시 다음의 9가지 우대이율 항목 중 6가지를 자유롭게 선택하고, 아래 우대이율 적용조건 충족 시 항목 당 각 연0.1%p의 우대이율 적용\n(최고 연0.6%p)\n - 우대이율 항목 : 급여이체, 카드결제계좌, 자동이체 저축, 아파트관리비 이체, KB스타뱅킹 이체, 장기거래, 첫 거래, 주택청약종합저축, 소중한 날', 2.55, 3.15, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000821', NOW(), 'seokyun', NULL, NULL, 'N'),
(11, 'SAVING', TRUE, '010200100070', '국민은행', 'KB내맘대로적금', '실명의 개인 또는 개인사업자', 10000, 3000000, '인터넷뱅킹/KB스타뱅킹 전용상품', 'S', 'S', 24, '신규 시 다음의 9가지 우대이율 항목 중 6가지를 자유롭게 선택하고, 아래 우대이율 적용조건 충족 시 항목 당 각 연0.1%p의 우대이율 적용\n(최고 연0.6%p)\n - 우대이율 항목 : 급여이체, 카드결제계좌, 자동이체 저축, 아파트관리비 이체, KB스타뱅킹 이체, 장기거래, 첫 거래, 주택청약종합저축, 소중한 날', 2.75, 3.35, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000821', NOW(), 'seokyun', NULL, NULL, 'N'),
(12, 'SAVING', TRUE, '010200100070', '국민은행', 'KB내맘대로적금', '실명의 개인 또는 개인사업자', 10000, 3000000, '인터넷뱅킹/KB스타뱅킹 전용상품', 'S', 'S', 36, '신규 시 다음의 9가지 우대이율 항목 중 6가지를 자유롭게 선택하고, 아래 우대이율 적용조건 충족 시 항목 당 각 연0.1%p의 우대이율 적용\n(최고 연0.6%p)\n - 우대이율 항목 : 급여이체, 카드결제계좌, 자동이체 저축, 아파트관리비 이체, KB스타뱅킹 이체, 장기거래, 첫 거래, 주택청약종합저축, 소중한 날', 2.95, 3.55, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000821', NOW(), 'seokyun', NULL, NULL, 'N'),
(13, 'SAVING', TRUE, '010200100070', '국민은행', 'KB내맘대로적금', '실명의 개인 또는 개인사업자', 10000, 3000000, '인터넷뱅킹/KB스타뱅킹 전용상품', 'S', 'S', 6, '신규 시 다음의 9가지 우대이율 항목 중 6가지를 자유롭게 선택하고, 아래 우대이율 적용조건 충족 시 항목 당 각 연0.1%p의 우대이율 적용\n(최고 연0.6%p)\n - 우대이율 항목 : 급여이체, 카드결제계좌, 자동이체 저축, 아파트관리비 이체, KB스타뱅킹 이체, 장기거래, 첫 거래, 주택청약종합저축, 소중한 날', 2.3, 2.9, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000821', NOW(), 'seokyun', NULL, NULL, 'N'),
(14, 'SAVING', TRUE, '010200100084', '국민은행', 'KB맑은하늘적금', '실명의 개인', 10000, 1000000, '공동명의 불가\n(1인 최대 3계좌)', 'S', 'F', 12, '맑은하늘을 위한 미션별 제공조건을 달성하는 경우 각 미션별 우대이율 제공\n - 1년제 최고 연 0.8%p, 2년제 최고 연 0.9%p, 3년제 최고 연 1.0%p\n① 종이통장 줄이기 미션: 연 0.1%p\n② 종이서식 줄이기 미션: 연 0.2%p\n③ 대중교통 미션: 1년제 연 0.4%p, 2년제 연 0.5%p, 3년제 연 0.6%p\n④ 퀴즈미션: 연 0.1%p', 2.45, 3.25, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000942', NOW(), 'seokyun', NULL, NULL, 'N'),
(15, 'SAVING', TRUE, '010200100084', '국민은행', 'KB맑은하늘적금', '실명의 개인', 10000, 1000000, '공동명의 불가\n(1인 최대 3계좌)', 'S', 'F', 24, '맑은하늘을 위한 미션별 제공조건을 달성하는 경우 각 미션별 우대이율 제공\n - 1년제 최고 연 0.8%p, 2년제 최고 연 0.9%p, 3년제 최고 연 1.0%p\n① 종이통장 줄이기 미션: 연 0.1%p\n② 종이서식 줄이기 미션: 연 0.2%p\n③ 대중교통 미션: 1년제 연 0.4%p, 2년제 연 0.5%p, 3년제 연 0.6%p\n④ 퀴즈미션: 연 0.1%p', 2.55, 3.45, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000942', NOW(), 'seokyun', NULL, NULL, 'N'),
(16, 'SAVING', TRUE, '010200100084', '국민은행', 'KB맑은하늘적금', '실명의 개인', 10000, 1000000, '공동명의 불가\n(1인 최대 3계좌)', 'S', 'F', 36, '맑은하늘을 위한 미션별 제공조건을 달성하는 경우 각 미션별 우대이율 제공\n - 1년제 최고 연 0.8%p, 2년제 최고 연 0.9%p, 3년제 최고 연 1.0%p\n① 종이통장 줄이기 미션: 연 0.1%p\n② 종이서식 줄이기 미션: 연 0.2%p\n③ 대중교통 미션: 1년제 연 0.4%p, 2년제 연 0.5%p, 3년제 연 0.6%p\n④ 퀴즈미션: 연 0.1%p', 2.85, 3.85, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000942', NOW(), 'seokyun', NULL, NULL, 'N'),
(17, 'SAVING', TRUE, '010200100104', '국민은행', 'KB 특★한 적금', '실명의 개인', 1000, 300000, '개인사업자, 임의단체 및\n공동명의 가입 불가\n(1인 최대 3계좌)', 'S', 'F', 1, '항목별 적용 조건 충족시, 최고 연 4.0%p\n① 목표달성 축하 우대이율: 최고 연 1.0%p\n    50만원 이하: 연 0.5%p, 50만원 초과: 연 1.0%p \n② 별 모으기 우대이율 : 최고 연 1.0%p\n    10개: 연 0.5%p, 20개: 연 1.0%p\n③ 함께해요 우대이율: 최고 연 2.0%p', 2.0, 6.0, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01001566', NOW(), 'seokyun', NULL, NULL, 'N'),
(18, 'SAVING', TRUE, '010200100104', '국민은행', 'KB 특★한 적금', '실명의 개인', 1000, 300000, '개인사업자, 임의단체 및\n공동명의 가입 불가\n(1인 최대 3계좌)', 'S', 'F', 3, '항목별 적용 조건 충족시, 최고 연 4.0%p\n① 목표달성 축하 우대이율: 최고 연 1.0%p\n    50만원 이하: 연 0.5%p, 50만원 초과: 연 1.0%p \n② 별 모으기 우대이율 : 최고 연 1.0%p\n    10개: 연 0.5%p, 20개: 연 1.0%p\n③ 함께해요 우대이율: 최고 연 2.0%p', 2.0, 6.0, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01001566', NOW(), 'seokyun', NULL, NULL, 'N'),
(19, 'SAVING', TRUE, '010200100104', '국민은행', 'KB 특★한 적금', '실명의 개인', 1000, 300000, '개인사업자, 임의단체 및\n공동명의 가입 불가\n(1인 최대 3계좌)', 'S', 'F', 6, '항목별 적용 조건 충족시, 최고 연 4.0%p\n① 목표달성 축하 우대이율: 최고 연 1.0%p\n    50만원 이하: 연 0.5%p, 50만원 초과: 연 1.0%p \n② 별 모으기 우대이율 : 최고 연 1.0%p\n    10개: 연 0.5%p, 20개: 연 1.0%p\n③ 함께해요 우대이율: 최고 연 2.0%p', 2.0, 6.0, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01001566', NOW(), 'seokyun', NULL, NULL, 'N'),
(20, 'SAVING', TRUE, 'TRV-01', '국민은행', 'KB두근두근여행적금', '실명의 개인', 50000, 1000000, '노랑풍선 제휴 상품\n① 1회차 납입 시: 패키지여행 1만원 할인쿠폰 (20만원 이상 결제)\n② 4회차 납입 시: 할인 쿠폰팩 4종\n   - 패키지여행 4% 할인\n   - 호텔 1만원 (15만원 이상)\n   - 항공 5천원 (20만원 이상, 해외 전용)\n   - 액티비티 5천원 (10만원 이상)\n※ 쿠폰 등록기간: 제공일로부터 1개월, 사용기간 최대 2년', 'S', 'S', 6, '① 여행친구 우대이율: 최고 연 0.6%p\n    - 인증번호 제공: 연 0.6%p\n    - 인증번호 입력: 연 0.4%p\n② 오픈뱅킹 우대이율: 연 0.3%p\n③ 자동이체저축 우대이율: 연 0.1%p', 2.65, 3.65, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01001495', NOW(), 'jotaeseok', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [석윤] 공통/ 군적금 상품 만기·중도해지이율(military_saving_product)
--  테이블: military_saving_product
--  출처(KB): KB장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 제2026-3282호, 2026.07.21 현재 세전) — 전부 확인, 스크린샷 기반 기존 데이터와 100% 일치
--  출처(IBK): IBK장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 제2025-8992호, 2026.1.5 현재 세전) — 기본이자율/우대이자율/중도해지이자율(가계우대정기적금 준용) 전부 확인
--  출처(신한): 신한 장병내일준비적금 상품설명서(PDF, 준법감시인 사전심사필 제2026-13556-1호, 2026.07.24 현재 세전) — 계약기간 4구간(1~6/6~12/12~15/15~24개월), 1~6개월 구간은 우대이율 미적용
--    ※ gov_match_rate는 PDF에 "3:1 매칭지원금"이라고만 표기(표 없음) — 병역법 시행령상 국가 공통 정책으로 보고 KB/IBK와 동일하게 100 적용, 확인 필요
--  출처(하나): 하나 장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 제2026-설명서-027호, 2026.03.03 현재 세전) — 중도해지금리가 1~6개월 구간은 고정값(0.10/0.15/0.20%), 6개월 이상만 산식 적용
--  출처(Sh수협): Sh장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 상제 2026-1227호, 2026.05.04 기준 세전) — 우대금리 없음(기본금리=최종금리), 3~6개월 구간 산식의 분자가 "경과월수" 대신 고정값 "3"인 특이사항은 무시하고 다른 은행과 동일하게 경과월수로 처리
--  출처(NH농협): NH장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 2026-1166호, 2026.1.6 작성기준 세전) — 계약기간 5구간(1~3/3~6/6~12/12~15/15~24개월), 6개월 미만 구간은 우대이율 미적용
--  출처(우리): 우리은행 장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 2025-10385호, '25.11.25 세금납부 전) — 계약기간 5구간, 3개월 미만 구간은 우대이율 상한이 0.4%p로 낮음, 중도해지 배율 50/70/80/90%(60% 구간 없음), 최저이율 0.15%. 산식이 "보유일수/계약일수"(일 단위)라 하나은행과 동일하게 기존 코드의 월 단위 근사 적용됨
--  출처(iM뱅크): iM장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 25-2785호, '25.11.25 현재 세금납부전) — IBK와 동일하게 경과비율(RATIO) 기준, 배율만 곱함(경과월수/계약월수 추가 없음). 최저이율(floor_rate) 명시 없어 NULL로 처리
--  출처(부산): 부산은행 장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 2025-2544호, 2025.11.25 현재 세전) — 우대이율 0.10%p(주택청약 단일조건) 전 구간 동일적용. 중도해지는 4구간뿐(1개월미만 별도 고정구간 없음), 산식이 "경과일수/계약일수"(일 단위, 하나·우리와 동일 근사), 최저이율 0.01%(타행보다 낮음)
--  출처(광주=KJB): 광주은행 홈페이지 상품안내 페이지(웹, 공시유효기간 2026.01.01~2026.12.31) — 만기이율 5구간은 사용자 확인으로 보완(웹페이지엔 15~24개월 구간만 명시됨). 우대이율 0.5%p는 계약기간 6개월 이상만 적용. 중도해지는 1개월미만 고정 0.1% + 이후 경과비율(RATIO) 구간 혼합, floor_rate 명시 없어 NULL
--  출처(제주): 제주은행 장병내일준비적금 상품설명서(PDF, 준법감시인심의필 2025-01-571호, 2025.11.25 현재 세전) — 우대이율 자체가 없음(기본금리=최종금리). 중도해지 6구간(KB와 같은 개수지만 배율 20/30/60/70/80%로 다름, 경과월수/계약월수 MONTH 방식), 최저금리 0.1%
--  출처(전북=JB): JB장병내일준비적금 상품설명서(PDF, 2021.12.27 심의, 구버전) + 사용자 제공 갱신 표(1~6개월 구간 3% 포함) — 원문서가 2021년 버전이라 가입금액(20만원/40만원)·최소계약기간(6개월)·매칭비율(33%고정)·예금보호(5천만원) 등 정책연동 값은 타행과 동일한 최신 공통기준(1000~30만원/24개월/매칭100%)으로 정규화함. 우대이율 없음(기본=최종). 중도해지는 iM/광주와 동일 패턴(1개월미만 고정 0.1%+RATIO 10/20/40/60/80%, floor 없음)
--  출처(경남=BNK): 경남은행 장병내일준비적금 상품설명서(PDF, 심의번호 2025-A-1054, 2025.11.25 기준 세전) — 우대이율 0.10%p(주택청약, 전 구간 동일적용, 최종이율 최저3.10~최고5.10% 검증됨). 중도해지는 부산은행과 동일 패턴(경과일수/계약일수, 4구간, 최저이율 0.01%)이나 배율만 다름(50/70/80/90%)
--  출처(우체국): 우체국 장병내일준비적금 상품설명서(PDF, 심의번호 2026-101, 2026.6.19 현재 세전) — 우대이율 최고 6.0%p(기초생활수급자 3.3%p 포함, 6개월 미만은 5.4%p)로 타행보다 훨씬 큼. 중도해지 6구간(경과월수/계약월수 MONTH, 배율 50/60/70/80/90%), 최저금리가 구간별로 다름(0.15/0.15/0.20/0.20/0.25%). 최저가입금액 제한없음(우리은행과 동일)
-- --------------------------------------------------------------------
INSERT INTO `military_saving_product`
(`military_saving_id`, `bank_code`, `product_name`, `min_limit`, `max_limit`, `max_join_month`, `gov_match_rate`, `rate_type`, `value_unit`, `min_value`, `max_value`, `basic_rate`, `max_rate`, `rate_ratio`, `floor_rate`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 12, 4.0, 9.5, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(2, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.5, 10.0, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(3, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.0, 10.5, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(4, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 1, NULL, NULL, NULL, 0.1, NOW(), 'seokyun', NULL, NULL, 'N'),
(5, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 1, 3, NULL, NULL, 50, 0.1, NOW(), 'seokyun', NULL, NULL, 'N'),
(6, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 3, 6, NULL, NULL, 50, 0.1, NOW(), 'seokyun', NULL, NULL, 'N'),
(7, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 8, NULL, NULL, 60, 0.2, NOW(), 'seokyun', NULL, NULL, 'N'),
(8, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 8, 10, NULL, NULL, 70, 0.2, NOW(), 'seokyun', NULL, NULL, 'N'),
(9, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 10, 11, NULL, NULL, 80, 0.2, NOW(), 'seokyun', NULL, NULL, 'N'),
(10, '004', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 90, 0.2, NOW(), 'seokyun', NULL, NULL, 'N'),
(11, '003', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', NULL, 10, NULL, NULL, 5, 0.1, NOW(), 'seokyun', NULL, NULL, 'N'),
(12, '003', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 10, 20, NULL, NULL, 10, 0.1, NOW(), 'seokyun', NULL, NULL, 'N'),
(13, '003', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 20, 40, NULL, NULL, 20, 0.1, NOW(), 'seokyun', NULL, NULL, 'N'),
(14, '003', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 40, 60, NULL, NULL, 40, 0.1, NOW(), 'seokyun', NULL, NULL, 'N'),
(15, '003', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 60, 80, NULL, NULL, 60, 0.1, NOW(), 'seokyun', NULL, NULL, 'N'),
(16, '003', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 80, NULL, NULL, NULL, 80, 0.1, NOW(), 'seokyun', NULL, NULL, 'N'),
(17, '003', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 12, 4.0, 9.2, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(18, '003', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.5, 9.7, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(19, '003', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.0, 10.2, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(20, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 6, 3.50, 3.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(21, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 4.00, 9.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(22, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.50, 10.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(23, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 10.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(24, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 1, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(25, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 1, 3, NULL, NULL, 20, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(26, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 3, 6, NULL, NULL, 30, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(27, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 9, NULL, NULL, 70, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(28, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 9, 11, NULL, NULL, 80, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(29, '088', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 90, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(30, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 12, 3.50, 8.70, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(31, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.60, 9.80, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(32, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 10.20, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(33, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 1, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(34, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 1, 3, NULL, NULL, NULL, 0.15, NOW(), 'seokyun', NULL, NULL, 'N'),
(35, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 3, 6, NULL, NULL, NULL, 0.20, NOW(), 'seokyun', NULL, NULL, 'N'),
(36, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 9, NULL, NULL, 60, 0.20, NOW(), 'seokyun', NULL, NULL, 'N'),
(37, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 9, 11, NULL, NULL, 70, 0.20, NOW(), 'seokyun', NULL, NULL, 'N'),
(38, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 90, 0.20, NOW(), 'seokyun', NULL, NULL, 'N'),
(39, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 6, 3.50, 3.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(40, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 4.00, 4.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(41, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.50, 4.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(42, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 5.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(43, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 1, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(44, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 1, 3, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(45, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 3, 6, NULL, NULL, 50, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(46, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 10, NULL, NULL, 60, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(47, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 10, 11, NULL, NULL, 80, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(48, '007', '장병내일준비적금', 1, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 90, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(49, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 3, 2.70, 2.70, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(50, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 3, 6, 2.80, 2.80, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(51, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 3.00, 6.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(52, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.50, 7.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(53, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 8.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(54, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 3, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(55, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 3, 6, NULL, NULL, 50, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(56, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 9, NULL, NULL, 60, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(57, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 9, 11, NULL, NULL, 70, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(58, '011', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 80, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(59, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 3, 3.70, 4.10, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(60, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 3, 6, 3.70, 4.70, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(61, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 4.00, 5.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(62, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.30, 5.30, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(63, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 6.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(64, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 3, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(65, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 3, 6, NULL, NULL, 50, 0.15, NOW(), 'seokyun', NULL, NULL, 'N'),
(66, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 9, NULL, NULL, 70, 0.15, NOW(), 'seokyun', NULL, NULL, 'N'),
(67, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 9, 11, NULL, NULL, 80, 0.15, NOW(), 'seokyun', NULL, NULL, 'N'),
(68, '020', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 90, 0.15, NOW(), 'seokyun', NULL, NULL, 'N'),
(69, '031', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 6, 3.00, 3.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(70, '031', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 4.00, 4.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(71, '031', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.50, 5.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(72, '031', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 5.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(73, '031', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', NULL, 20, NULL, NULL, 10, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(74, '031', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 20, 40, NULL, NULL, 20, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(75, '031', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 40, 60, NULL, NULL, 40, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(76, '031', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 60, 80, NULL, NULL, 60, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(77, '031', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 80, NULL, NULL, NULL, 80, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(78, '032', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 6, 3.00, 3.10, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(79, '032', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 3.50, 3.60, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(80, '032', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.00, 4.10, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(81, '032', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 5.10, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(82, '032', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 6, NULL, NULL, 40, 0.01, NOW(), 'seokyun', NULL, NULL, 'N'),
(83, '032', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 9, NULL, NULL, 70, 0.01, NOW(), 'seokyun', NULL, NULL, 'N'),
(84, '032', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 9, 11, NULL, NULL, 80, 0.01, NOW(), 'seokyun', NULL, NULL, 'N'),
(85, '032', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 90, 0.01, NOW(), 'seokyun', NULL, NULL, 'N'),
(86, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 3, 2.90, 2.90, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(87, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 3, 6, 3.20, 3.20, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(88, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 3.70, 4.20, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(89, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.20, 4.70, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(90, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 5.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(91, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 1, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(92, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', NULL, 20, NULL, NULL, 10, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(93, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 20, 40, NULL, NULL, 20, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(94, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 40, 60, NULL, NULL, 40, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(95, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 60, 80, NULL, NULL, 60, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(96, '034', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 80, NULL, NULL, NULL, 80, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(97, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 6, 3.50, 3.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(98, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 4.00, 4.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(99, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.50, 4.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(100, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 5.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(101, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 1, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(102, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 1, 3, NULL, NULL, 20, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(103, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 3, 6, NULL, NULL, 30, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(104, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 9, NULL, NULL, 60, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(105, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 9, 11, NULL, NULL, 70, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(106, '035', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 80, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(107, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 6, 3.00, 3.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(108, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 3.60, 3.60, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(109, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.10, 4.10, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(110, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 5.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(111, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 1, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(112, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', NULL, 20, NULL, NULL, 10, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(113, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 20, 40, NULL, NULL, 20, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(114, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 40, 60, NULL, NULL, 40, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(115, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 60, 80, NULL, NULL, 60, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(116, '037', '장병내일준비적금', 1000, 300000, 24, 100.00, 'WITHDRAWAL', 'RATIO', 80, NULL, NULL, NULL, 80, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(117, '039', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 6, 3.00, 3.10, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(118, '039', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 3.70, 3.80, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(119, '039', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.50, 4.60, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(120, '039', '장병내일준비적금', 10000, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 5.10, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(121, '039', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 6, NULL, NULL, 50, 0.01, NOW(), 'seokyun', NULL, NULL, 'N'),
(122, '039', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 9, NULL, NULL, 70, 0.01, NOW(), 'seokyun', NULL, NULL, 'N'),
(123, '039', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 9, 11, NULL, NULL, 80, 0.01, NOW(), 'seokyun', NULL, NULL, 'N'),
(124, '039', '장병내일준비적금', 10000, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 90, 0.01, NOW(), 'seokyun', NULL, NULL, 'N'),
(125, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 1, 6, 3.00, 8.40, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(126, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 6, 12, 4.00, 10.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(127, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 12, 15, 4.50, 10.50, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(128, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'MATURITY', 'MONTH', 15, 24, 5.00, 11.00, NULL, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(129, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', NULL, 1, NULL, NULL, NULL, 0.10, NOW(), 'seokyun', NULL, NULL, 'N'),
(130, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 1, 3, NULL, NULL, 50, 0.15, NOW(), 'seokyun', NULL, NULL, 'N'),
(131, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 3, 6, NULL, NULL, 60, 0.15, NOW(), 'seokyun', NULL, NULL, 'N'),
(132, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 6, 9, NULL, NULL, 70, 0.20, NOW(), 'seokyun', NULL, NULL, 'N'),
(133, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 9, 12, NULL, NULL, 80, 0.20, NOW(), 'seokyun', NULL, NULL, 'N'),
(134, '071', '장병내일준비적금', NULL, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 12, NULL, NULL, NULL, 90, 0.25, NOW(), 'seokyun', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [석윤] 시뮬레이터/ 정책상품(policy_product)
--  테이블: policy_product
-- --------------------------------------------------------------------
INSERT INTO `policy_product` (`policy_id`, `policy_name`, `policy_status`, `benefits`, `join_member`,
  `min_limit`, `max_limit`, `save_trm_note`, `policy_link`, `has_calculator`, `calc_period_months`, `min_rate`, `max_rate`, `normal_match_rate`, `prefer_match_rate`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, '청년미래적금', TRUE, '1. 금리 연 5~8%\n2. 세제혜택: 만기유지시 이자소득 비과세\n3. 정부기여금 매칭비율(일반 6%, 우대 12%)', '1. 나이 요건: 만 19세 이상 34세 이하인 사람 (군 복무 기간은 최대 6년까지 나이에서 빼고 계산 가능).\n2. 개인 소득: 직전 년도 총급여 7,500만 원 이하 (종합소득 6,300만 원 이하) 또는 소상공인 연 매출 3억 원 이하.\n3. 가구 소득: 등본상 가구원 합산 소득이 기준 중위소득 200% 이하\n4. 금융 과세: 최근 3년 중 한 번이라도 금융소득종합과세 대상자(연 이자·배당 2,000만 원 초과)가 아니었을 것.',
1000, 500000, '36개월', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=Y&prcode=DP01001656', TRUE, 36, 5.00, 8.00, 6.00, 12.00, NOW(), 'seokyun', NULL, NULL, 'N'),
(2, '청년주택드림 청약통장', TRUE, '1. 금리 연 3.1~4.5%\n2. 세제혜택: 이자소득 비과세 및 연말정산소득공제', '1. 나이 요건: 만 19세 이상 34세 이하인 사람 (군 복무 기간은 최대 6년까지 나이에서 빼고 계산 가능).\n2. 주택요건: 가입일 기준 본인 명의의 주택을 소유하지 않은 자\n3. 개인소득: 직전년도 총급여 5,000만원 이하 또는 현역병 등 군 복무(전역)자',
20000, 1000000, '별도의 만기 없음', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000935',  FALSE,  NULL,  3.10,  4.50,  NULL,  NULL,  NOW(),  'seokyun',  NULL,  NULL, 'N');
