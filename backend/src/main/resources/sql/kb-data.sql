SET FOREIGN_KEY_CHECKS = 0;

-- ######################################################################
--  Ⅰ. 공통 마스터 · 코드
-- ######################################################################

-- --------------------------------------------------------------------
--  [석윤] 공통/ 군종정보(military_types)
--  테이블: military_types
-- --------------------------------------------------------------------
INSERT INTO `military_types` (`type_id`, `type_name`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, '육군', NOW(), 'seokyun', NULL, NULL, 'N'),
(2, '해군', NOW(), 'seokyun', NULL, NULL, 'N'),
(3, '공군', NOW(), 'seokyun', NULL, NULL, 'N'),
(4, '해병대', NOW(), 'seokyun', NULL, NULL, 'N'),
(5, '공익', NOW(), 'seokyun', NULL, NULL, 'N'),
(6, '기타', NOW(), 'seokyun', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [석윤] 공통/ 계급(military_rank)
--  테이블: military_rank
-- --------------------------------------------------------------------
INSERT INTO `military_rank` (`rank_id`, `rank_name`, `rank_salary`, `image_url`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, '이병', 750000, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(2, '일병', 900000, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(3, '상병', 1200000, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(4, '병장', 1500000, NULL, NOW(), 'seokyun', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [석윤] 공통/ 은행카테고리 (bank_category)
--  테이블: bank_category
-- --------------------------------------------------------------------
INSERT INTO `bank_category` (`bank_code`, `bank_name`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES 
('039', '경남은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('034', '광주은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('032', '부산은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('045', '새마을금고', NOW(), 'seokyun', NULL, NULL, 'N'),
('064', '산림조합', NOW(), 'seokyun', NULL, NULL, 'N'),
('088', '신한은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('048', '신협', NOW(), 'seokyun', NULL, NULL, 'N'),
('027', '씨티은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('020', '우리은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('071', '우체국예금보험', NOW(), 'seokyun', NULL, NULL, 'N'),
('050', '저축은행중앙회', NOW(), 'seokyun', NULL, NULL, 'N'),
('037', '전북은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('035', '제주은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('090', '카카오뱅크', NOW(), 'seokyun', NULL, NULL, 'N'),
('089', '케이뱅크', NOW(), 'seokyun', NULL, NULL, 'N'),
('092', '토스뱅크', NOW(), 'seokyun', NULL, NULL, 'N'),
('081', '하나은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('054', '홍콩상하이은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('003', 'IBK기업은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('004', 'KB국민은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('031', 'iM뱅크(대구)', NOW(), 'seokyun', NULL, NULL, 'N'),
('002', '한국산업은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('011', 'NH농협은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('023', 'SC제일은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('007', 'Sh수협은행', NOW(), 'seokyun', NULL, NULL, 'N'),
('030', '수협중앙회', NOW(), 'seokyun', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [석윤] 공통/ 로드맵카테고리(roadmap_category)
--  테이블: roadmap_category
-- --------------------------------------------------------------------
INSERT INTO `roadmap_category` (`category_id`, `category_name`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, '여행', NOW(), 'seokyun', NULL, NULL, 'N'),
(2, '진로', NOW(), 'seokyun', NULL, NULL, 'N'),
(3, '자동차', NOW(), 'seokyun', NULL, NULL, 'N'),
(4, '자취', NOW(), 'seokyun', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [태석] 공통 / 뱃지(badge)
--  테이블: badge
-- --------------------------------------------------------------------
INSERT INTO badge (badge_id, badge_name, image_url, created_date, created_nm, del_yn)

VALUES
	-- 랭킹 
	(1, '상위 1% 뱃지',   '/images/badge/rank_1.png',   NOW(), 'jotaeseok', 'N'),
	(2, '상위 5% 뱃지',   '/images/badge/rank_5.png',   NOW(), 'jotaeseok', 'N'),
	(3, '상위 10% 뱃지',  '/images/badge/rank_10.png',  NOW(), 'jotaeseok', 'N'),
	(4, '상위 30% 뱃지',  '/images/badge/rank_30.png',  NOW(), 'jotaeseok', 'N'),
	-- 업적
	(5, '진행률 50% 뱃지',  '/images/badge/prog_50.png',  NOW(), 'jotaeseok', 'N'),
	(6, '진행률 75% 뱃지',  '/images/badge/prog_75.png',  NOW(), 'jotaeseok', 'N'),
	(7, '진행률 100% 뱃지', '/images/badge/prog_100.png', NOW(), 'jotaeseok', 'N');


-- --------------------------------------------------------------------
--  [수연] 자취/법정동코드
--  테이블: region_code
-- --------------------------------------------------------------------
INSERT INTO region_code (region_code, sido_name, sigungu_name, umd_name, sigungu_code, is_abolished, created_date, created_nm, del_yn) VALUES
('1168010100', '서울특별시', '강남구',   '역삼동',  '11680', 'N', NOW(), 'suyeon', 'N'),
('1168010300', '서울특별시', '강남구',   '개포동',  '11680', 'N', NOW(), 'suyeon', 'N'),
('1171010100', '서울특별시', '송파구',   '잠실동',  '11710', 'N', NOW(), 'suyeon', 'N'),
('1147010100', '서울특별시', '양천구',   '신정동',  '11470', 'N', NOW(), 'suyeon', 'N'),
('2620010100', '부산광역시', '부산진구', '부전동',  '26200', 'N', NOW(), 'suyeon', 'N'),
('2644010300', '부산광역시', '해운대구', '중동',    '26440', 'N', NOW(), 'suyeon', 'N'),
('2647010100', '부산광역시', '사하구',   '괴정동',  '26470', 'N', NOW(), 'suyeon', 'N'),
('4113510500', '경기도',     '성남시 분당구', '정자동', '41135', 'N', NOW(), 'suyeon', 'N');


-- --------------------------------------------------------------------
--  [지원] 진로/ 직무직렬학과코드(job_code)
--  테이블: job_code
-- --------------------------------------------------------------------
-- 취업(J01) 8개
INSERT INTO job_code (job_code_id, goal_type, code_name, info_url, created_date, created_nm, del_yn) VALUES
(1, 'J01', '백엔드 개발자', NULL, NOW(), 'jiwon', 'N'),
(2, 'J01', '데이터 분석가', NULL, NOW(), 'jiwon', 'N'),
(3, 'J01', '경영지원(총무/인사)', NULL, NOW(), 'jiwon', 'N'),
(4, 'J01', '회계/재무', NULL, NOW(), 'jiwon', 'N'),
(5, 'J01', '마케터', NULL, NOW(), 'jiwon', 'N'),
(6, 'J01', '영업/세일즈', NULL, NOW(), 'jiwon', 'N'),
(7, 'J01', '그래픽 디자이너', NULL, NOW(), 'jiwon', 'N'),
(8, 'J01', '생산관리', NULL, NOW(), 'jiwon', 'N');

-- 공무원(J02) 6개
INSERT INTO job_code (job_code_id, goal_type, code_name, info_url, created_date, created_nm, del_yn) VALUES
(9, 'J02', '일반행정직', NULL, NOW(), 'jiwon', 'N'),
(10, 'J02', '세무직', NULL, NOW(), 'jiwon', 'N'),
(11, 'J02', '전산직', NULL, NOW(), 'jiwon', 'N'),
(12, 'J02', '사회복지직', NULL, NOW(), 'jiwon', 'N'),
(13, 'J02', '경찰공무원(순경)', NULL, NOW(), 'jiwon', 'N'),
(14, 'J02', '교정직', NULL, NOW(), 'jiwon', 'N');

-- 편입(J03) 6개
INSERT INTO job_code (job_code_id, goal_type, code_name, info_url, created_date, created_nm, del_yn) VALUES
(15, 'J03', '경영학과', NULL, NOW(), 'jiwon', 'N'),
(16, 'J03', '컴퓨터공학과', NULL, NOW(), 'jiwon', 'N'),
(17, 'J03', '전자전기공학과', NULL, NOW(), 'jiwon', 'N'),
(18, 'J03', '경제학과', NULL, NOW(), 'jiwon', 'N'),
(19, 'J03', '행정학과', NULL, NOW(), 'jiwon', 'N'),
(20, 'J03', '심리학과', NULL, NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
--  [호빈] 약관
--  테이블: terms
-- --------------------------------------------------------------------
INSERT into terms (name, required, content, version, created_date, created_nm, modified_date, modified_nm, del_yn) values
('서비스 이용약관', true, '(약관 본문 - 추후 법무 검토 후 최종화)', '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
('개인정보 수집·이용 동의', true, '수집항목: 이메일, 이름, 전화번호, 군종, 부대정보, 입대일, 전역예정일 / 수집목적: 맞춤형 전역 로드맵 및 정책상품 매칭 서비스 제공', '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
('만 14세 이상 확인', true, '만 14세 이상만 가입 가능합니다.', '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
('마케팅 정보 수신 동의', false, '정책상품 알림, 이벤트 등 마케팅 정보를 수신합니다.', '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
('제3자 정보제공 동의', false, 'KB 계열사 상품 연계 추천을 위해 정보를 제공합니다.', '1.0', NOW(), 'hobin', NULL, NULL, 'N');


-- ######################################################################
--  Ⅱ. 상품 · 기준 데이터
-- ######################################################################

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
(20, 'SAVING', TRUE, '미확인', '국민은행', 'KB두근두근여행적금', '실명의 개인', 50000, 1000000, '노랑풍선 제휴 상품\n① 1회차 납입 시: 패키지여행 1만원 할인쿠폰 (20만원 이상 결제)\n② 4회차 납입 시: 할인 쿠폰팩 4종\n   - 패키지여행 4% 할인\n   - 호텔 1만원 (15만원 이상)\n   - 항공 5천원 (20만원 이상, 해외 전용)\n   - 액티비티 5천원 (10만원 이상)\n※ 쿠폰 등록기간: 제공일로부터 1개월, 사용기간 최대 2년', 'S', 'S', 6, '① 여행친구 우대이율: 최고 연 0.6%p\n    - 인증번호 제공: 연 0.6%p\n    - 인증번호 입력: 연 0.4%p\n② 오픈뱅킹 우대이율: 연 0.3%p\n③ 자동이체저축 우대이율: 연 0.1%p', 2.65, 3.65, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01001495', NOW(), 'jotaeseok', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [석윤] 시뮬레이터/ 정책상품(policy_product)
--  테이블: policy_product
-- --------------------------------------------------------------------
INSERT INTO `policy_product` (`policy_id`, `policy_name`, `policy_status`, `benefits`, `join_member`, 
  `min_limit`, `max_limit`, `save_trm_note`, `policy_link`, `has_calculator`, `calc_period_months`, `max_rate`, `normal_match_rate`, `prefer_match_rate`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES 
(1, '청년미래적금', TRUE, '1. 금리 연 5~8%\n2. 세제혜택: 만기유지시 이자소득 비과세\n3. 정부기여금 매칭비율(일반 6%, 우대 12%)', '1. 나이 요건: 만 19세 이상 34세 이하인 사람 (군 복무 기간은 최대 6년까지 나이에서 빼고 계산 가능).\n2. 개인 소득: 직전 년도 총급여 7,500만 원 이하 (종합소득 6,300만 원 이하) 또는 소상공인 연 매출 3억 원 이하.\n3. 가구 소득: 등본상 가구원 합산 소득이 기준 중위소득 200% 이하\n4. 금융 과세: 최근 3년 중 한 번이라도 금융소득종합과세 대상자(연 이자·배당 2,000만 원 초과)가 아니었을 것.', 
1000, 500000, '36개월', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=Y&prcode=DP01001656', TRUE, 36, 8.00, 6.00, 12.00, NOW(), 'seokyun', NULL, NULL, 'N'),
(2, '청년주택드림 청약통장', TRUE, '1. 금리 연 3.1~4.5%\n2. 세제혜택: 이자소득 비과세 및 연말정산소득공제', '1. 나이 요건: 만 19세 이상 34세 이하인 사람 (군 복무 기간은 최대 6년까지 나이에서 빼고 계산 가능).\n2. 주택요건: 가입일 기준 본인 명의의 주택을 소유하지 않은 자\n3. 개인소득: 직전년도 총급여 5,000만원 이하 또는 현역병 등 군 복무(전역)자', 
20000, 1000000, '별도의 만기 없음', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000935',  FALSE,  NULL,  4.50,  NULL,  NULL,  NOW(),  'seokyun',  NULL,  NULL, 'N');


-- --------------------------------------------------------------------
--  [태석] 여행 / 도시별물가(city_cost)
--  테이블: city_cost
-- --------------------------------------------------------------------
INSERT INTO city_cost
    (city_cost_id, country, city, saving_cost, common_cost, premium_cost,
     created_date, created_nm, del_yn)
VALUES
	(1, '일본', '오사카',     49086,  78322, 117886, NOW(), 'jotaeseok', 'N'),
	(2, '일본', '도쿄',       56315,  99263, 155052, NOW(), 'jotaeseok', 'N'),
	(3, '일본', '교토',       60230,  94817, 146154, NOW(), 'jotaeseok', 'N'),
	(4, '일본', '나고야',     58856,  85885, 125304, NOW(), 'jotaeseok', 'N'),
	(5, '일본', '아이치',     53375,  82609, 125941, NOW(), 'jotaeseok', 'N'),
	(6, '일본', '아키타',     45313,  67603, 103001, NOW(), 'jotaeseok', 'N'),
	(7, '일본', '아마가사키', 51131,  84741, 130649, NOW(), 'jotaeseok', 'N'),
    (8, '일본', '아오모리',   53348,  85850, 130649, NOW(), 'jotaeseok', 'N'),
    (9, '일본', '아쓰기',     63738,  84544, 118428, NOW(), 'jotaeseok', 'N'),
    (10, '일본', '벳푸',       59185,  77773, 106458, NOW(), 'jotaeseok', 'N'),
    (11, '일본', '지바',       43742,  76124, 124108, NOW(), 'jotaeseok', 'N'),
    (12, '일본', '에비나',     50896,  80724, 124777, NOW(), 'jotaeseok', 'N'),
    (13, '일본', '후쿠이',     49743,  76133, 112851, NOW(), 'jotaeseok', 'N'),
    (14, '일본', '후쿠오카',   56448,  90685, 134101, NOW(), 'jotaeseok', 'N'),
    (15, '일본', '후쿠시마',   60069, 102682, 151841, NOW(), 'jotaeseok', 'N'),
    (16, '일본', '후쿠야마',   50032,  84405, 137364, NOW(), 'jotaeseok', 'N'),
    (17, '일본', '후나바시',   47455,  65729,  97952, NOW(), 'jotaeseok', 'N'),
    (18, '일본', '기후',       50877,  79516, 118484, NOW(), 'jotaeseok', 'N'),
    (19, '일본', '하치오지',   42583,  92698, 158875, NOW(), 'jotaeseok', 'N'),
    (20, '일본', '하코다테',   64535, 126888, 193112, NOW(), 'jotaeseok', 'N'),
    (21, '베트남', '하노이',     21611, 39577,  59629, NOW(), 'jotaeseok', 'N'),
    (22, '베트남', '호치민',     21198, 39559,  60086, NOW(), 'jotaeseok', 'N'),
    (23, '베트남', '다낭',       22673, 40552,  60950, NOW(), 'jotaeseok', 'N'),
    (24, '베트남', '달랏',       22613, 36107,  52419, NOW(), 'jotaeseok', 'N'),
    (25, '베트남', '하롱베이',   28927, 71028, 112932, NOW(), 'jotaeseok', 'N'),
    (26, '베트남', '깜라인',     22939, 38502,  56552, NOW(), 'jotaeseok', 'N'),
    (27, '베트남', '박장',       20226, 32850,  48021, NOW(), 'jotaeseok', 'N'),
    (28, '베트남', '박닌',       14258, 32082,  52567, NOW(), 'jotaeseok', 'N'),
    (29, '베트남', '비엔호아',   20294, 28966,  40086, NOW(), 'jotaeseok', 'N'),
    (30, '베트남', '빈즈엉',     18058, 34509,  53055, NOW(), 'jotaeseok', 'N'),
    (31, '베트남', '부온마투옷', 22939, 38502,  56552, NOW(), 'jotaeseok', 'N'),
    (32, '베트남', '박깐',       15940, 34034,  54449, NOW(), 'jotaeseok', 'N'),
    (33, '베트남', '까마우',     20259, 37652,  57281, NOW(), 'jotaeseok', 'N'),
    (34, '베트남', '껌파',       20827, 38965,  59186, NOW(), 'jotaeseok', 'N'),
    (35, '베트남', '껀터',       15877, 33840,  53328, NOW(), 'jotaeseok', 'N'),
    (36, '베트남', '디엔비엔푸', 20260, 37653,  57282, NOW(), 'jotaeseok', 'N'),
    (37, '베트남', '동허이',     17987, 36232,  58155, NOW(), 'jotaeseok', 'N'),
    (38, '베트남', '동쏘아이',   17086, 25673,  36496, NOW(), 'jotaeseok', 'N'),
    (39, '베트남', '하동',       20923, 39112,  59384, NOW(), 'jotaeseok', 'N'),
    (40, '대한민국', '부산',     54499,  84499, 124499, NOW(), 'jotaeseok', 'N'),
    (41, '대한민국', '서울',   60367, 105792, 153492, NOW(), 'jotaeseok', 'N'),
    (42, '대한민국', '대구',   57227,  79974, 104371, NOW(), 'jotaeseok', 'N'),
    (43, '대한민국', '대전',   49476,  80026, 113126, NOW(), 'jotaeseok', 'N'),
    (44, '대한민국', '강릉',   43103,  62349,  84012, NOW(), 'jotaeseok', 'N'),
    (45, '대한민국', '춘천',   46144,  71005,  98032, NOW(), 'jotaeseok', 'N'),
    (46, '대한민국', '거제',   52098,  83198, 118798, NOW(), 'jotaeseok', 'N'),
    (47, '대한민국', '안동',   48928,  74336, 101560, NOW(), 'jotaeseok', 'N'),
    (48, '대한민국', '보령',   60067, 105592, 153392, NOW(), 'jotaeseok', 'N'),
    (49, '대한민국', '창원',   49782,  68382,  89982, NOW(), 'jotaeseok', 'N'),
    (50, '대한민국', '천안',   45300,  60050,  78800, NOW(), 'jotaeseok', 'N'),
    (51, '대한민국', '청주',   51748,  83598, 117048, NOW(), 'jotaeseok', 'N'),
    (52, '대한민국', '충주',   54198,  87248, 123098, NOW(), 'jotaeseok', 'N'),
    (53, '대한민국', '안산',   52596,  77536, 105092, NOW(), 'jotaeseok', 'N'),
    (54, '대한민국', '안양',   48269,  72694, 100095, NOW(), 'jotaeseok', 'N'),
    (55, '대한민국', '부천',   60067, 105592, 153392, NOW(), 'jotaeseok', 'N'),
    (56, '대한민국', '김천',   48595,  74090, 101402, NOW(), 'jotaeseok', 'N');


-- --------------------------------------------------------------------
--  [태석] 여행 / 여행패키지상품(travel_package)
--  테이블: travel_package
-- --------------------------------------------------------------------
INSERT INTO travel_package
    (package_id, goods_code, country, region_name, name, image_url, description,
     min_price, departure_period, crawled_at, is_active, detail_url,
     created_date, created_nm, del_yn)
VALUES
	(1, 'AVP4484', '베트남', '다낭',
 '다낭/호이안 5일',
 'https://dimgcdn.ybtour.co.kr/TN/79/798308110f991d072ac7a24eea0e6da9.tn.410x280.jpg',
 '#5성 #NO옵션NO팁 #호이안야간투어 #마사지2회 #럭셔리크루즈',
 569900, '2026.07.22~2027.06.30', NOW(), TRUE,
 'https://prdt.ybtour.co.kr/product/detailPackage?menu=PKG&goodsCd=AVP4484',
 NOW(), 'jotaeseok', 'N'),
 
	(2, 'AVP4474', '베트남', '다낭',
 '부산출발 다낭/호이안 5/6일',
 'https://dimgcdn.ybtour.co.kr/TN/79/798308110f991d072ac7a24eea0e6da9.tn.410x280.jpg',
 '#노팁 #노옵션 #씨클로 #과일바구니증정 #5성급호텔',
 599000, '2026.07.22~2027.03.31', NOW(), TRUE,
 'https://prdt.ybtour.co.kr/product/detailPackage?menu=PKG&goodsCd=AVP4474',
 NOW(), 'jotaeseok', 'N'),
 
	(3, 'AVP1202', '베트남', '하노이',
 '하노이/하롱베이/옌뜨 4/5/6일',
 'https://dimgcdn.ybtour.co.kr/TN/77/771299e08968c64ad3627cdda4760b68.tn.410x280.jpg',
 '#노옵션#크루즈디너뷔페#씨푸드+비경스피보트#1일1커피',
 549000, '2026.08.16~2026.09.24', NOW(), TRUE,
 'https://prdt.ybtour.co.kr/product/detailPackage?menu=PKG&goodsCd=AVP1202',
 NOW(), 'jotaeseok', 'N'),
 
	(4, 'JOP1190', '일본', '오사카',
 '[USJ부터자유일정까지]오사카4일',
 'https://dimgcdn.ybtour.co.kr/TN/7b/7b0fa486c79aa70155626ab4a8d9f3eb.tn.410x280.jpg',
 '#취향따라골라가는/오사카,교토,고베,아라시야마',
 599000, '2026.07.26~2027.03.31', NOW(), TRUE,
 'https://prdt.ybtour.co.kr/product/detailPackage?menu=PKG&goodsCd=JOP1190',
 NOW(), 'jotaeseok', 'N');


-- --------------------------------------------------------------------
--  [태석] 여행 / 여행보험(travel_insurance)
--  테이블: travel_insurance
-- --------------------------------------------------------------------
INSERT INTO travel_insurance (
    insurance_id, title, insurance_inf, insurance_period, insurance_url,
    created_date, created_nm, del_yn
) VALUES (
		1,
    'KB 해외여행보험',
    '해외여행 중 상해·질병 의료비, 휴대품 파손·도난, 배상책임, 항공기 지연, 여권 재발급, 구조·송환비 등 보장. 기후질환 진단비 및 지수형 항공기 지연 특약 포함',
    90,
    'https://direct.kbinsure.co.kr/home/#/GL/OT/GN_CM0101M/otrav_step?pid=6110972&code=5524',
    NOW(),
    'jotaeseok',
    'N'
);


-- --------------------------------------------------------------------
--  [수연] 자취/월세 실거래 매물
--  테이블: rent_listing
-- --------------------------------------------------------------------
INSERT INTO rent_listing (listing_id, estate_type, sigungu_code, region_code, umd_name, jibun, building_name, built_year, floor, area_sqm, deposit, monthly_rent, deal_date, latitude, longitude, base_date, created_date, created_nm, del_yn) VALUES
(1, 'OFFICETEL', '26200', '2620010100', '부전동', '168-1',  '부전현대',   2018, 5,  23.14, 5000000,  450000, '2026-05-12', 35.15784, 129.05903, '2026-07-15', NOW(), 'BATCH', 'N'),
(2, 'OFFICETEL', '26200', '2620010100', '부전동', '505-3',  '서면SK뷰',   2020, 8,  28.50, 10000000, 550000, '2026-05-20', 35.15691, 129.05812, '2026-07-15', NOW(), 'BATCH', 'N'),
(3, 'VILLA',     '26200', '2620010100', '부전동', '210-15', '한양빌라',   2015, 3,  33.20, 3000000,  380000, '2026-06-01', 35.15522, 129.06012, '2026-07-15', NOW(), 'BATCH', 'N'),
(4, 'OFFICETEL', '11680', '1168010100', '역삼동', '736-40', '역삼래미안', 2019, 12, 25.72, 20000000, 900000, '2026-05-25', 37.49952, 127.03737, '2026-07-15', NOW(), 'BATCH', 'N'),
(5, 'OFFICETEL', '11680', '1168010100', '역삼동', '825-4',  '역삼푸르지오', 2021, 15, 33.05, 30000000, 1200000, '2026-06-05', 37.50108, 127.03621, '2026-07-15', NOW(), 'BATCH', 'N'),
(6, 'VILLA',     '11680', '1168010100', '역삼동', '619-2',  '역삼그린빌', 2012, 4,  39.60, 10000000, 750000, '2026-05-30', 37.50274, 127.03495, '2026-07-15', NOW(), 'BATCH', 'N'),
(7, 'OFFICETEL', '11710', '1171010100', '잠실동', '40-1',   '잠실리센츠', 2020, 10, 26.80, 15000000, 850000, '2026-06-08', 37.51139, 127.09800, '2026-07-15', NOW(), 'BATCH', 'N'),
(8, 'OFFICETEL', '26440', '2644010300', '중동',   '1394',   '해운대두산위브', 2022, 20, 30.11, 20000000, 950000, '2026-06-12', 35.16294, 129.16745, '2026-07-15', NOW(), 'BATCH', 'N');


-- --------------------------------------------------------------------
--  [수연] 자취/지역관리비통계
--  테이블: region_fee_stat
-- --------------------------------------------------------------------
INSERT INTO region_fee_stat (stat_id, region_code, base_month, mgmt_fee_per_sqm, elec_fee_per_sqm, water_fee_per_sqm, heat_fee_per_sqm, sample_count, base_date, created_date, created_nm, del_yn) VALUES
(1, '2620010100', '2026-06', 1420, 1180, 460, 780, 12, '2026-07-10', NOW(), 'BATCH', 'N'),
(2, '1168010100', '2026-06', 1830, 1350, 520, 890, 25, '2026-07-10', NOW(), 'BATCH', 'N'),
(3, '1171010100', '2026-06', 1710, 1290, 500, 850, 18, '2026-07-10', NOW(), 'BATCH', 'N');


-- --------------------------------------------------------------------
--  [수연] 자취/청년 주거 대출 상품
--  테이블: housing_loan
-- --------------------------------------------------------------------
INSERT INTO housing_loan (loan_id, name, product_type, loan_type, provider, rate_summary, loan_limit, min_age, max_age, veteran_benefit, join_condition, detail, external_url, is_kb, base_date, created_date, created_nm, del_yn) VALUES
(1, '청년전용 버팀목 전월세대출', 'POLICY', 'DEPOSIT', 'KB국민은행', '연 1.5% ~ 2.7%', 200000000, 19, 34, '병역 이행 시 최대 만 39세까지 연장', '연소득 5천만원 이하·무주택 세대주', '주택도시기금 재원 청년 전세자금 대출', 'https://nhuf.molit.go.kr/', TRUE, '2026-07-10', NOW(), 'ADMIN', 'N'),
(2, '청년전용 보증부월세대출',   'POLICY', 'MONTHLY', 'KB국민은행', '보증금 연 1.3% / 월세 연 0%', 45000000, 19, 34, '병역 이행 시 최대 만 39세까지 연장', '연소득 5천만원 이하', '주택도시기금 청년 월세 대출', 'https://nhuf.molit.go.kr/', TRUE, '2026-07-10', NOW(), 'ADMIN', 'N'),
(3, 'KB 청년 맞춤형 전세자금대출', 'BANK', 'DEPOSIT', 'KB국민은행', '연 3.8% ~ 5.5%', 200000000, 19, 34, '군필자 만 39세까지 연장', '재직 3개월 이상 또는 사업자 6개월 이상', 'KB국민은행 자체 상품', 'https://obank.kbstar.com/', TRUE, '2026-07-10', NOW(), 'ADMIN', 'N'),
(4, '중소기업취업청년 전월세보증금대출', 'POLICY', 'DEPOSIT', '5개 시중은행', '연 1.5% 고정', 100000000, 19, 34, '병역 이행 시 최대 만 39세까지 연장', '중소·중견기업 재직 청년 / 연소득 3.5천만원 이하', '주택도시기금 정책 상품', 'https://nhuf.molit.go.kr/', TRUE, '2026-07-10', NOW(), 'ADMIN', 'N');


-- --------------------------------------------------------------------
--  [지원] 진로/ 준비항목추천기준(prep_item_criteria)
--  테이블: prep_item_criteria
-- --------------------------------------------------------------------
-- ===== 2. prep_item_criteria (대표 3개 직무/직렬/학과) =====
INSERT INTO prep_item_criteria (prep_crit_id, goal_type, item_type, job_code_id, item_name, apply_url, amount, amount_source, created_date, created_nm, del_yn) VALUES
-- 백엔드 개발자 (job_code_id=1)
(1, 'J01', 'P01', 1, '정보처리기사 필기', 'https://q-net.or.kr', 19400, 'E01', NOW(), 'jiwon', 'N'),
(2, 'J01', 'P01', 1, '정보처리기사 실기', 'https://q-net.or.kr', 22600, 'E01', NOW(), 'jiwon', 'N'),
(3, 'J01', 'P01', 1, 'SQLD', 'https://www.dataq.or.kr', 50000, 'E02', NOW(), 'jiwon', 'N'),
(4, 'J01', 'P01', 1, '토익', 'https://www.ybmnet.co.kr', 26250, 'E02', NOW(), 'jiwon', 'N'),
(5, 'J01', 'P02', 1, '백엔드 개발 입문 인강', 'https://www.inflearn.com', 218000, 'E02', NOW(), 'jiwon', 'N'),
(6, 'J01', 'P03', 1, '백엔드 개발자 양성 과정', 'https://www.work24.go.kr', 540000, 'E02', NOW(), 'jiwon', 'N'),
-- 일반행정직 (job_code_id=9)
(7, 'J02', 'P01', 9, '한국사능력검정시험', 'https://www.historyexam.go.kr', 22000, 'E02', NOW(), 'jiwon', 'N'),
(8, 'J02', 'P02', 9, '9급 공무원 국어 종합반 인강', 'https://www.pmg2000.co.kr', 300000, 'E02', NOW(), 'jiwon', 'N'),
-- 경영학과 (job_code_id=15)
(9, 'J03', 'P01', 15, '토익', 'https://www.ybmnet.co.kr', 26250, 'E02', NOW(), 'jiwon', 'N'),
(10, 'J03', 'P02', 15, '편입경영학 전공 종합반 인강', 'https://www.pmg2000.co.kr', 350000, 'E02', NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
--  [지원] 진로/정책 및 서비스 기준(service_criteria)
--  테이블: service_criteria
-- --------------------------------------------------------------------
-- ===== 5. service_criteria (정부 정책 + KB 서비스, 카드 제외) =====
INSERT INTO service_criteria (svc_crit_id, goal_type, service_type, service_name, service_desc, use_time, info_url, created_date, created_nm, del_yn) VALUES
(1, 'J01', 'G01', 'KB Pay', '복무 중 이용', 'S01', NULL, NOW(), 'jiwon', 'N'),
(2, 'J01', 'G01', '국민내일배움카드', '전역 후 신청', 'S02', NULL, NOW(), 'jiwon', 'N'),
(3, 'J02', 'G01', '공무원연금 대출제도', '재직 공무원 대상 저리 생활안정자금 대출', 'U02', NULL, NOW(), 'jiwon', 'N'),
(4, 'J02', 'G02', 'KB Pay', '복무 중에도 간편결제로 이용 가능', 'U01', NULL, NOW(), 'jiwon', 'N'),
(5, 'J03', 'G01', '국가장학금(편입생 대상)', '소득분위별 등록금 지원', 'U03', NULL, NOW(), 'jiwon', 'N'),
(6, 'J03', 'G02', 'KB Pay', '복무 중에도 간편결제로 이용 가능', 'U01', NULL, NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
--  [지원] 공통/KB카드상품
--  테이블: card_product
-- --------------------------------------------------------------------
INSERT INTO card_product (card_id, category, card_name, card_type, card_qual, card_desc, card_url, created_date, created_nm, del_yn) VALUES
(1, 1, '트래블러스 체크카드', '체크카드', NULL, '해외가맹점 이용수수료 면제, 해외 ATM 인출 수수료 면제', 'https://m.kbcard.com/CRD/DVIEW/MCAMCXHIACRC0002?mainCC=b&allianceCode=09562', NOW(), 'jotaeseok', 'N'),
(2, 1, '노리2 체크카드', '체크카드', '전월실적 20만원', '해외 이용금액 2% 환급할인, 더라운지 공항 라운지 연 1회 무료 이용', 'https://card.kbcard.com/CRD/DVIEW/HCAMCXPRICAC0076?mainCC=a&cooperationcode=07972', NOW(), 'jotaeseok', 'N'),
(3, 1, '가온글로벌카드', '신용카드', '전월실적 20만원, 연회비 2만원', '해외 전 가맹점 최대 3% 포인트 적립, 면세점 및 항공/여행 업종 5% 적립', 'https://card.kbcard.com/CRD/DVIEW/HCAMCXPRICAC0076?mainCC=a&cooperationcode=09167', NOW(), 'jotaeseok', 'N'),
(4, 1, 'WE:SH Travel 카드', '신용카드', '전월실적 30만원, 연회비 25,000원', '해외 이용 수수료 면제, 해외 이용금액 10% 할인, 더라운지 공항 라운지 연 2회 무료', 'https://card.kbcard.com/CRD/DVIEW/HCAMCXPRICAC0076?mainCC=a&cooperationcode=09561', NOW(), 'jotaeseok', 'N'),
(5, 1, 'NeeD global 카드', '신용카드', NULL, '해외 가맹점 3.5% 청구할인(한도 없음), 국내 가맹점 0.5% 청구할인', 'https://card.kbcard.com/CRD/DVIEW/HCAMCXPRICAC0076?mainCC=a&cooperationcode=09137', NOW(), 'jotaeseok', 'N'),
(6, 2, '히어로즈체크카드', '체크카드', '전역 후 신청', '외국어학원/서점, 어학시험(TOEIC/JPT) 10% 할인 / 교통, 구독서비스 20% 할인 / 이동통신, 손해보험, 숙박 5% 할인 (연회비 없음)', 'https://card.kbcard.com/CRD/DVIEW/HCAMCXPRICAC0076?mainCC=a&cooperationcode=01934', NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
--  [호빈] 취득세율 기준
--  테이블: car_tax
-- --------------------------------------------------------------------
INSERT into car_tax (car_type_code, acquisition_tax_rate, bond_exempt_engine_cc, region, created_date, created_nm, modified_date, modified_nm, del_yn) values
(1, 4.00, 1600, '전국', NOW(), 'hobin', NULL, NULL, 'N'),
(2, 7.00, 1600, '전국', NOW(), 'hobin', NULL, NULL, 'N'),
(3, 7.00, 1600, '전국', NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [호빈] 자동차세 연납 할인
--  테이블: car_tax_prepay
-- --------------------------------------------------------------------
INSERT into car_tax_prepay (year, prepay_month, discount_rate, created_date, created_nm, modified_date, modified_nm, del_yn) values
(2026, '1월', 4.60, NOW(), 'hobin', NULL, NULL, 'N'),
(2026, '3월', 3.45, NOW(), 'hobin', NULL, NULL, 'N'),
(2026, '6월', 2.30, NOW(), 'hobin', NULL, NULL, 'N'),
(2026, '9월', 1.15, NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [호빈] 전기차 보조금
--  테이블: car_ev
-- --------------------------------------------------------------------
INSERT into car_ev (region, national_subsidy, local_subsidy, base_year, created_date, created_nm, modified_date, modified_nm, del_yn) values
('서울특별시', 580, 150, 2026, NOW(), 'hobin', NULL, NULL, 'N'),
('경기도', 580, 200, 2026, NOW(), 'hobin', NULL, NULL, 'N'),
('제주특별자치도', 580, 650, 2026, NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [호빈] 자동차 보험
--  테이블: car_insurance
-- --------------------------------------------------------------------
INSERT into car_insurance (car_type_code, experience_bracket, estimated_premium_min, estimated_premium_max, created_date, created_nm, modified_date, modified_nm, del_yn) values
(1, '3년미만', 45, 55, NOW(), 'hobin', NULL, NULL, 'N'),
(1, '3~5년', 40, 50, NOW(), 'hobin', NULL, NULL, 'N'),
(1, '5년이상', 35, 45, NOW(), 'hobin', NULL, NULL, 'N'),
(2, '3년미만', 58, 72, NOW(), 'hobin', NULL, NULL, 'N'),
(2, '3~5년', 52, 65, NOW(), 'hobin', NULL, NULL, 'N'),
(2, '5년이상', 48, 58, NOW(), 'hobin', NULL, NULL, 'N'),
(3, '3년미만', 68, 85, NOW(), 'hobin', NULL, NULL, 'N'),
(3, '3~5년', 62, 78, NOW(), 'hobin', NULL, NULL, 'N'),
(3, '5년이상', 58, 70, NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [수연] 후회소비/가맹점 카테고리 매핑
--  테이블: merchant_category
-- --------------------------------------------------------------------
INSERT INTO merchant_category (mapping_id, keyword, category, priority, created_date, created_nm, del_yn) VALUES
(1,  '국군복지단',  'PX',          100, NOW(), 'suyeon', 'N'),
(2,  'PX',          'PX',          85,  NOW(), 'suyeon', 'N'),
(3,  '배달의민족',  'DELIVERY',    100, NOW(), 'suyeon', 'N'),
(4,  '쿠팡이츠',    'DELIVERY',    100, NOW(), 'suyeon', 'N'),
(5,  '요기요',      'DELIVERY',    100, NOW(), 'suyeon', 'N'),
(6,  'GS25',        'CONVENIENCE', 90,  NOW(), 'suyeon', 'N'),
(7,  'CU',          'CONVENIENCE', 90,  NOW(), 'suyeon', 'N'),
(8,  '세븐일레븐',  'CONVENIENCE', 90,  NOW(), 'suyeon', 'N'),
(9,  '넥슨',        'GAME',        95,  NOW(), 'suyeon', 'N'),
(10, 'APPLE',       'GAME',        60,  NOW(), 'suyeon', 'N'),
(11, 'KTX',         'VACATION',    85,  NOW(), 'suyeon', 'N'),
(12, '스타벅스',    'ETC',         50,  NOW(), 'suyeon', 'N');



-- --------------------------------------------------------------------
--  [호빈] 차량 종류
--  테이블: car_type
-- --------------------------------------------------------------------
insert into car_type (code, name, created_date, created_nm, modified_date, modified_nm, del_yn) values
(1, '경차', NOW(), 'hobin', NULL, NULL, 'N'),
(2, '준중형', NOW(), 'hobin', NULL, NULL, 'N'),
(3, 'SUV', NOW(), 'hobin', NULL, NULL, 'N');



-- --------------------------------------------------------------------
--  [호빈] 차량 모델
--  테이블: car_model
-- --------------------------------------------------------------------
insert into car_model (model_id, manufacturer, model_name, car_type_code, fuel_type, base_price, created_date, created_nm, modified_date, modified_nm, del_yn) values
(1, '기아', '모닝', 1, '가솔린', 1325, NOW(), 'hobin', NULL, NULL, 'N'),
(2, '기아', '레이', 1, '가솔린', 1400, NOW(), 'hobin', NULL, NULL, 'N'),
(3, '현대', '캐스퍼', 1, '가솔린', 1460, NOW(), 'hobin', NULL, NULL, 'N'),
(4, '현대', '아반떼', 2, '가솔린', 1964, NOW(), 'hobin', NULL, NULL, 'N'),
(5, '기아', '셀토스', 3, '가솔린', 2477, NOW(), 'hobin', NULL, NULL, 'N'),
(6, '기아', '셀토스', 3, '하이브리드', 2898, NOW(), 'hobin', NULL, NULL, 'N');



-- ######################################################################
--  Ⅲ. 회원
-- ######################################################################

-- --------------------------------------------------------------------
--  [호빈] 회원 정보
--  테이블: user
-- --------------------------------------------------------------------
INSERT INTO `user` (`id`, `user_id`, `password`, `name`, `phone`, `type_id`, `rank_id`, `unit_name`, `unit_code`, `enlist_date`, `discharge_date`, `login_provider`, `status`, `withdrawn_at`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 'hobin@kbthink.com', '$2a$10$testHashedPassword01', '김호빈', '010-1234-5678', 1, 3, '수도방위사령부 제1경비단', 'A01-102', '2026-01-05', '2027-07-04', 'local', 'ACTIVE', NULL, NOW(), 'hobin', NULL, NULL, 'N'),
(2, 'sukyun@kbthink.com', '$2a$10$testHashedPassword02', '김호빈', '010-2345-6789', 1, 4, '제5보병사단', 'A01-205', '2025-11-10', '2027-05-09', 'local', 'ACTIVE', NULL, NOW(), 'hobin', NULL, NULL, 'N'),
(3, 'test.marine@kbthink.com', '$2a$10$testHashedPassword03', '테스트해병', '010-3456-7890', 4, 2, NULL, NULL, '2026-02-01', '2027-07-31', 'local', 'ACTIVE', NULL, NOW(), 'hobin', NULL, NULL, 'N');


-- ######################################################################
--  Ⅳ. 회원 종속 (1차)
-- ######################################################################

-- --------------------------------------------------------------------
--  [석윤] 대시보드/ 군적금계좌(saving_account)
--  테이블: saving_account
-- --------------------------------------------------------------------
INSERT INTO `saving_account` 
(`account_id`, `user_id`, `bank_code`, `monthly_save`, `monthly_count`, `curr_amount`, `account_status`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) 
VALUES
-- 회원 1 (ACTIVE): 총 55만원 (30 + 25) / 5개월 납입 중 (26년 1월 입대, 26년 3월 개설 => 납입가능 회차는 17회차)
(1, 1, '004', 300000, 5, 1500000, 'ACTIVE', '2026-03-9 10:00:00', 'seokyun', NULL, NULL, 'N'),
(2, 1, '003', 250000, 5, 1250000, 'ACTIVE', '2026-03-9 10:05:00', 'seokyun', NULL, NULL, 'N'),

-- 회원 2 (ACTIVE): 계좌 3은 일정하게 납입, 계좌 4는 중간에 증액 (25년 11월 입대, 25년 11월 개설 => 납입가능 회차 18회차)
(3, 2, '004', 300000, 8, 2400000, 'ACTIVE', '2025-11-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(4, 2, '088', 250000, 8, 950000, 'ACTIVE', '2025-11-15 14:35:00', 'seokyun', NULL, NULL, 'N'),

-- 회원 3 (TERMINATED): 총 45만원 (30 + 15) / 3개월 후 중도 해지 (26년 2월 입대, 26년 2월 개설 => 납입가능 회차 18회차, 하지만 4월분 납입후 해지)
(5, 3, '081', 300000, 2, 900000, 'TERMINATED', '2026-02-10 09:00:00', 'seokyun', NULL, NULL, 'Y'),
(6, 3, '004', 150000, 2, 450000, 'TERMINATED', '2026-02-10 09:05:00', 'seokyun', NULL, NULL, 'Y');


-- --------------------------------------------------------------------
--  [석윤] 대시보드/ 휴가(vacation)
--  테이블: vacation
-- --------------------------------------------------------------------
INSERT INTO `vacation` 
(`vacation_id`, `user_id`, `vacation_cate`, `vacation_name`, `vacation_get`, `vacation_day`, `vacation_state`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) 
VALUES
(1, 1, 'REGULAR', '정기휴가', '2026-03-15', 24, FALSE, NOW(), 'seokyun', NULL, NULL, 'N'),
(2, 1, 'CONSOLATION', '신병위로휴가', '2025-06-01', 3, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),
(3, 1, 'REWARD', '특급전사 포상휴가', '2025-06-20', 4, FALSE, NOW(), 'seokyun', NULL, NULL, 'N'),

-- 회원 2(육군)의 정기휴가(연가) 처리 시나리오
(4, 2, 'REGULAR', '정기휴가 (총 부여일수)', '2025-01-01', 24, FALSE, NOW(), 'seokyun', NULL, NULL, 'N'),     -- 총 부여 휴가
(5, 2, 'REGULAR', '1차 정기휴가 (사용 완료)', '2025-05-01', 4, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),     -- 4일 사용
(6, 2, 'REGULAR', '2차 정기휴가 (사용 완료)', '2025-07-01', 5, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),     -- 5일 사용
(7, 2, 'PETITION', '자격증시험', '2023-08-01', 2, TRUE, NOW(), 'seokyun', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [태석] 공통 / 유저뱃지(user_badge)
--  테이블: user_badge
-- --------------------------------------------------------------------
INSERT INTO user_badge (id, user_id, badge_id, created_date, created_nm, del_yn)

VALUES
	(1, 1, 4, NOW(), 'jotaeseok', 'N'),
	(2, 1, 5, NOW(), 'jotaeseok', 'N'),
	(3, 2, 1, NOW(), 'jotaeseok', 'N'),
	(4, 2, 7, NOW(), 'jotaeseok', 'N'),
	(5, 3, 3, NOW(), 'jotaeseok', 'N'),
	(6, 3, 6, NOW(), 'jotaeseok', 'N');


-- --------------------------------------------------------------------
--  [태석] 여행 / 여행목표(travel_goal)
--  테이블: travel_goal
-- --------------------------------------------------------------------
INSERT INTO travel_goal (goal_id, user_id, title, departure, destination, is_domestic, style, start_date, end_date, total_budget, places, benefits, package_id, created_date, created_nm, del_yn)

VALUES
-- 회원1 : 저예산 케이스
(1, 1, '다낭 전역여행', '인천', '다낭', FALSE, 'saving',
 '2026-11-02', '2026-11-05', 800000,
 '[
	 {"type":"tour","name":"마블마운틴","info":"동굴과 사원이 있는 다채로운 산","image":"https://serpapi.com/searches/6a5e618ebd1731af1e19eb49/images/GvHh68c8DPUuCxuCmLUqrqhEVKB0PniwjOoesTS9NHI.jpeg"},
   {"type":"tour","name":"팝럼사 (法林寺)","info":"1934년에 지어진 2층 불교 사원","image":"https://serpapi.com/searches/6a5e618ebd1731af1e19eb49/images/KIIJPeq5zLUg5AxWiV9g_yITnny5yEPZfUdosanpD2g.jpeg"},
   {"type":"food","name":"NGON DA NANG - VIETNAMESE CUISINE RESTAURANT","info":"매장 내 식사·테이크아웃","image":"https://serpapi.com/searches/6a5e618e66bc789017fe9cab/images/AquqTDV9riK8l0A34ktzJSoiVo0tlR1a1GPquUNhk-8.jpeg"}
  ]',
 '[
   {"type":"saving","productId":20,"name":"KB두근두근여행적금"},
   {"type":"card","productId":1,"name":"트래블러스 체크카드"}
  ]',
	1, NOW(), 'jotaeseok', 'N'),
 
-- 회원2 : 예산 여유 케이스
(2, 2, '오사카 졸업여행', '인천', '오사카', FALSE, 'common',
 '2026-09-20', '2026-09-23', 2000000,
  '[
   {"type":"tour","name":"도톤보리","info":"음식점 및 극장으로 유명한 지역","image":"https://serpapi.com/searches/6a5e679adbb9ff379459dd5a/images/Ghc_3-LmSqc_k812psTKMmZ7MaEk99okFzyh4KQOho4.jpeg"},
   {"type":"food","name":"OSAKAVILLAGE","info":"오사카 여행에서 꼭 한 번 이상을 들려야 할 필수 맛집이 아닐까 합니다!","image":"https://serpapi.com/searches/6a5e679a5b92f8db433c9794/images/v9J1uOJY3xSdqiyRH9E0lD9D8hC8-QEvPNv8iOZCLK8.jpeg"}
  ]',
 '[
   {"type":"insurance","productId":1,"name":"KB 해외여행보험"},
   {"type":"card","productId":4,"name":"WE:SH Travel 카드"}
  ]',
 4, NOW(), 'jotaeseok', 'N'),
 
-- 회원3 : 국내 여행
(3, 3, '부산 여행', '서울', '부산', TRUE, 'premium',
 '2027-01-10', '2027-01-12', 1500000,
 '[
	 {"type":"tour","name":"감천문화마을","info":"이곳은 원래 달동네였으나 2009년부터 관광지 개발을 했습니다.","image":"https://serpapi.com/searches/6a5e672571a41dfc92d32afe/images/a0iTqz3Qmsf2emXFXkb6f1Ht7Db-jXjOtcZ8g1bvoaQ.jpeg"}
	]',
 '[
   {"type":"card","productId":2,"name":"노리2 체크카드"}
  ]',
 NULL, NOW(), 'jotaeseok', 'N');


-- --------------------------------------------------------------------
--  [에스더] 챗봇
--  테이블: chat_session, chat_message, chat_feedback
-- --------------------------------------------------------------------
INSERT INTO `chat_session`
  (`session_id`, `user_id`, `title`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
  (1, 1, '청년미래적금 문의', NOW(), 'esther', NOW(), 'esther', 'N'),
  (2, 1, '목돈 활용 상담', NOW(), 'esther', NULL, NULL, 'N');
  
INSERT INTO `chat_message`
  (`message_id`, `session_id`, `role`, `content`, `source`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
  (1, 1, 'bot', '고객님, 안녕하세요! 어떤 내용이 궁금하세요?', NULL, NOW(), 'esther', NULL, NULL, 'N'),
  (2, 1, 'user', '청년미래적금', NULL, NOW(), 'esther', NULL, NULL, 'N'),
  (3, 1, 'bot', '청년미래적금은 만 19~34세 청년의 중장기 자산형성을 지원하는, 정부기여금(6~12%)과 비과세 혜택이 있는 자유적립식 상품이에요.', 'KB국민은행 상품안내 · 청년미래적금 (2026년 5월 기준)', NOW(), 'esther', NULL, NULL, 'N'),
  (4, 1, 'user', '정부기여금은 얼마나 받을 수 있어요?', NULL, NOW(), 'esther', NULL, NULL, 'N'),
  (5, 1, 'bot', '소득 수준에 따라 납입액의 6~12%를 정부기여금으로 추가 지원받을 수 있어요.', 'KB국민은행 상품안내 · 청년미래적금 (2026년 5월 기준)', NOW(), 'esther', NULL, NULL, 'N'),
  (6, 2, 'bot', '고객님, 안녕하세요! 어떤 내용이 궁금하세요?', NULL, NOW(), 'esther', NULL, NULL, 'N'),
  
  (7, 2, 'user', '목돈 어떻게 쓸지 상담받기', NULL, NOW(), 'esther', NULL, NULL, 'N'),
  (8, 2, 'bot', '몇 가지만 여쭤볼게요. 목표 기간이 어떻게 되세요?', NULL, NOW(), 'esther', NULL, NULL, 'N');
  
INSERT INTO `chat_feedback`
  (`feedback_id`, `session_id`, `message_id`, `feedback`, `reason`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
  (1, 1, NULL, 'like', NULL, NOW(), 'esther', NULL, NULL, 'N'),
  (2, 2, NULL, 'dislike', '원하는 답변이 아니었어요', NOW(), 'esther', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [수연] 자취/로드맵 추천 (2명)
--  테이블: rent_goal, rent_goal_region, rent_recommend, loan_recommend
-- --------------------------------------------------------------------
--  월세 목표
INSERT INTO rent_goal (goal_id, user_id, title, trade_type, estate_type, max_deposit, max_monthly, room_count, expected_fee, residence_term, current_asset, target_date, status, created_date, created_nm, del_yn) VALUES
(1, 1, '전역 후 부산 자취',   'MONTHLY', 'OFFICETEL', 10000000, 600000, 'ONE', 100000, 'Y1', 20150000, '2026-11-01', 'CONFIRMED', NOW(), 'suyeon', 'N'),
(2, 2, '전역 후 서울 자취',   'MONTHLY', 'OFFICETEL', 20000000, 900000, 'ONE', 100000, 'Y1', 18800000, '2027-01-01', 'DRAFT',     NOW(), 'suyeon', 'N');

-- 목표별 희망 지역
INSERT INTO rent_goal_region (region_id, goal_id, region_code, created_date, created_nm, del_yn) VALUES
(1, 1, '2620010100', NOW(), 'suyeon', 'N'),
(2, 1, '2644010300', NOW(), 'suyeon', 'N'),
(3, 1, '2647010100', NOW(), 'suyeon', 'N'),
(4, 2, '1168010100', NOW(), 'suyeon', 'N'),
(5, 2, '1171010100', NOW(), 'suyeon', 'N');

-- 매물 추천 이력 (회원 A만 CONFIRMED이므로 스냅샷 있음)
INSERT INTO rent_recommend (rec_id, goal_id, listing_id, snapshot, rank_order, created_date, created_nm, del_yn) VALUES
(1, 1, 1, '{"building_name":"부전현대","umd_name":"부전동","estate_type":"OFFICETEL","deposit":5000000,"monthly_rent":450000,"expected_fee":100000,"area_sqm":23.14,"monthly_total":550000,"total_required":11600000,"residence_months":12}', 1, NOW(), 'suyeon', 'N'),
(2, 1, 3, '{"building_name":"한양빌라","umd_name":"부전동","estate_type":"VILLA","deposit":3000000,"monthly_rent":380000,"expected_fee":80000,"area_sqm":33.20,"monthly_total":460000,"total_required":8520000,"residence_months":12}', 2, NOW(), 'suyeon', 'N'),
(3, 1, 8, '{"building_name":"해운대두산위브","umd_name":"중동","estate_type":"OFFICETEL","deposit":20000000,"monthly_rent":950000,"expected_fee":150000,"area_sqm":30.11,"monthly_total":1100000,"total_required":33200000,"residence_months":12,"over_budget":true}', 3, NOW(), 'suyeon', 'N');

-- 금융상품 추천 이력
INSERT INTO loan_recommend (rec_id, goal_id, loan_id, snapshot, rank_order, created_date, created_nm, del_yn) VALUES
(1, 1, 1, '{"name":"청년전용 버팀목 전월세대출","product_type":"POLICY","loan_type":"DEPOSIT","provider":"KB국민은행","rate_summary":"연 1.5% ~ 2.7%","is_kb":true,"veteran_eligible":true}', 1, NOW(), 'suyeon', 'N'),
(2, 1, 3, '{"name":"KB 청년 맞춤형 전세자금대출","product_type":"BANK","loan_type":"DEPOSIT","provider":"KB국민은행","rate_summary":"연 3.8% ~ 5.5%","is_kb":true}', 2, NOW(), 'suyeon', 'N');


-- --------------------------------------------------------------------
--  [지원] 진로/진로 목표(job_goal)
--  테이블: job_goal
-- --------------------------------------------------------------------
-- ===== job_goal =====
INSERT INTO job_goal (goal_id, user_id, goal_type, job_code_id, expected_date, status, created_date, created_nm, modified_date, modified_nm, del_yn) VALUES
(1, 1, 'J01', 1,  '2027-03', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N'),  -- 백엔드 개발자
(2, 1, 'J01', 2,  '2027-06', 'DRAFT',     NOW(), 'jiwon', NULL, NULL, 'N'),  -- 데이터 분석가 (임시저장 테스트용)
(3, 1, 'J02', 9,  '2027-09', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N'),  -- 일반행정직
(4, 1, 'J03', 15, '2027-03', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N'); -- 경영학과


-- --------------------------------------------------------------------
--  [호빈] 차량 목표
--  테이블: car_goal
-- --------------------------------------------------------------------
INSERT INTO `car_goal` (`user_id`, `budget`, `car_type_code`, `is_new`, `target_date`, `region`, `selected_model_id`, `selected_year`, `status`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 2000, 1, false, '2027-03-01', '서울특별시', 3, 2023, 'confirmed', NOW(), 'hobin', NULL, NULL, 'N'),
(1, 1500, 1, false, '2027-08-01', '서울특별시', NULL, NULL, 'draft', NOW(), 'hobin', NULL, NULL, 'N'),
(2, 2500, 2, true, '2027-06-01', '경기도',  4, NULL, 'confirmed', NOW(), 'hobin', NULL, NULL, 'N'),
(3, 1900, 1, true, '2027-09-01', '제주특별자치도', NULL, NULL, 'draft', NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [호빈] 약관 동의
--  테이블: terms_agreement
-- --------------------------------------------------------------------
INSERT INTO `terms_agreement` (`user_id`, `terms_id`, `agreed`, `agreed_date`, `terms_version`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 1, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(1, 2, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(1, 3, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(1, 4, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(1, 5, false, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(2, 1, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(2, 2, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(2, 3, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(3, 1, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(3, 2, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N'),
(3, 3, true, NOW(), '1.0', NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [수연] 후회소비/오픈뱅킹 연동
--  테이블: openbanking_link
-- --------------------------------------------------------------------
-- ---------------------------------------------------------------------
-- [오픈뱅킹 연동 (회원당 3행: 적금 KB, 적금 신한, 입출금 KB)
-- ※ 적금계좌만 account_id 채움 / 입출금계좌는 NULL (거래내역 수집 대상)
-- ---------------------------------------------------------------------
INSERT INTO openbanking_link (link_id, user_id, access_token, refresh_token, fintech_use_num, bank_code, account_id, account_num_masked, expires_at, created_date, created_nm, del_yn) VALUES
-- 회원 A (user_id=1)
(1, 1, 'test_at_A_kb_saving',    'test_rt_A_kb_saving',    '199167428338341000001', '004', 1,    '004-01-****111', DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), 'suyeon', 'N'),
(2, 1, 'test_at_A_shinhan',      'test_rt_A_shinhan',      '199167428338341000002', '088', 2,    '088-01-****112', DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), 'suyeon', 'N'),
(3, 1, 'test_at_A_kb_checking',  'test_rt_A_kb_checking',  '199167428338341000003', '004', NULL, '004-01-****113', DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), 'suyeon', 'N'),
-- 회원 B (user_id=2)
(4, 2, 'test_at_B_kb_saving',    'test_rt_B_kb_saving',    '199167428338341000004', '004', 3,    '004-02-****221', DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), 'suyeon', 'N'),
(5, 2, 'test_at_B_shinhan',      'test_rt_B_shinhan',      '199167428338341000005', '088', 4,    '004-02-****222', DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), 'suyeon', 'N'),
(6, 2, 'test_at_B_kb_checking',  'test_rt_B_kb_checking',  '199167428338341000006', '004', NULL, '004-02-****223', DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), 'suyeon', 'N'),
-- 회원 C (user_id=3)
(7, 3, 'test_at_B_kb_saving',    'test_rt_B_kb_saving',    '199167428338341000007', '004', 5,    '004-02-****331', DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), 'suyeon', 'N'),
(8, 3, 'test_at_B_shinhan',      'test_rt_B_shinhan',      '199167428338341000008', '088', 6,    '004-02-****332', DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), 'suyeon', 'N'),
(9, 3, 'test_at_B_kb_checking',  'test_rt_B_kb_checking',  '199167428338341000009', '004', NULL, '004-02-****333', DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), 'suyeon', 'N');


-- --------------------------------------------------------------------
--  [수연] 후회소비/지출내역 부분
--  테이블: spending
-- --------------------------------------------------------------------
-- 지출 내역 (입출금계좌에서 수집, 각 회원당 6건씩)
INSERT INTO spending (spending_id, user_id, merchant_name, category, amount, spent_at, created_date, created_nm, del_yn) VALUES
-- 회원 A (배달·게임 위주)
(1, 1, '국군복지단 PX', 'PX',         15200, '2026-07-14 20:15:00', NOW(), 'BATCH', 'N'),
(2, 1, '배달의민족',    'DELIVERY',    23500, '2026-07-14 22:40:00', NOW(), 'BATCH', 'N'),
(3, 1, '넥슨',          'GAME',        30000, '2026-07-15 23:55:00', NOW(), 'BATCH', 'N'),
(4, 1, '쿠팡이츠',      'DELIVERY',    18900, '2026-07-16 21:10:00', NOW(), 'BATCH', 'N'),
(5, 1, 'KTX 서울-부산', 'VACATION',    59800, '2026-07-17 14:20:00', NOW(), 'BATCH', 'N'),
(6, 1, '넥슨',          'GAME',        50000, '2026-07-17 23:30:00', NOW(), 'BATCH', 'N'),
-- 회원 B (편의점·PX 위주)
(7,  2, '국군복지단 PX', 'PX',          8500, '2026-07-14 19:30:00', NOW(), 'BATCH', 'N'),
(8,  2, 'GS25 부대앞점', 'CONVENIENCE',  4300, '2026-07-15 19:20:00', NOW(), 'BATCH', 'N'),
(9,  2, '세븐일레븐',    'CONVENIENCE',  6800, '2026-07-16 20:10:00', NOW(), 'BATCH', 'N'),
(10, 2, 'CU 위병소점',   'CONVENIENCE',  5200, '2026-07-17 08:15:00', NOW(), 'BATCH', 'N'),
(11, 2, '스타벅스',      'ETC',          6500, '2026-07-17 15:45:00', NOW(), 'BATCH', 'N'),
(12, 2, '국군복지단 PX', 'PX',         12500, '2026-07-18 20:45:00', NOW(), 'BATCH', 'N'),
-- 회원 C (편의점·PX 위주) - ID 중복 수정 완료
(13, 3, '국군복지단 PX', 'PX',          8500, '2026-07-14 19:30:00', NOW(), 'BATCH', 'N'),
(14, 3, 'GS25 부대앞점', 'CONVENIENCE',  4300, '2026-07-15 19:20:00', NOW(), 'BATCH', 'N'),
(15, 3, '세븐일레븐',    'CONVENIENCE',  6800, '2026-07-16 20:10:00', NOW(), 'BATCH', 'N'),
(16, 3, 'CU 위병소점',   'CONVENIENCE',  5200, '2026-07-17 08:15:00', NOW(), 'BATCH', 'N'),
(17, 3, '스타벅스',      'ETC',          6500, '2026-07-17 15:45:00', NOW(), 'BATCH', 'N'),
(18, 3, '국군복지단 PX', 'PX',         12500, '2026-07-18 20:45:00', NOW(), 'BATCH', 'N');


-- --------------------------------------------------------------------
--  [수연] 후회소비/월간 분석
--  테이블: spending_review, saving_challenge
-- --------------------------------------------------------------------
-- 태깅 (회원 A는 후회 많음, 회원 B는 만족 많음)
INSERT INTO spending_review (review_id, spending_id, review_type, reviewed_at, created_date, created_nm, del_yn) VALUES
(1, 1, 'SATISFIED', '2026-07-15 21:00:00', NOW(), 'suyeon', 'N'),
(2, 2, 'REGRET',    '2026-07-15 21:01:00', NOW(), 'suyeon', 'N'),
(3, 3, 'REGRET',    '2026-07-16 21:00:00', NOW(), 'suyeon', 'N'),
(4, 4, 'REGRET',    '2026-07-17 21:00:00', NOW(), 'suyeon', 'N'),
(5, 5, 'SATISFIED', '2026-07-18 09:00:00', NOW(), 'suyeon', 'N'),
(6, 6, 'REGRET',    '2026-07-18 21:00:00', NOW(), 'suyeon', 'N'),
(7, 7,  'SATISFIED','2026-07-15 21:00:00', NOW(), 'suyeon', 'N'),
(8, 8,  'SATISFIED','2026-07-16 21:00:00', NOW(), 'suyeon', 'N'),
(9, 9,  'SATISFIED','2026-07-17 21:00:00', NOW(), 'suyeon', 'N'),
(10,10, 'REGRET',   '2026-07-18 09:00:00', NOW(), 'suyeon', 'N');

-- 월간 절감 목표
INSERT INTO saving_challenge (challenge_id, user_id, target_month, base_amount, target_amount, result_amount, is_achieved, created_date, created_nm, del_yn) VALUES
(1, 1, '2026-06', 145000, 116000, 128000, TRUE,  NOW(), 'suyeon', 'N'),
(2, 1, '2026-07', 128000, 102000, NULL,   NULL,  NOW(), 'suyeon', 'N'),
(3, 2, '2026-06',  25000,  20000,  18000, TRUE,  NOW(), 'suyeon', 'N'),
(4, 2, '2026-07',  18000,  14000,  NULL,  NULL,  NOW(), 'suyeon', 'N'),
(5, 3, '2026-06',  25000,  20000,  18000, TRUE,  NOW(), 'suyeon', 'N'),
(6, 3, '2026-07',  18000,  14000,  NULL,  NULL,  NOW(), 'suyeon', 'N');


-- --------------------------------------------------------------------
--  [수연] 후회소비/카카오
--  테이블: kakao_token, notification
-- --------------------------------------------------------------------
-- 카카오 알림 토큰
INSERT INTO kakao_token (token_id, user_id, access_token, refresh_token, expires_at, created_date, created_nm, del_yn) VALUES
(1, 1, 'kakao_at_A_test', 'kakao_rt_A_test', DATE_ADD(NOW(), INTERVAL 60 DAY), NOW(), 'suyeon', 'N'),
(2, 2, 'kakao_at_B_test', 'kakao_rt_B_test', DATE_ADD(NOW(), INTERVAL 60 DAY), NOW(), 'suyeon', 'N'),
(3, 3, 'kakao_at_B_test', 'kakao_rt_B_test', DATE_ADD(NOW(), INTERVAL 60 DAY), NOW(), 'suyeon', 'N');

-- 알림 발송 이력
INSERT INTO notification (noti_id, user_id, notify_type, content, is_read, sent_at, created_date, created_nm, del_yn) VALUES
(1, 1, 'REVIEW',  '[소비 점호] 오늘 3건 확인 부탁드립니다.',                     TRUE,  '2026-07-15 21:00:00', NOW(), 'BATCH', 'N'),
(2, 1, 'PREVENT', '[예방 알림] 심야 게임 결제, 잠시 멈춰볼까요?',                 FALSE, '2026-07-17 23:00:00', NOW(), 'BATCH', 'N'),
(3, 2, 'REVIEW',  '[소비 점호] 오늘 2건 확인 부탁드립니다.',                     TRUE,  '2026-07-15 21:00:00', NOW(), 'BATCH', 'N'),
(4, 2, 'REPORT',  '[월간 리포트] 6월 후회 소비 절감 목표를 달성하셨어요! 🎖️',   TRUE,  '2026-07-01 09:00:00', NOW(), 'BATCH', 'N'),
(5, 3, 'REVIEW',  '[소비 점호] 오늘 2건 확인 부탁드립니다.',                     TRUE,  '2026-07-15 21:00:00', NOW(), 'BATCH', 'N'),
(6, 3, 'REPORT',  '[월간 리포트] 6월 후회 소비 절감 목표를 달성하셨어요! 🎖️',   TRUE,  '2026-07-01 09:00:00', NOW(), 'BATCH', 'N');


-- ######################################################################
--  Ⅴ. 회원 종속 (2차)
-- ######################################################################

-- --------------------------------------------------------------------
--  [석윤] 대시보드/ 군적금납입내역(saving_history)
--  테이블: saving_history
-- --------------------------------------------------------------------
INSERT INTO `saving_history` 
(`history_id`, `account_id`, `pay_round`, `pay_amount`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) 
VALUES

-- 회원 1 (ACTIVE): 총 55만원 (30 + 25) / 5개월 납입 중 (26년 1월 입대, 26년 3월 개설 => 납입가능 회차는 17회차)
-- [회원 1] account_id = 1 (ACTIVE: 5회차 / 30만원 고정)
(1, 1, 1, 300000, '2026-03-10 10:00:00', 'seokyun', NULL, NULL, 'N'),
(2, 1, 2, 300000, '2026-04-10 10:00:00', 'seokyun', NULL, NULL, 'N'),
(3, 1, 3, 300000, '2026-05-10 10:00:00', 'seokyun', NULL, NULL, 'N'),
(4, 1, 4, 300000, '2026-06-10 10:00:00', 'seokyun', NULL, NULL, 'N'),
(5, 1, 5, 300000, '2026-07-10 10:00:00', 'seokyun', NULL, NULL, 'N'),

-- [회원 1] account_id = 2 (ACTIVE: 5회차 / 25만원 고정)
(6, 2, 1, 250000, '2026-03-10 10:05:00', 'seokyun', NULL, NULL, 'N'),
(7, 2, 2, 250000, '2026-04-10 10:05:00', 'seokyun', NULL, NULL, 'N'),
(8, 2, 3, 250000, '2026-05-10 10:05:00', 'seokyun', NULL, NULL, 'N'),
(9, 2, 4, 250000, '2026-06-10 10:05:00', 'seokyun', NULL, NULL, 'N'),
(10, 2, 5, 250000, '2026-07-10 10:05:00', 'seokyun', NULL, NULL, 'N'),

-- 회원 2 (ACTIVE): 계좌 3은 일정하게 납입, 계좌 4는 중간에 증액 (25년 11월 입대, 25년 11월 개설 => 납입가능 회차 18회차)
-- [회원 2] account_id = 3 (ACTIVE: 18회차 / 30만원 고정)
(11, 3, 1, 300000, '2025-12-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(12, 3, 2, 300000, '2026-01-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(13, 3, 3, 300000, '2026-02-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(14, 3, 4, 300000, '2026-03-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(15, 3, 5, 300000, '2026-04-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(16, 3, 6, 300000, '2026-05-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(17, 3, 7, 300000, '2026-06-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(18, 3, 8, 300000, '2026-07-15 14:30:00', 'seokyun', NULL, NULL, 'N'),

-- [회원 2] account_id = 4 (ACTIVE: 납입 금액 변동 시나리오 / 1~7회차: 10만원, 8~회차: 25만원)
(19, 4, 1, 100000, '2025-12-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(20, 4, 2, 100000, '2026-01-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(21, 4, 3, 100000, '2026-02-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(22, 4, 4, 100000, '2026-03-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(23, 4, 5, 100000, '2026-04-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(24, 4, 6, 100000, '2026-05-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(25, 4, 7, 100000, '2026-06-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(26, 4, 8, 250000, '2026-07-15 14:35:00', 'seokyun', NULL, NULL, 'N'),

-- 회원 3 (TERMINATED): 총 45만원 (30 + 15) / 3개월 후 중도 해지 (26년 2월 입대, 26년 2월 개설 => 납입가능 회차 18회차)
-- [회원 3] account_id = 5 (TERMINATED: 2회차 / 30만원 고정)
(27, 5, 1, 300000, '2026-03-20 09:00:00', 'seokyun', NULL, NULL, 'Y'),
(28, 5, 2, 300000, '2026-04-20 09:00:00', 'seokyun', NULL, NULL, 'Y'),

-- [회원 3] account_id = 6 (TERMINATED: 2회차 / 15만원 고정)
(29, 6, 1, 150000, '2026-03-20 09:05:00', 'seokyun', NULL, NULL, 'Y'),
(30, 6, 2, 150000, '2026-04-20 09:05:00', 'seokyun', NULL, NULL, 'Y');


-- --------------------------------------------------------------------
--  [태석] 여행 / 예상여행경비(travel_cost)
--  테이블: travel_cost
-- --------------------------------------------------------------------
INSERT INTO travel_cost (cost_id, goal_id, flight_cost, hotel_cost, living_cost, total_cost, remaining_budget, created_date, created_nm, del_yn)

VALUES
(1, 1,  450000, 240000,  90692,  780692,   19308, NOW(), 'jotaeseok', 'N'),
(2, 2,  380000, 520000, 313288, 1213288,  786712, NOW(), 'jotaeseok', 'N'),
(3, 3,   87000, 360000, 373497,  627000,  873000, NOW(), 'jotaeseok', 'N');


-- --------------------------------------------------------------------
--  [지원] 진로/희망 준비항목(job_interested_type)
--  테이블: job_interested_type
-- --------------------------------------------------------------------
-- ===== job_interested_type =====
INSERT INTO job_interested_type (interested_id, goal_id, item_type, created_date, created_nm, modified_date, modified_nm, del_yn) VALUES
(1, 1, 'P01', NOW(), 'jiwon', NULL, NULL, 'N'),
(2, 1, 'P03', NOW(), 'jiwon', NULL, NULL, 'N'),
(3, 2, 'P02', NOW(), 'jiwon', NULL, NULL, 'N'),
(4, 3, 'P01', NOW(), 'jiwon', NULL, NULL, 'N'),
(5, 4, 'P01', NOW(), 'jiwon', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [지원] 진로/ 진로준비항목저장(job_plan)
--  테이블: job_plan
-- --------------------------------------------------------------------
-- ===== 4. job_plan (CONFIRMED 3건에 준비항목 스냅샷) =====
INSERT INTO job_plan (plan_id, goal_id, item_type, item_name, apply_url, amount, created_date, created_nm, del_yn) VALUES
(1, 1, 'P01', '정보처리기사 실기', 'https://q-net.or.kr', 22600, NOW(), 'jiwon', 'N'),
(2, 1, 'P02', '백엔드 개발 입문 인강', 'https://www.inflearn.com', 218000, NOW(), 'jiwon', 'N'),
(3, 1, 'P03', '백엔드 개발자 양성 과정', 'https://www.work24.go.kr', 540000, NOW(), 'jiwon', 'N'),
(4, 3, 'P01', '한국사능력검정시험', 'https://www.historyexam.go.kr', 22000, NOW(), 'jiwon', 'N'),
(5, 4, 'P01', '토익', 'https://www.ybmnet.co.kr', 26250, NOW(), 'jiwon', 'N'),
(6, 4, 'P02', '편입경영학 전공 종합반 인강', 'https://www.pmg2000.co.kr', 350000, NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
--  [지원] 진로/정책서비스선택저장(service_selection)
--  테이블: service_selection
-- --------------------------------------------------------------------
-- ===== service_selection 전체 테스트 데이터 (card_id 컬럼 반영) =====
INSERT INTO service_selection (selection_id, goal_id, svc_crit_id, card_id, created_date, created_nm, modified_date, modified_nm, del_yn) VALUES
(1, 1, 1,    NULL, NOW(), 'jiwon', NULL, NULL, 'N'),  -- KB Pay (복무 중 이용)
(2, 1, 2,    NULL, NOW(), 'jiwon', NULL, NULL, 'N'),  -- 국민내일배움카드 (전역 후 신청)
(3, 3, 3,    NULL, NOW(), 'jiwon', NULL, NULL, 'N'),  -- 공무원연금 대출제도
(4, 3, 4,    NULL, NOW(), 'jiwon', NULL, NULL, 'N'),  -- KB Pay (J02)
(5, 4, 5,    NULL, NOW(), 'jiwon', NULL, NULL, 'N'),  -- 국가장학금(편입생 대상)
(6, 4, 6,    NULL, NOW(), 'jiwon', NULL, NULL, 'N'),  -- KB Pay (J03)
(7, 1, NULL, 1,    NOW(), 'jiwon', NULL, NULL, 'N');  -- 히어로즈체크카드 선택

SET FOREIGN_KEY_CHECKS = 1;