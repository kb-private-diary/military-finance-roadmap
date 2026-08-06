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
--  [석윤] 공통/ 부대정보(military_unit)
--  테이블: military_unit
-- --------------------------------------------------------------------
INSERT INTO `military_unit` (`unit_code`, `unit_name`, `type_id`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
-- 육군 (type_id=1)
('AD01', '제1보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD09', '제9보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD25', '제25보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD07', '제7보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD15', '제15보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD12', '제12보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD21', '제21보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD22', '제22보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD03', '제3보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD05', '제5보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD06', '제6보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD17', '제17보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD90', '수도기계화보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD08', '제8기동사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD11', '제11기동사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD02', '제2신속대응사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD36', '제36보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD51', '제51보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD55', '제55보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD31', '제31보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD32', '제32보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD35', '제35보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD37', '제37보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD39', '제39보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD50', '제50보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD53', '제53보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD52', '제52보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AD56', '제56보병사단', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AC01', '수도방위사령부', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AC02', '육군특수전사령부', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AC03', '육군항공사령부', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AC04', '육군미사일전략사령부', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AC05', '육군동원전력사령부', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AC06', '육군교육사령부', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('AC07', '육군군수사령부/인사사령부', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
-- 해군 (type_id=2)
('NF01', '제1함대', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NF02', '제2함대', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NF03', '제3함대', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NF90', '기동함대', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NC01', '잠수함사령부', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NC02', '해군항공사령부', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NC03', '인천해역방어사령부', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NC04', '진해기지사령부', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NC05', '해군작전사령부', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NC06', '해군교육사령부', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NC07', '해군군수사령부', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('NC08', '해군본부 및 직할', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
-- 공군 (type_id=3)
('FW01', '제1전투비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW08', '제8전투비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW10', '제10전투비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW11', '제11전투비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW16', '제16전투비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW17', '제17전투비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW18', '제18전투비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW19', '제19전투비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW20', '제20전투비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FG38', '제38전투비행전대', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW03', '제3훈련비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW05', '제5공중기동비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW15', '제15특수임무비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FW39', '제39정찰비행단', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FG06', '제6탐색구조비행전대', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FC01', '공군작전사령부', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FC02', '방공관제사령부', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FC03', '공군미사일방어사령부', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FC04', '공군교육사령부', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FC05', '공군군수사령부', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('FC06', '공군본부 및 직할', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
-- 해병대 (type_id=4)
('MD01', '해병대 제1사단', 4, NOW(), 'seokyun', NULL, NULL, 'N'),
('MD02', '해병대 제2사단', 4, NOW(), 'seokyun', NULL, NULL, 'N'),
('MB06', '제6해병여단', 4, NOW(), 'seokyun', NULL, NULL, 'N'),
('MB09', '제9해병여단', 4, NOW(), 'seokyun', NULL, NULL, 'N'),
('MB90', '연평부대', 4, NOW(), 'seokyun', NULL, NULL, 'N'),
('MC01', '해병대사령부', 4, NOW(), 'seokyun', NULL, NULL, 'N'),
('MC02', '해병대교육훈련단/군수단', 4, NOW(), 'seokyun', NULL, NULL, 'N');

-- 군종별 "기타" 캐치올 부대 (엑셀 원본 목록에는 없음, 별도 추가)
-- 목적: 목록에 없는 부대 소속 사용자도 unit_code 값을 갖게 해서 부대별 통계(GROUP BY unit_code)가 가능하도록 함.
--       unit_name은 화면에서 사용자가 직접 입력한 값으로 덮어써서 보여주고, unit_code만 이 값으로 고정.
INSERT INTO `military_unit` (`unit_code`, `unit_name`, `type_id`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
('AETC', '기타', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
('NETC', '기타', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
('FETC', '기타', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
('METC', '기타', 4, NOW(), 'seokyun', NULL, NULL, 'N'),
('OETC', '기타', 5, NOW(), 'seokyun', NULL, NULL, 'N'),
('EETC', '기타', 6, NOW(), 'seokyun', NULL, NULL, 'N');


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
--  [호빈] 차량 종류
--  테이블: car_type
-- --------------------------------------------------------------------
INSERT INTO `car_type` (`code`, `name`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, '경차', NOW(), 'hobin', NULL, NULL, 'N'),
(2, '준중형', NOW(), 'hobin', NULL, NULL, 'N'),
(3, 'SUV', NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [호빈] 신차 모델 데이터
--  테이블: car_model
-- --------------------------------------------------------------------
INSERT INTO `car_model` (`model_id`, `manufacturer`, `model_name`, `car_type_code`, `fuel_type`, `base_price`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, '기아', '모닝', 1, '가솔린', 1325, NOW(), 'hobin', NULL, NULL, 'N'),
(2, '기아', '레이', 1, '가솔린', 1400, NOW(), 'hobin', NULL, NULL, 'N'),
(3, '현대', '캐스퍼', 1, '가솔린', 1460, NOW(), 'hobin', NULL, NULL, 'N'),
(4, '현대', '아반떼', 2, '가솔린', 1964, NOW(), 'hobin', NULL, NULL, 'N'),
(5, '기아', '셀토스', 3, '가솔린', 2477, NOW(), 'hobin', NULL, NULL, 'N'),
(6, '기아', '셀토스', 3, '하이브리드', 2898, NOW(), 'hobin', NULL, NULL, 'N'),
(7, '쉐보레', '스파크', 1, '가솔린', 992, NOW(), 'hobin', NULL, NULL, 'N'),
(8, '기아', 'K3', 2, '가솔린', 1738, NOW(), 'hobin', NULL, NULL, 'N'),
(9, '현대', '투싼', 3, '가솔린', 2805, NOW(), 'hobin', NULL, NULL, 'N'),
(10, '기아', '스포티지', 3, '가솔린', 2863, NOW(), 'hobin', NULL, NULL, 'N'),
(11, '쉐보레', '트레일블레이저', 3, '가솔린', 2757, NOW(), 'hobin', NULL, NULL, 'N'),
(12, '현대', '코나', 3, '가솔린', 2360, NOW(), 'hobin', NULL, NULL, 'N'),
(13, '현대', '베뉴', 3, '가솔린', 1620, NOW(), 'hobin', NULL, NULL, 'N'),
(14, '쌍용', '티볼리', 3, '가솔린', 1872, NOW(), 'hobin', NULL, NULL, 'N'),
(15, '쉐보레', '트랙스', 3, '가솔린', 1914, NOW(), 'hobin', NULL, NULL, 'N'),
(16, '현대', '캐스퍼 일렉트릭', 1, '전기', 2787, NOW(), 'hobin', NULL, NULL, 'N'),
(17, '기아', '레이 EV', 1, '전기', 2852, NOW(), 'hobin', NULL, NULL, 'N'),
(18, '현대', '아반떼', 2, '하이브리드', 2401, NOW(), 'hobin', NULL, NULL, 'N'),
(19, '현대', '아이오닉 6', 2, '전기', 4995, NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [호빈] 취득세율 기준
--  테이블: car_tax
-- --------------------------------------------------------------------
INSERT INTO `car_tax` (`car_type_code`, `acquisition_tax_rate`, `bond_exempt_engine_cc`, `region`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 4.00, 1600, '전국', NOW(), 'hobin', NULL, NULL, 'N'),
(2, 7.00, 1600, '전국', NOW(), 'hobin', NULL, NULL, 'N'),
(3, 7.00, 1600, '전국', NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [호빈] 자동차세 연납 할인
--  테이블: car_tax_prepay
-- --------------------------------------------------------------------
INSERT INTO `car_tax_prepay` (`year`, `prepay_month`, `discount_rate`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(2026, '1월', 4.60, NOW(), 'hobin', NULL, NULL, 'N'),
(2026, '3월', 3.45, NOW(), 'hobin', NULL, NULL, 'N'),
(2026, '6월', 2.30, NOW(), 'hobin', NULL, NULL, 'N'),
(2026, '9월', 1.15, NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [호빈] 전기차 보조금
--  테이블: car_ev
-- --------------------------------------------------------------------
INSERT INTO `car_ev` (`region`, `national_subsidy`, `local_subsidy`, `base_year`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
('서울특별시', 580, 150, 2026, NOW(), 'hobin', NULL, NULL, 'N'),
('경기도', 580, 200, 2026, NOW(), 'hobin', NULL, NULL, 'N'),
('제주특별자치도', 580, 650, 2026, NOW(), 'hobin', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [호빈] 자동차 보험
--  테이블: car_insurance
--  KB자동차보험 기준 추정치 (공식 요율표 미공개, 참고용)
-- --------------------------------------------------------------------
INSERT INTO `car_insurance` (`car_type_code`, `experience_bracket`, `estimated_premium_min`, `estimated_premium_max`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
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
--  [지원] 진로/취업·공무원 분류
--  테이블: job_category
-- --------------------------------------------------------------------
INSERT INTO job_category (`category_id`, `parent_id`, `goal_type`, `category_name`, `category_level`,`created_date`, `created_nm`, `del_yn`) VALUES
(1, NULL, 'J01', '경영·사무', 1, NOW(), 'jiwon',  'N'),
(2, NULL, 'J01', '마케팅·광고·홍보', 1, NOW(), 'jiwon',  'N'),
(3, NULL, 'J01', '무역·유통', 1, NOW(), 'jiwon',  'N'),
(4, NULL, 'J01', 'IT·개발', 1, NOW(), 'jiwon',  'N'),
(5, NULL, 'J01', '생산·제조', 1, NOW(), 'jiwon',  'N'),
(6, NULL, 'J01', '영업·고객상담', 1, NOW(), 'jiwon',  'N'),
(7, NULL, 'J01', '건설', 1, NOW(), 'jiwon',  'N'),
(8, NULL, 'J01', '금융', 1, NOW(), 'jiwon',  'N'),
(9, NULL, 'J01', '연구개발·설계', 1, NOW(), 'jiwon',  'N'),
(10, NULL, 'J01', '디자인', 1, NOW(), 'jiwon',  'N'),
(11, NULL, 'J01', '미디어', 1, NOW(), 'jiwon',  'N'),
(12, NULL, 'J01', '전문·특수직', 1, NOW(), 'jiwon',  'N'),
(13, NULL, 'J02', '일반직 9급', 1, NOW(), 'jiwon',  'N'),
(14, NULL, 'J02', '군무원 9급', 1, NOW(), 'jiwon',  'N'),
(15, NULL, 'J02', '경찰', 1, NOW(), 'jiwon',  'N'),
(16, NULL, 'J02', '소방', 1, NOW(), 'jiwon',  'N'),
(17, 1, 'J01', '기획·전략·경영', 2, NOW(), 'jiwon', 'N'),
(18, 1, 'J01', '인사·노무·교육', 2, NOW(), 'jiwon',  'N'),
(19, 1, 'J01', '재무·세무·IR', 2, NOW(), 'jiwon',  'N'),
(20, 1, 'J01', '경리·회계·결산', 2, NOW(), 'jiwon',  'N'),
(21, 1, 'J01', '일반사무·총무·비서', 2, NOW(), 'jiwon',  'N'),
(22, 1, 'J01', '법무', 2, NOW(), 'jiwon',  'N'),
(23, 2, 'J01', '마케팅', 2, NOW(), 'jiwon', 'N'),
(24, 2, 'J01', '광고·홍보', 2, NOW(), 'jiwon',  'N'),
(25, 3, 'J01', '유통·물류·재고', 2, NOW(), 'jiwon',  'N'),
(26, 3, 'J01', '무역·해외영업', 2, NOW(), 'jiwon',  'N'),
(27, 3, 'J01', '구매·자재', 2, NOW(), 'jiwon',  'N'),
(28, 3, 'J01', '운전·운송', 2, NOW(), 'jiwon',  'N'),
(29, 3, 'J01', '상품기획·MD', 2, NOW(), 'jiwon',  'N'),
(30, 4, 'J01', 'QA', 2, NOW(), 'jiwon',  'N'),
(31, 4, 'J01', '앱개발', 2, NOW(), 'jiwon',  'N'),
(32, 4, 'J01', '웹개발', 2, NOW(), 'jiwon',  'N'),
(33, 4, 'J01', '데이터엔지니어·데이터분석·DBA', 2, NOW(), 'jiwon',  'N'),
(34, 4, 'J01', '시스템프로그래머', 2, NOW(), 'jiwon',  'N'),
(35, 4, 'J01', '응용프로그래머', 2, NOW(), 'jiwon',  'N'),
(36, 4, 'J01', '네트워크·보안·운영', 2, NOW(), 'jiwon',  'N'),
(37, 4, 'J01', 'AI·빅데이터', 2, NOW(), 'jiwon',  'N'),
(38, 4, 'J01', '게임개발', 2, NOW(), 'jiwon',  'N'),
(39, 4, 'J01', 'HW·임베디드', 2, NOW(), 'jiwon',  'N'),
(40, 4, 'J01', 'SW·솔루션·ERP', 2, NOW(), 'jiwon',  'N'),
(41, 4, 'J01', '서비스기획·PM', 2, NOW(), 'jiwon',  'N'),
(42, 5, 'J01', '생산관리·공정관리·품질관리', 2, NOW(), 'jiwon', 'N'),
(43, 5, 'J01', '안전·환경관리', 2, NOW(), 'jiwon',  'N'),
(44, 5, 'J01', '생산·제조·설비·조립', 2, NOW(), 'jiwon',  'N'),
(45, 5, 'J01', '설치·정비·AS·시공·공무', 2, NOW(), 'jiwon',  'N'),
(46, 6, 'J01', '제품·서비스영업', 2, NOW(), 'jiwon',  'N'),
(47, 6, 'J01', 'IT·솔루션·기술영업', 2, NOW(), 'jiwon',  'N'),
(48, 6, 'J01', 'B2B·법인영업', 2, NOW(), 'jiwon',  'N'),
(49, 6, 'J01', '영업관리·지원·기획', 2, NOW(), 'jiwon',  'N'),
(50, 6, 'J01', '아웃바운드', 2, NOW(), 'jiwon',  'N'),
(51, 6, 'J01', '인바운드', 2, NOW(), 'jiwon', 'N'),
(52, 6, 'J01', '고객응대·CS', 2, NOW(), 'jiwon',  'N'),
(53, 6, 'J01', '금융·보험영업', 2, NOW(), 'jiwon',  'N'),
(54, 7, 'J01', '현장·시공·감리·공무', 2, NOW(), 'jiwon',  'N'),
(55, 7, 'J01', '안전·품질관리', 2, NOW(), 'jiwon',  'N'),
(56, 7, 'J01', '전기·통신', 2, NOW(), 'jiwon',  'N'),
(57, 7, 'J01', '기계·설비·화학', 2, NOW(), 'jiwon',  'N'),
(58, 7, 'J01', '토목·조경·도시', 2, NOW(), 'jiwon',  'N'),
(59, 7, 'J01', '건축·설계·인테리어', 2, NOW(), 'jiwon',  'N'),
(60, 7, 'J01', '환경·플랜트', 2, NOW(), 'jiwon',  'N'),
(61, 7, 'J01', '부동산·영업·견적', 2, NOW(), 'jiwon',  'N'),
(62, 8, 'J01', '증권·투자', 2, NOW(), 'jiwon',  'N'),
(63, 8, 'J01', '외환·펀드·자산운용', 2, NOW(), 'jiwon',  'N'),
(64, 8, 'J01', '보험계리·손해사정', 2, NOW(), 'jiwon',  'N'),
(65, 8, 'J01', '채권·심사', 2, NOW(), 'jiwon',  'N'),
(66, 8, 'J01', '은행원', 2, NOW(), 'jiwon',  'N'),
(67, 8, 'J01', '애널리스트', 2, NOW(), 'jiwon',  'N'),
(68, 9, 'J01', '자동차·기계', 2, NOW(), 'jiwon',  'N'),
(69, 9, 'J01', '화학·에너지·환경', 2, NOW(), 'jiwon',  'N'),
(70, 9, 'J01', '바이오·제약·식품', 2, NOW(), 'jiwon',  'N'),
(71, 9, 'J01', '기계설계·CAD·CAM', 2, NOW(), 'jiwon',  'N'),
(72, 9, 'J01', '전기·전자·제어', 2, NOW(), 'jiwon',  'N'),
(73, 9, 'J01', '반도체·디스플레이', 2, NOW(), 'jiwon',  'N'),
(74, 9, 'J01', '통신기술·네트워크', 2, NOW(), 'jiwon',  'N'),
(75, 9, 'J01', '금속·철강', 2, NOW(), 'jiwon',  'N'),
(76, 9, 'J01', '조선·항공·우주', 2, NOW(), 'jiwon',  'N'),
(77, 9, 'J01', '인문·사회과학', 2, NOW(), 'jiwon',  'N'),
(78, 10, 'J01', '광고·시각디자인', 2, NOW(), 'jiwon',  'N'),
(79, 10, 'J01', '제품·산업디자인', 2, NOW(), 'jiwon',  'N'),
(80, 10, 'J01', '건축·인테리어디자인', 2, NOW(), 'jiwon',  'N'),
(81, 10, 'J01', '의류·패션·잡화디자인', 2, NOW(), 'jiwon',  'N'),
(82, 10, 'J01', 'UI·UX디자인', 2, NOW(), 'jiwon', 'N'),
(83, 11, 'J01', '연출·제작·PD·작가', 2, NOW(), 'jiwon',  'N'),
(84, 11, 'J01', '음악·영상·사진', 2, NOW(), 'jiwon',  'N'),
(85, 11, 'J01', '아나운서·리포터·성우·기자', 2, NOW(), 'jiwon',  'N'),
(86, 11, 'J01', '무대·스태프·오퍼레이터', 2, NOW(), 'jiwon',  'N'),
(87, 11, 'J01', '연예·엔터테인먼트', 2, NOW(), 'jiwon',  'N'),
(88, 11, 'J01', '인쇄·출판·편집', 2, NOW(), 'jiwon',  'N'),
(89, 12, 'J01', '리서치·시장조사', 2, NOW(), 'jiwon',  'N'),
(90, 12, 'J01', '외국어·번역·통역', 2, NOW(), 'jiwon',  'N'),
(91, 12, 'J01', '법률·특허·상표', 2, NOW(), 'jiwon',  'N'),
(92, 12, 'J01', '회계·세무·CPA·CFA', 2, NOW(), 'jiwon',  'N'),
(93, 12, 'J01', '보안·경비·경호', 2, NOW(), 'jiwon',  'N'),
(94, 12, 'J01', '보건·의료', 2, NOW(), 'jiwon',  'N'),
(95, 12, 'J01', '초·중·고 교사', 2, NOW(), 'jiwon',  'N'),
(96, 12, 'J01', '교육개발·기획', 2, NOW(), 'jiwon',  'N'),
(97, 12, 'J01', '외국어·자격증·기술강사', 2, NOW(), 'jiwon',  'N'),
(98, 12, 'J01', '사회복지·요양보호', 2, NOW(), 'jiwon',  'N'),
(99, 12, 'J01', '승무원·숙박·여행서비스', 2, NOW(), 'jiwon',  'N'),
(100, 12, 'J01', '음식서비스', 2, NOW(), 'jiwon',  'N'),
(101, 13, 'J02', '일반행정', 2, NOW(), 'jiwon',  'N'),
(102, 13, 'J02', '교육행정', 2, NOW(), 'jiwon',  'N'),
(103, 13, 'J02', '세무', 2, NOW(), 'jiwon',  'N'),
(104, 13, 'J02', '사회복지', 2, NOW(), 'jiwon',  'N'),
(105, 13, 'J02', '교정', 2, NOW(), 'jiwon',  'N'),
(106, 13, 'J02', '전산', 2, NOW(), 'jiwon',  'N'),
(107, 13, 'J02', '관세', 2, NOW(), 'jiwon',  'N'),
(108, 13, 'J02', '출입국관리', 2, NOW(), 'jiwon',  'N'),
(109, 14, 'J02', '행정직', 2, NOW(), 'jiwon',  'N'),
(110, 14, 'J02', '군수직', 2, NOW(), 'jiwon',  'N'),
(111, 14, 'J02', '전산직', 2, NOW(), 'jiwon',  'N'),
(112, 14, 'J02', '시설직', 2, NOW(), 'jiwon',  'N'),
(113, 14, 'J02', '전기직', 2, NOW(), 'jiwon',  'N'),
(114, 15, 'J02', '순경 공채', 2, NOW(), 'jiwon',  'N'),
(115, 15, 'J02', '경찰행정 경채', 2, NOW(), 'jiwon',  'N'),
(116, 15, 'J02', '해양경찰 순경', 2, NOW(), 'jiwon',  'N'),
(117, 16, 'J02', '소방 공채', 2, NOW(), 'jiwon',  'N'),
(118, 16, 'J02', '구급 경채', 2, NOW(), 'jiwon',  'N'),
(119, 16, 'J02', '구조 경채', 2, NOW(), 'jiwon',  'N');



-- --------------------------------------------------------------------
--  [지원] 진로/편입 대학교
--  테이블: job_transfer_university
-- --------------------------------------------------------------------
INSERT INTO job_transfer_university (univ_id, univ_name, created_date, created_nm, del_yn) VALUES
(1, '가천대학교', NOW(), 'jiwon', 'N'),
(2, '가톨릭대학교', NOW(), 'jiwon', 'N'),
(3, '건국대학교', NOW(), 'jiwon', 'N'),
(4, '경북대학교', NOW(), 'jiwon', 'N'),
(5, '경희대학교', NOW(), 'jiwon', 'N'),
(6, '고려대학교', NOW(), 'jiwon', 'N'),
(7, '광운대학교', NOW(), 'jiwon', 'N'),
(8, '국민대학교', NOW(), 'jiwon', 'N'),
(9, '단국대학교', NOW(), 'jiwon', 'N'),
(10, '동국대학교', NOW(), 'jiwon', 'N'),
(11, '명지대학교', NOW(), 'jiwon', 'N'),
(12, '부산대학교', NOW(), 'jiwon', 'N'),
(13, '상명대학교', NOW(), 'jiwon', 'N'),
(14, '서강대학교', NOW(), 'jiwon', 'N'),
(15, '서울시립대학교', NOW(), 'jiwon', 'N'),
(16, '성균관대학교', NOW(), 'jiwon', 'N'),
(17, '세종대학교', NOW(), 'jiwon', 'N'),
(18, '숙명여자대학교', NOW(), 'jiwon', 'N'),
(19, '숭실대학교', NOW(), 'jiwon', 'N'),
(20, '아주대학교', NOW(), 'jiwon', 'N'),
(21, '연세대학교', NOW(), 'jiwon', 'N'),
(22, '이화여자대학교', NOW(), 'jiwon', 'N'),
(23, '인하대학교', NOW(), 'jiwon', 'N'),
(24, '전남대학교', NOW(), 'jiwon', 'N'),
(25, '중앙대학교', NOW(), 'jiwon', 'N'),
(26, '충남대학교', NOW(), 'jiwon', 'N'),
(27, '충북대학교', NOW(), 'jiwon', 'N'),
(28, '한국외국어대학교', NOW(), 'jiwon', 'N'),
(29, '한양대학교', NOW(), 'jiwon', 'N'),
(30, '홍익대학교', NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
--  [지원] 진로/편입 학과계열
--  테이블: job_transfer_major_category
-- --------------------------------------------------------------------
INSERT INTO job_transfer_major_category
    (major_code, major_name, created_date, created_nm, del_yn)
VALUES
    ('M01', '경영·회계·금융', NOW(), 'jiwon', 'N'),
    ('M02', '경제·무역·통계', NOW(), 'jiwon', 'N'),
    ('M03', '컴퓨터·SW·AI·데이터', NOW(), 'jiwon', 'N'),
    ('M04', '전기·전자·반도체·통신', NOW(), 'jiwon', 'N'),
    ('M05', '기계·자동차·로봇·항공', NOW(), 'jiwon', 'N'),
    ('M06', '산업·안전·시스템공학', NOW(), 'jiwon', 'N'),
    ('M07', '건축·토목·도시·환경', NOW(), 'jiwon', 'N'),
    ('M08', '화학·신소재·에너지공학', NOW(), 'jiwon', 'N'),
    ('M09', '생명·바이오·식품·농림', NOW(), 'jiwon', 'N'),
    ('M10', '간호·의약·보건', NOW(), 'jiwon', 'N'),
    ('M11', '법·행정·정치·사회', NOW(), 'jiwon', 'N'),
    ('M12', '교육·심리·아동·상담', NOW(), 'jiwon', 'N'),
    ('M13', '어문·인문', NOW(), 'jiwon', 'N'),
    ('M14', '미디어·광고·콘텐츠', NOW(), 'jiwon', 'N'),
    ('M15', '디자인·예술·체육·의류', NOW(), 'jiwon', 'N'),
    ('M16', '수학·물리·화학·지구과학', NOW(), 'jiwon', 'N'),
    ('M17', '관광·호텔·서비스', NOW(), 'jiwon', 'N'),
    ('M18', '자율·융합', NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
--  [지원] 진로/학교별 편입 모집 학과계열
--  테이블: job_transfer_major
-- --------------------------------------------------------------------
INSERT INTO job_transfer_major
    (major_id, univ_id, major_code, admission_year, created_date, created_nm, del_yn)
VALUES
    (1, 1, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (2, 1, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (3, 1, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (4, 1, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (5, 1, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (6, 1, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (7, 1, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (8, 1, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (9, 1, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (10, 1, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (11, 1, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (12, 1, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (13, 1, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (14, 1, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (15, 1, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (16, 1, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (17, 2, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (18, 2, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (19, 2, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (20, 2, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (21, 2, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (22, 2, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (23, 2, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (24, 2, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (25, 2, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (26, 2, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (27, 2, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (28, 2, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (29, 2, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (30, 2, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (31, 3, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (32, 3, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (33, 3, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (34, 3, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (35, 3, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (36, 3, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (37, 3, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (38, 3, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (39, 3, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (40, 3, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (41, 3, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (42, 3, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (43, 3, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (44, 3, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (45, 3, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (46, 3, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (47, 4, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (48, 4, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (49, 4, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (50, 4, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (51, 4, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (52, 4, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (53, 4, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (54, 4, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (55, 4, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (56, 4, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (57, 4, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (58, 4, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (59, 4, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (60, 4, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (61, 4, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (62, 4, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (63, 4, 'M17', 2026, NOW(), 'jiwon', 'N'),
    (64, 5, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (65, 5, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (66, 5, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (67, 5, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (68, 5, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (69, 5, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (70, 5, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (71, 5, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (72, 5, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (73, 5, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (74, 5, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (75, 5, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (76, 6, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (77, 6, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (78, 6, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (79, 6, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (80, 6, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (81, 6, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (82, 6, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (83, 6, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (84, 6, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (85, 6, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (86, 6, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (87, 6, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (88, 6, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (89, 6, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (90, 7, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (91, 7, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (92, 7, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (93, 7, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (94, 7, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (95, 7, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (96, 7, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (97, 7, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (98, 7, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (99, 7, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (100, 7, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (101, 7, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (102, 7, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (103, 8, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (104, 8, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (105, 8, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (106, 8, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (107, 8, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (108, 8, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (109, 8, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (110, 8, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (111, 8, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (112, 8, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (113, 8, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (114, 8, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (115, 8, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (116, 8, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (117, 9, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (118, 9, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (119, 9, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (120, 9, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (121, 9, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (122, 9, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (123, 9, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (124, 9, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (125, 9, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (126, 9, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (127, 9, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (128, 9, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (129, 10, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (130, 10, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (131, 11, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (132, 11, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (133, 11, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (134, 11, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (135, 11, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (136, 11, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (137, 11, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (138, 12, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (139, 12, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (140, 12, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (141, 12, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (142, 12, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (143, 12, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (144, 12, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (145, 12, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (146, 12, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (147, 12, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (148, 12, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (149, 12, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (150, 12, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (151, 12, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (152, 12, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (153, 12, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (154, 12, 'M17', 2026, NOW(), 'jiwon', 'N'),
    (155, 13, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (156, 13, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (157, 13, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (158, 13, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (159, 13, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (160, 13, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (161, 13, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (162, 13, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (163, 13, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (164, 13, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (165, 14, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (166, 14, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (167, 14, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (168, 14, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (169, 14, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (170, 14, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (171, 14, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (172, 14, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (173, 14, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (174, 14, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (175, 14, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (176, 15, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (177, 15, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (178, 15, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (179, 15, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (180, 15, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (181, 15, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (182, 15, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (183, 15, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (184, 15, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (185, 15, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (186, 15, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (187, 16, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (188, 16, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (189, 16, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (190, 16, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (191, 16, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (192, 16, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (193, 16, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (194, 17, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (195, 17, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (196, 17, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (197, 17, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (198, 17, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (199, 17, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (200, 17, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (201, 17, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (202, 17, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (203, 17, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (204, 17, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (205, 17, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (206, 17, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (207, 17, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (208, 17, 'M18', 2026, NOW(), 'jiwon', 'N'),
    (209, 18, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (210, 18, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (211, 18, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (212, 18, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (213, 18, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (214, 18, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (215, 18, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (216, 18, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (217, 18, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (218, 18, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (219, 18, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (220, 18, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (221, 18, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (222, 18, 'M17', 2026, NOW(), 'jiwon', 'N'),
    (223, 19, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (224, 19, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (225, 19, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (226, 19, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (227, 19, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (228, 19, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (229, 19, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (230, 19, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (231, 19, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (232, 19, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (233, 19, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (234, 19, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (235, 19, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (236, 20, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (237, 20, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (238, 20, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (239, 20, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (240, 20, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (241, 20, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (242, 20, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (243, 20, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (244, 20, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (245, 20, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (246, 20, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (247, 20, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (248, 20, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (249, 20, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (250, 20, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (251, 21, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (252, 21, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (253, 21, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (254, 21, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (255, 21, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (256, 21, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (257, 21, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (258, 21, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (259, 21, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (260, 21, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (261, 21, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (262, 21, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (263, 21, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (264, 21, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (265, 21, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (266, 21, 'M18', 2026, NOW(), 'jiwon', 'N'),
    (267, 22, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (268, 22, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (269, 22, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (270, 22, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (271, 22, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (272, 22, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (273, 22, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (274, 22, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (275, 22, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (276, 22, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (277, 22, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (278, 22, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (279, 22, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (280, 22, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (281, 22, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (282, 23, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (283, 23, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (284, 23, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (285, 23, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (286, 23, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (287, 23, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (288, 23, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (289, 23, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (290, 23, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (291, 23, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (292, 23, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (293, 23, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (294, 23, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (295, 23, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (296, 23, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (297, 24, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (298, 24, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (299, 24, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (300, 24, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (301, 24, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (302, 24, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (303, 24, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (304, 24, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (305, 24, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (306, 24, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (307, 24, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (308, 24, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (309, 24, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (310, 24, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (311, 24, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (312, 24, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (313, 24, 'M18', 2026, NOW(), 'jiwon', 'N'),
    (314, 25, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (315, 25, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (316, 25, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (317, 25, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (318, 25, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (319, 25, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (320, 25, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (321, 25, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (322, 25, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (323, 25, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (324, 25, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (325, 25, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (326, 25, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (327, 25, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (328, 25, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (329, 26, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (330, 26, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (331, 26, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (332, 26, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (333, 26, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (334, 26, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (335, 26, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (336, 26, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (337, 26, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (338, 26, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (339, 26, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (340, 26, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (341, 26, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (342, 26, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (343, 26, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (344, 27, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (345, 27, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (346, 27, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (347, 27, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (348, 27, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (349, 27, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (350, 27, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (351, 27, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (352, 27, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (353, 27, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (354, 27, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (355, 27, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (356, 27, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (357, 27, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (358, 27, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (359, 28, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (360, 28, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (361, 28, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (362, 28, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (363, 28, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (364, 28, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (365, 29, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (366, 29, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (367, 29, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (368, 29, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (369, 29, 'M06', 2026, NOW(), 'jiwon', 'N'),
    (370, 29, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (371, 29, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (372, 29, 'M09', 2026, NOW(), 'jiwon', 'N'),
    (373, 29, 'M10', 2026, NOW(), 'jiwon', 'N'),
    (374, 29, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (375, 29, 'M12', 2026, NOW(), 'jiwon', 'N'),
    (376, 29, 'M13', 2026, NOW(), 'jiwon', 'N'),
    (377, 29, 'M14', 2026, NOW(), 'jiwon', 'N'),
    (378, 29, 'M15', 2026, NOW(), 'jiwon', 'N'),
    (379, 29, 'M16', 2026, NOW(), 'jiwon', 'N'),
    (380, 29, 'M17', 2026, NOW(), 'jiwon', 'N'),
    (381, 30, 'M01', 2026, NOW(), 'jiwon', 'N'),
    (382, 30, 'M02', 2026, NOW(), 'jiwon', 'N'),
    (383, 30, 'M03', 2026, NOW(), 'jiwon', 'N'),
    (384, 30, 'M04', 2026, NOW(), 'jiwon', 'N'),
    (385, 30, 'M05', 2026, NOW(), 'jiwon', 'N'),
    (386, 30, 'M07', 2026, NOW(), 'jiwon', 'N'),
    (387, 30, 'M08', 2026, NOW(), 'jiwon', 'N'),
    (388, 30, 'M11', 2026, NOW(), 'jiwon', 'N'),
    (389, 30, 'M13', 2026, NOW(), 'jiwon', 'N');

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
(20, 'SAVING', TRUE, 'TRV-01', '국민은행', 'KB두근두근여행적금', '실명의 개인', 50000, 1000000, '노랑풍선 제휴 상품\n① 1회차 납입 시: 패키지여행 1만원 할인쿠폰 (20만원 이상 결제)\n② 4회차 납입 시: 할인 쿠폰팩 4종\n   - 패키지여행 4% 할인\n   - 호텔 1만원 (15만원 이상)\n   - 항공 5천원 (20만원 이상, 해외 전용)\n   - 액티비티 5천원 (10만원 이상)\n※ 쿠폰 등록기간: 제공일로부터 1개월, 사용기간 최대 2년', 'S', 'S', 6, '① 여행친구 우대이율: 최고 연 0.6%p\n    - 인증번호 제공: 연 0.6%p\n    - 인증번호 입력: 연 0.4%p\n② 오픈뱅킹 우대이율: 연 0.3%p\n③ 자동이체저축 우대이율: 연 0.1%p', 2.65, 3.65, FALSE, 0.0, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01001495', NOW(), 'jotaeseok', NULL, NULL, 'N');


-- --------------------------------------------------------------------
--  [석윤] 공통/ 군적금 상품 만기·중도해지이율(military_saving_product)
--  테이블: military_saving_product
--  출처(KB): KB장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 제2026-3282호, 2026.07.21 현재 세전) — 전부 확인, 스크린샷 기반 기존 데이터와 100% 일치
--  출처(IBK): IBK장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 제2025-8992호, 2026.1.5 현재 세전) — 기본이자율/우대이자율/중도해지이자율(가계우대정기적금 준용) 전부 확인
--  출처(신한): 신한 장병내일준비적금 상품설명서(PDF, 준법감시인 사전심사필 제2026-13556-1호, 2026.07.24 현재 세전) — 계약기간 4구간(1~6/6~12/12~15/15~24개월), 1~6개월 구간은 우대이율 미적용
--    ※ gov_match_rate는 PDF에 "3:1 매칭지원금"이라고만 표기(표 없음) — 병역법 시행령상 국가 공통 정책으로 보고 KB/IBK와 동일하게 100 적용, 확인 필요
--  출처(하나): 하나 장병내일준비적금 상품설명서(PDF, 준법감시인 심의필 제2026-설명서-027호, 2026.03.03 현재 세전) — 중도해지금리가 1~6개월 구간은 고정값(0.10/0.15/0.20%), 6개월 이상만 산식 적용
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
(38, '081', '장병내일준비적금', 10, 300000, 24, 100.00, 'WITHDRAWAL', 'MONTH', 11, NULL, NULL, NULL, 90, 0.20, NOW(), 'seokyun', NULL, NULL, 'N');


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
--  [태석] 여행 / 숙박비(hotel_cost)
--  테이블: hotel_cost
-- --------------------------------------------------------------------
INSERT INTO hotel_cost (
    city_cost_id,q1_cost,q2_cost,q3_cost,q4_cost,created_date,created_nm,del_yn
)
VALUES
    -- 일본
    (1,  156000, 182000, 170000, 186000, NOW(), 'jotaeseok', 'N'), -- 오사카
    (2,  174000, 203000, 189000, 207000, NOW(), 'jotaeseok', 'N'), -- 도쿄
    (3,  170000, 199000, 185000, 202000, NOW(), 'jotaeseok', 'N'), -- 교토
    (4,  163000, 190000, 177000, 194000, NOW(), 'jotaeseok', 'N'), -- 나고야
    (5,  160000, 187000, 174000, 190000, NOW(), 'jotaeseok', 'N'), -- 아이치
    (6,  146000, 171000, 159000, 174000, NOW(), 'jotaeseok', 'N'), -- 아키타
    (7,  162000, 189000, 176000, 193000, NOW(), 'jotaeseok', 'N'), -- 아마가사키
    (8,  163000, 190000, 177000, 194000, NOW(), 'jotaeseok', 'N'), -- 아오모리
    (9,  162000, 189000, 176000, 192000, NOW(), 'jotaeseok', 'N'), -- 아쓰기
    (10, 156000, 182000, 170000, 185000, NOW(), 'jotaeseok', 'N'), -- 벳푸
    (11, 154000, 180000, 168000, 183000, NOW(), 'jotaeseok', 'N'), -- 지바
    (12, 158000, 185000, 173000, 188000, NOW(), 'jotaeseok', 'N'), -- 에비나
    (13, 154000, 180000, 168000, 183000, NOW(), 'jotaeseok', 'N'), -- 후쿠이
    (14, 167000, 195000, 182000, 198000, NOW(), 'jotaeseok', 'N'), -- 후쿠오카
    (15, 177000, 206000, 192000, 210000, NOW(), 'jotaeseok', 'N'), -- 후쿠시마
    (16, 162000, 189000, 176000, 192000, NOW(), 'jotaeseok', 'N'), -- 후쿠야마
    (17, 144000, 168000, 157000, 172000, NOW(), 'jotaeseok', 'N'), -- 후나바시
    (18, 157000, 184000, 171000, 187000, NOW(), 'jotaeseok', 'N'), -- 기후
    (19, 169000, 197000, 184000, 200000, NOW(), 'jotaeseok', 'N'), -- 하치오지
    (20, 194000, 227000, 211000, 231000, NOW(), 'jotaeseok', 'N'), -- 하코다테

    -- 베트남
    (21, 167000, 142000, 139000, 170000, NOW(), 'jotaeseok', 'N'), -- 하노이
    (22, 167000, 142000, 139000, 170000, NOW(), 'jotaeseok', 'N'), -- 호치민
    (23, 205000, 175000, 171000, 209000, NOW(), 'jotaeseok', 'N'), -- 다낭
    (24, 160000, 136000, 133000, 163000, NOW(), 'jotaeseok', 'N'), -- 달랏
    (25, 205000, 175000, 171000, 209000, NOW(), 'jotaeseok', 'N'), -- 하롱베이
    (26, 187000, 159000, 156000, 190000, NOW(), 'jotaeseok', 'N'), -- 깜라인
    (27, 153000, 131000, 128000, 156000, NOW(), 'jotaeseok', 'N'), -- 박장
    (28, 152000, 129000, 127000, 155000, NOW(), 'jotaeseok', 'N'), -- 박닌
    (29, 145000, 124000, 121000, 148000, NOW(), 'jotaeseok', 'N'), -- 비엔호아
    (30, 157000, 134000, 131000, 160000, NOW(), 'jotaeseok', 'N'), -- 빈즈엉
    (31, 165000, 140000, 137000, 168000, NOW(), 'jotaeseok', 'N'), -- 부온마투옷
    (32, 156000, 133000, 130000, 159000, NOW(), 'jotaeseok', 'N'), -- 박깐
    (33, 163000, 139000, 136000, 166000, NOW(), 'jotaeseok', 'N'), -- 까마우
    (34, 166000, 141000, 138000, 169000, NOW(), 'jotaeseok', 'N'), -- 껌파
    (35, 156000, 132000, 130000, 158000, NOW(), 'jotaeseok', 'N'), -- 껀터
    (36, 163000, 139000, 136000, 166000, NOW(), 'jotaeseok', 'N'), -- 디엔비엔푸
    (37, 160000, 137000, 134000, 163000, NOW(), 'jotaeseok', 'N'), -- 동허이
    (38, 137000, 117000, 114000, 140000, NOW(), 'jotaeseok', 'N'), -- 동쏘아이
    (39, 166000, 141000, 138000, 169000, NOW(), 'jotaeseok', 'N'), -- 하동

    -- 대한민국
    (40,  70000,  80000, 100000,  75000, NOW(), 'jotaeseok', 'N'), -- 부산
    (41,  85000,  90000,  90000,  95000, NOW(), 'jotaeseok', 'N'), -- 서울
    (42,  60000,  65000,  70000,  65000, NOW(), 'jotaeseok', 'N'), -- 대구
    (43,  60000,  65000,  65000,  65000, NOW(), 'jotaeseok', 'N'), -- 대전
    (44,  65000,  75000, 110000,  70000, NOW(), 'jotaeseok', 'N'), -- 강릉
    (45,  60000,  70000,  90000,  65000, NOW(), 'jotaeseok', 'N'), -- 춘천
    (46,  65000,  75000, 100000,  70000, NOW(), 'jotaeseok', 'N'), -- 거제
    (47,  55000,  60000,  70000,  65000, NOW(), 'jotaeseok', 'N'), -- 안동
    (48,  55000,  65000, 105000,  60000, NOW(), 'jotaeseok', 'N'), -- 보령
    (49,  55000,  65000,  70000,  65000, NOW(), 'jotaeseok', 'N'), -- 창원
    (50,  55000,  60000,  65000,  65000, NOW(), 'jotaeseok', 'N'), -- 천안
    (51,  55000,  60000,  65000,  60000, NOW(), 'jotaeseok', 'N'), -- 청주
    (52,  55000,  65000,  85000,  60000, NOW(), 'jotaeseok', 'N'), -- 충주
    (53,  55000,  60000,  65000,  60000, NOW(), 'jotaeseok', 'N'), -- 안산
    (54,  60000,  65000,  65000,  65000, NOW(), 'jotaeseok', 'N'), -- 안양
    (55,  55000,  60000,  60000,  60000, NOW(), 'jotaeseok', 'N'), -- 부천
    (56,  50000,  55000,  60000,  55000, NOW(), 'jotaeseok', 'N'); -- 김천



-- --------------------------------------------------------------------
--  [태석] 여행 / 항공비(flight_cost)
--  테이블: flight_cost
-- --------------------------------------------------------------------
INSERT INTO flight_cost (
    city_cost_id,q1_cost,q2_cost,q3_cost,q4_cost,created_date,created_nm,del_yn
)
VALUES
    -- 일본
    (1, 300000, 320000, 350000, 330000,
     NOW(), 'jotaeseok', 'N'),

    (2, 380000, 340000, 290000, 400000,
     NOW(), 'jotaeseok', 'N'),

    (3, 300000, 320000, 350000, 330000,
     NOW(), 'jotaeseok', 'N'),

    (4, 340000, 320000, 300000, 350000,
     NOW(), 'jotaeseok', 'N'),

    (5, 340000, 320000, 300000, 350000,
     NOW(), 'jotaeseok', 'N'),

    (6, 430000, 400000, 420000, 450000,
     NOW(), 'jotaeseok', 'N'),

    (7, 300000, 320000, 350000, 330000,
     NOW(), 'jotaeseok', 'N'),

    (8, 390000, 380000, 410000, 420000,
     NOW(), 'jotaeseok', 'N'),

    (9, 420000, 400000, 380000, 430000,
     NOW(), 'jotaeseok', 'N'),

    (10, 350000, 320000, 330000, 360000,
     NOW(), 'jotaeseok', 'N'),

    (11, 380000, 340000, 290000, 400000,
     NOW(), 'jotaeseok', 'N'),

    (12, 420000, 400000, 380000, 430000,
     NOW(), 'jotaeseok', 'N'),

    (13, 360000, 330000, 340000, 370000,
     NOW(), 'jotaeseok', 'N'),

    (14, 280000, 260000, 290000, 300000,
     NOW(), 'jotaeseok', 'N'),

    (15, 360000, 340000, 350000, 380000,
     NOW(), 'jotaeseok', 'N'),

    (16, 340000, 320000, 330000, 350000,
     NOW(), 'jotaeseok', 'N'),

    (17, 380000, 340000, 290000, 400000,
     NOW(), 'jotaeseok', 'N'),

    (18, 340000, 320000, 300000, 350000,
     NOW(), 'jotaeseok', 'N'),

    (19, 420000, 400000, 380000, 430000,
     NOW(), 'jotaeseok', 'N'),

    (20, 420000, 400000, 440000, 430000,
     NOW(), 'jotaeseok', 'N'),

    -- 베트남
    (21, 430000, 380000, 450000, 420000,
     NOW(), 'jotaeseok', 'N'),

    (22, 470000, 420000, 480000, 450000,
     NOW(), 'jotaeseok', 'N'),

    (23, 430000, 370000, 420000, 450000,
     NOW(), 'jotaeseok', 'N'),

    (24, 500000, 450000, 480000, 520000,
     NOW(), 'jotaeseok', 'N'),

    (25, 430000, 380000, 450000, 420000,
     NOW(), 'jotaeseok', 'N'),

    (26, 450000, 400000, 460000, 470000,
     NOW(), 'jotaeseok', 'N'),

    (27, 430000, 380000, 450000, 420000,
     NOW(), 'jotaeseok', 'N'),

    (28, 430000, 380000, 450000, 420000,
     NOW(), 'jotaeseok', 'N'),

    (29, 470000, 420000, 480000, 450000,
     NOW(), 'jotaeseok', 'N'),

    (30, 470000, 420000, 480000, 450000,
     NOW(), 'jotaeseok', 'N'),

    (31, 450000, 400000, 460000, 470000,
     NOW(), 'jotaeseok', 'N'),

    (32, 430000, 380000, 450000, 420000,
     NOW(), 'jotaeseok', 'N'),

    (33, 470000, 420000, 480000, 450000,
     NOW(), 'jotaeseok', 'N'),

    (34, 430000, 380000, 450000, 420000,
     NOW(), 'jotaeseok', 'N'),

    (35, 470000, 420000, 480000, 450000,
     NOW(), 'jotaeseok', 'N'),

    (36, 430000, 380000, 450000, 420000,
     NOW(), 'jotaeseok', 'N'),

    (37, 430000, 370000, 420000, 450000,
     NOW(), 'jotaeseok', 'N'),

    (38, 470000, 420000, 480000, 450000,
     NOW(), 'jotaeseok', 'N'),

    (39, 430000, 380000, 450000, 420000,
     NOW(), 'jotaeseok', 'N');



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
--  [지원] 진로/자격증·어학 기준
--  테이블: job_qualification
-- --------------------------------------------------------------------
INSERT INTO job_qualification(
    qual_id, qual_type, external_code, data_source, qual_name, qual_summary, organization_name, written_fee,
    practical_fee, military_fee, detail_url, last_synced_date, created_date, created_nm, del_yn) VALUES
-- 웹개발
(1, 'Q01', '1320', 'D02', '정보처리기사',
 '정보처리기사 자격으로 소프트웨어 개발과 정보시스템 구축·운영 능력을 평가하는 국가기술자격입니다.',
 '한국산업인력공단',
 19400, 22600, NULL,
 'https://www.q-net.or.kr/crf005.do?id=crf00503&jmCd=1320',
 NULL, NOW(), 'jiwon', 'N'),

(2, 'Q01', '2290', 'D02', '정보처리산업기사',
 '정보처리산업기사 자격으로 정보시스템 개발 및 운영에 필요한 실무 능력을 평가합니다.',
 '한국산업인력공단',
 19400, 20800, NULL,
 'https://www.q-net.or.kr/crf005.do?id=crf00503&jmCd=2290',
 NULL, NOW(), 'jiwon', 'N'),

(3, 'Q01', '6921', 'D02', '프로그래밍기능사',
 '프로그래밍기능사 자격으로 프로그래밍 기초와 프로그램 개발 능력을 평가합니다.',
 '한국산업인력공단',
 14500, 17200, NULL,
 'https://www.q-net.or.kr/crf005.do?id=crf00503&jmCd=6921',
 NULL, NOW(), 'jiwon', 'N'),

(4, 'Q01', NULL, 'D01', 'SQLD',
 'SQL 개발자(SQLD) 자격으로 데이터 모델링과 SQL 활용 능력을 평가하는 국가공인 민간자격입니다.',
 '한국데이터산업진흥원',
 50000, NULL, NULL,
 'https://www.dataq.or.kr/www/sub/a_04.do',
 NULL, NOW(), 'jiwon', 'N'),

-- 데이터엔지니어·DBA
(5, 'Q01', NULL, 'D01', 'ADsP',
 '데이터분석 준전문가(ADsP) 자격으로 데이터 분석 기초 역량을 평가하는 국가공인 민간자격입니다.',
 '한국데이터산업진흥원',
 50000, NULL, NULL,
 'https://www.dataq.or.kr/www/sub/a_06.do',
 NULL, NOW(), 'jiwon', 'N'),

-- 전기·전자·제어 (API 테스트용)
(6, 'Q01', '1150', 'D02', '전기기사',
 '전기기사 자격으로 전기설비의 설계·시공·유지관리 능력을 평가하는 국가기술자격입니다.',
 '한국산업인력공단',
 NULL, NULL, NULL,
 'https://www.q-net.or.kr/crf005.do?id=crf00503&jmCd=1150',
 NULL, NOW(), 'jiwon', 'N'),

-- 기획·전략·경영 (API 테스트용)
(7, 'Q01', '9521', 'D02', '사회조사분석사 2급',
 '사회조사분석사 2급 자격으로 사회조사 설계 및 자료 분석 능력을 평가합니다.',
 '한국산업인력공단',
 NULL, NULL, NULL,
 'https://www.q-net.or.kr/crf005.do?id=crf00503&jmCd=9521',
 NULL, NOW(), 'jiwon', 'N'),

-- 마케팅
(8, 'Q01', NULL, 'D01', 'GTQ 1급',
 '그래픽기술자격(GTQ) 1급으로 Adobe Photoshop 활용 능력을 평가하는 국가공인 민간자격입니다.',
 '한국생산성본부',
 42000, NULL, 34000,
 'https://license.kpc.or.kr/nasec/qlfint/qlfint/selectGtqinfomg.do',
 NULL, NOW(), 'jiwon', 'N'),

-- 공통 어학
(9, 'Q02', NULL, 'D01', 'TOEIC',
 'TOEIC은 영어 듣기와 읽기 능력을 평가하는 공인 영어시험입니다.',
 'YBM 한국TOEIC위원회',
 52500, NULL, 26200,
 NULL,
 NULL, NOW(), 'jiwon', 'N'),

(10, 'Q02', NULL, 'D01', 'TOEIC Speaking',
 'TOEIC Speaking은 영어 말하기 능력을 평가하는 공인 영어시험입니다.',
 'YBM 한국TOEIC위원회',
 84000, NULL, 67200,
 NULL,
 NULL, NOW(), 'jiwon', 'N'),

(11, 'Q02', NULL, 'D01', 'OPIc',
 'OPIc은 실제 상황 중심의 영어 말하기 능력을 평가하는 공인 영어시험입니다.',
 '멀티캠퍼스',
 84000, NULL, 55000,
 NULL,
 NULL, NOW(), 'jiwon', 'N'),

-- 공무원 대표 자격증
(12, 'Q01', NULL, 'D01', '한국사능력검정시험',
'한국사능력검정시험으로 한국사에 대한 이해와 역사적 사고력을 평가하는 인증시험입니다.',
'국사편찬위원회',
27000, NULL, NULL,
'https://www.historyexam.go.kr',
NULL, NOW(), 'jiwon', 'N');

-- --------------------------------------------------------------------
--  [지원] 진로/자격증·어학 시험일정(job_qualification_schedule)
--  테이블: job_qualification_schedule
-- --------------------------------------------------------------------
INSERT INTO job_qualification_schedule
( schedule_id, qual_id, exam_year, exam_round, written_reg_start_date, written_reg_end_date, written_exam_start_date, written_exam_end_date,
    written_result_date, practical_reg_start_date, practical_reg_end_date, practical_exam_start_date, practical_exam_end_date, practical_result_date,
    last_synced_date, created_date, created_nm, del_yn)
VALUES
-- 정보처리기사 2026년 정기 기사 1회
(1, 1, 2026, '1회',
 '2026-01-12', '2026-01-15',
 '2026-01-30', '2026-03-03',
 '2026-03-11',
 '2026-03-23', '2026-03-26',
 '2026-04-18', '2026-05-06',
 '2026-06-12',
 NOW(), NOW(), 'jiwon', 'N'),

-- 정보처리기사 2026년 정기 기사 2회
(2, 1, 2026, '2회',
 '2026-04-20', '2026-04-23',
 '2026-05-09', '2026-05-29',
 '2026-06-10',
 '2026-06-22', '2026-06-25',
 '2026-07-18', '2026-08-05',
 '2026-09-11',
 NOW(), NOW(), 'jiwon', 'N'),

-- 정보처리기사 2026년 정기 기사 3회
(3, 1, 2026, '3회',
 '2026-08-10', '2026-08-13',
 '2026-09-07', '2026-10-01',
 '2026-10-09',
 '2026-10-20', '2026-10-23',
 '2026-11-21', '2026-12-10',
 '2027-01-15',
 NOW(), NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
-- [지원] 진로/추천 인강
-- 테이블: job_course
-- --------------------------------------------------------------------
INSERT INTO job_course
(course_id, course_type, provider_name, course_name, original_price, discount_price, military_price, benefit_detail, detail_url, created_date, created_nm, del_yn)
VALUES
    (1, 'C01', '인프런', '정보처리기사 실기', 88000, 88000, 88000, '정보처리기사 실기 대비', NULL, NOW(), 'jiwon', 'N'),
    (2, 'C01', '인프런', '정보처리기사 필기', 44000, 33000, 33000, '정보처리기사 필기 대비', NULL, NOW(), 'jiwon', 'N'),
    (3, 'C01', '에듀온', '정보처리산업기사 패키지', 220000, 131000, 131000, '정보처리산업기사 필기·실기', NULL, NOW(), 'jiwon', 'N'),
    (4, 'C01', 'YouTube', '이기적 프로그래밍기능사 필기', 0, 0, 0, '무료 필기 강의', NULL, NOW(), 'jiwon', 'N'),
    (5, 'C01', 'YouTube', '이기적 프로그래밍기능사 실기', 0, 0, 0, '무료 실기 강의', NULL, NOW(), 'jiwon', 'N'),
    (6, 'C01', 'YBM', 'ETS TOEIC 단기 공략 750+', 146000, 116800, 116800, 'TOEIC 750+ 대비', NULL, NOW(), 'jiwon', 'N'),
    (7, 'C01', 'YouTube', '제이크 TOEIC Speaking', 0, 0, 0, '무료 TOEIC Speaking 강의', NULL, NOW(), 'jiwon', 'N'),
    (8, 'C01', 'YouTube', '오픽노잼 OPIc IH·AL', 0, 0, 0, '무료 OPIc 강의', NULL, NOW(), 'jiwon', 'N'),
    (9, 'C02', '공단기', '27대비 9급 환급 프리패스', 1080000, 780000, 780000, '9급 행정직 환급 패스', NULL, NOW(), 'jiwon', 'N'),
    (10, 'C02', '넥스트공무원', '전 직렬 환급형 더블패스 2027', 730000, 580000, 580000, '9급 행정직 환급 패스', NULL, NOW(), 'jiwon', 'N'),
    (11, 'C02', '해커스공무원', '9급 행정직 전직렬 합불 0원 패스', 1099000, 299000, 299000, '9급 행정직 환급 패스', NULL, NOW(), 'jiwon', 'N'),
    (12, 'C02', '공단기', '27대비 9급 군무원 환급 직렬패스', 980000, 680000, 680000, '군무원 9급 환급 패스', NULL, NOW(), 'jiwon', 'N'),
    (13, 'C02', '해커스공무원', '27년 대비 9급 군무원 기적의 패스', 749000, 339000, 339000, '군무원 9급 종합 패스', NULL, NOW(), 'jiwon', 'N'),
    (14, 'C03', '김영편입', '2027+2028 김영패스 원더 [자연]', 1570000, 1470000, 1370000, '자연계 편입 종합 패스', NULL, NOW(), 'jiwon', 'N'),
    (15, 'C03', '해커스편입', '자연계 최대 400% 환급반', 1390000, 890000, 790000, '자연계 편입 환급반', NULL, NOW(), 'jiwon', 'N'),
    (16, 'C03', '에듀윌 편입', '에듀윌 편입 ALL PASS', 1090000, 790000, 690000, '편입 통합 패스', NULL, NOW(), 'jiwon', 'N'),
    (17, 'C03', '김영편입', '2027+2028 김영패스 원더 [인문]', 1370000, 1270000, 1170000, '인문계 편입 종합 패스', NULL, NOW(), 'jiwon', 'N'),
    (18, 'C03', '해커스편입', '인문계 최대 400% 환급반', 1290000, 790000, 690000, '인문계 편입 환급반', NULL, NOW(), 'jiwon', 'N');

-- --------------------------------------------------------------------
-- [지원] 진로/직무·직렬별 자격증·어학 매핑
-- 테이블: job_category_qualification
-- --------------------------------------------------------------------
INSERT INTO job_category_qualification (category_qual_id, category_id, qual_id, created_date, created_nm, del_yn)
VALUES
    (1, 32, 1, NOW(), 'jiwon', 'N'),
    (2, 32, 2, NOW(), 'jiwon', 'N'),
    (3, 32, 3, NOW(), 'jiwon', 'N'),
    (4, 32, 4, NOW(), 'jiwon', 'N'),
    (5, 32, 9, NOW(), 'jiwon', 'N'),
    (6, 32, 10, NOW(), 'jiwon', 'N'),
    (7, 32, 11, NOW(), 'jiwon', 'N'),
    (8, 33, 4, NOW(), 'jiwon', 'N'),
    (9, 33, 5, NOW(), 'jiwon', 'N'),
    (10, 33, 9, NOW(), 'jiwon', 'N'),
    (11, 33, 10, NOW(), 'jiwon', 'N'),
    (12, 33, 11, NOW(), 'jiwon', 'N'),
    (13, 72, 6, NOW(), 'jiwon', 'N'),
    (14, 72, 9, NOW(), 'jiwon', 'N'),
    (15, 72, 10, NOW(), 'jiwon', 'N'),
    (16, 72, 11, NOW(), 'jiwon', 'N'),
    (17, 17, 7, NOW(), 'jiwon', 'N'),
    (18, 17, 9, NOW(), 'jiwon', 'N'),
    (19, 17, 10, NOW(), 'jiwon', 'N'),
    (20, 17, 11, NOW(), 'jiwon', 'N'),
    (21, 23, 8, NOW(), 'jiwon', 'N'),
    (22, 23, 9, NOW(), 'jiwon', 'N'),
    (23, 23, 10, NOW(), 'jiwon', 'N'),
    (24, 23, 11, NOW(), 'jiwon', 'N'),
    (25, 101, 12, NOW(), 'jiwon', 'N'),
    (26, 109, 12, NOW(), 'jiwon', 'N');



-- --------------------------------------------------------------------
-- [지원] 진로/직무·직렬별 추천 인강 매핑
-- 테이블: job_category_course
-- --------------------------------------------------------------------
INSERT INTO job_category_course (category_course_id, category_id, course_id, created_date, created_nm, del_yn)
VALUES
    (1, 101, 9, NOW(), 'jiwon', 'N'),
    (2, 101, 10, NOW(), 'jiwon', 'N'),
    (3, 101, 11, NOW(), 'jiwon', 'N'),
    (4, 109, 12, NOW(), 'jiwon', 'N'),
    (5, 109, 13, NOW(), 'jiwon', 'N');

-- --------------------------------------------------------------------
-- [지원] 진로/자격증·어학별 추천 인강 매핑
-- 테이블: job_qualification_course
-- --------------------------------------------------------------------
INSERT INTO job_qualification_course (qual_course_id, qual_id, course_id, created_date, created_nm, del_yn)
VALUES
    (1, 1, 1, NOW(), 'jiwon', 'N'),
    (2, 1, 2, NOW(), 'jiwon', 'N'),
    (3, 2, 3, NOW(), 'jiwon', 'N'),
    (4, 3, 4, NOW(), 'jiwon', 'N'),
    (5, 3, 5, NOW(), 'jiwon', 'N'),
    (6, 9, 6, NOW(), 'jiwon', 'N'),
    (7, 10, 7, NOW(), 'jiwon', 'N'),
    (8, 11, 8, NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
-- [지원] 진로/편입 학과계열별 자격증·어학 매핑
-- 테이블: job_transfer_major_qualification
-- --------------------------------------------------------------------
INSERT INTO job_transfer_major_qualification
(major_qual_id, major_code, qual_id, created_date, created_nm, del_yn)
VALUES
    (1, 'M14', 8, NOW(), 'jiwon', 'N'),
    (2, 'M14', 9, NOW(), 'jiwon', 'N'),
    (3, 'M04', 6, NOW(), 'jiwon', 'N'),
    (4, 'M04', 9, NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
-- [지원] 진로/편입 학과계열별 인강 매핑
-- 테이블: job_transfer_major_course
-- --------------------------------------------------------------------
INSERT INTO job_transfer_major_course
(major_course_id, major_code, course_id, created_date, created_nm, del_yn)
VALUES
-- M04 : 전기·전자·반도체·통신 (자연)
(1, 'M04', 14, NOW(), 'jiwon', 'N'),
(2, 'M04', 15, NOW(), 'jiwon', 'N'),
(3, 'M04', 16, NOW(), 'jiwon', 'N'),

-- M14 : 미디어·광고·콘텐츠 (인문)
(4, 'M14', 17, NOW(), 'jiwon', 'N'),
(5, 'M14', 18, NOW(), 'jiwon', 'N'),
(6, 'M14', 16, NOW(), 'jiwon', 'N');


-- --------------------------------------------------------------------
-- [지원] 진로/추천 정책·KB서비스
-- 테이블: job_recommend_service
-- --------------------------------------------------------------------
INSERT INTO job_recommend_service
(service_id, service_type, goal_type, service_name, service_qual, service_desc,
 service_url, external_code, last_synced_date, sort_order,
 created_date, created_nm, del_yn)
VALUES
    (1, 'P01', 'J01', '국민취업지원제도', '전역 후 신청 가능',
     '취업을 희망하는 사람에게 취업지원서비스와 구직촉진수당 등을 지원하는 제도입니다.',
     NULL, NULL, NULL, 1,
     NOW(), 'jiwon', 'N'),

    (2, 'P01', 'J01', '국민내일배움카드', '전역 후 신청 가능',
     '직업훈련 비용을 지원하여 취업 역량을 높일 수 있도록 지원하는 제도입니다.',
     NULL, NULL, NULL, 2,
     NOW(), 'jiwon', 'N'),

    (3, 'P01', 'J03', '국가장학금', '편입 후 신청 가능',
     '편입 후 대학 재학생이 등록금 부담을 줄일 수 있도록 지원하는 국가장학금 제도입니다.',
     NULL, NULL, NULL, 1,
     NOW(), 'jiwon', 'N'),

    (4, 'P02', NULL, 'KB Pay', '복무 중 이용 가능',
     '결제, 송금, 자산관리 등 다양한 금융 서비스를 이용할 수 있는 KB금융 통합 플랫폼입니다.',
     NULL, NULL, NULL, 1,
     NOW(), 'jiwon', 'N');


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


-- ######################################################################
--  Ⅲ. 회원
-- ######################################################################

-- --------------------------------------------------------------------
--  [호빈] 회원 정보
--  테이블: user
-- --------------------------------------------------------------------
INSERT INTO `user` (`id`, `user_id`, `password`, `name`, `phone`, `type_id`, `rank_id`, `unit_name`, `unit_code`, `enlist_date`, `discharge_date`, `login_provider`, `status`, `withdrawn_at`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 'hobin@kbthink.com', '$2a$10$HbpaEJl9AV82dWtFjtftUOBEkwHjoXScs2bDTbpP7QgKitPYHyKM.', '김호빈', '010-1234-0001', 1, 3, '수도방위사령부 제1경비단', 'A01-102', '2026-01-05', '2027-07-04', 'local', 'ACTIVE', NULL, NOW(), 'hobin', NULL, NULL, 'N'),
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
(`account_id`, `user_id`, `bank_code`, `monthly_save`, `monthly_count`,
 `curr_amount`, `account_status`, `open_date`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
-- 회원 1 (ACTIVE): 총 55만원 (30 + 25) / 5개월 납입 중 (26년 1월 입대, 26년 3월 개설 => 납입가능 회차는 17회차)
(1, 1, '004', 300000, 5, 1500000, 'ACTIVE',
    '2026-03-09', '2026-03-9 10:00:00', 'seokyun', NULL, NULL, 'N'),
(2, 1, '003', 250000, 5, 1250000, 'ACTIVE',
    '2026-03-09', '2026-03-9 10:05:00', 'seokyun', NULL, NULL, 'N'),

-- 회원 2 (ACTIVE): 계좌 3은 일정하게 납입, 계좌 4는 중간에 증액 (25년 11월 입대, 25년 11월 개설 => 납입가능 회차 18회차)
(3, 2, '004', 300000, 8, 2400000, 'ACTIVE',
    '2025-11-15', '2025-11-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(4, 2, '088', 250000, 8, 950000, 'ACTIVE',
    '2025-11-15', '2025-11-15 14:35:00', 'seokyun', NULL, NULL, 'N'),

-- 회원 3 (TERMINATED): 총 45만원 (30 + 15) / 3개월 후 중도 해지 (26년 2월 입대, 26년 2월 개설 => 납입가능 회차 18회차, 하지만 4월분 납입후 해지)
(5, 3, '081', 300000, 2, 900000, 'TERMINATED',
    '2026-02-10', '2026-02-10 09:00:00', 'seokyun', NULL, NULL, 'Y'),
(6, 3, '004', 150000, 2, 450000, 'TERMINATED',
    '2026-02-10', '2026-02-10 09:05:00', 'seokyun', NULL, NULL, 'Y');


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
-- 사용내역 vacation_name은 "정기휴가 사용"으로 통일 (표시용 "N차"는 조회 시점에 동적 계산)
(4, 2, 'REGULAR', '정기휴가', '2025-01-01', 24, FALSE, NOW(), 'seokyun', NULL, NULL, 'N'),     -- 총 부여 휴가
(5, 2, 'REGULAR', '정기휴가 사용', '2025-05-01', 4, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),     -- 4일 사용
(6, 2, 'REGULAR', '정기휴가 사용', '2025-07-01', 5, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),     -- 5일 사용
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
--  테이블: school, rent_goal, rent_goal_region, rent_recommend, loan_recommend
-- --------------------------------------------------------------------
--  학교 마스터 (Mock: 대학알리미 API 전환 전 임시 3건)
INSERT INTO school (school_id, school_name, school_type, address, sigungu_code, region_code, latitude, longitude, created_date, created_nm, del_yn) VALUES
(1, '부산대학교', 'UNIVERSITY', '부산광역시 금정구 부산대학로63번길 2', '26410', '2641010100', 35.2339000, 129.0806000, NOW(), 'suyeon', 'N'),
(2, '부경대학교', 'UNIVERSITY', '부산광역시 남구 용소로 45',        '26290', '2629010100', 35.1336000, 129.1058000, NOW(), 'suyeon', 'N'),
(3, '서울대학교', 'UNIVERSITY', '서울특별시 관악구 관악로 1',       '11620', '1162010100', 37.4599000, 126.9520000, NOW(), 'suyeon', 'N');

--  월세 목표
INSERT INTO rent_goal (goal_id, user_id, title, selection_mode, school_id, commute_radius_km, monthly_budget, residence_preset, residence_months, status, created_date, created_nm, del_yn) VALUES
(1, 1, '전역 후 부산 자취', 'REGION', NULL, NULL, 700000,  'YEAR', 12, 'CONFIRMED', NOW(), 'suyeon', 'N'),
(2, 2, '전역 후 서울 자취', 'REGION', NULL, NULL, 1000000, 'YEAR', 12, 'DRAFT',     NOW(), 'suyeon', 'N');

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
-- [지원] 진로/진로 목표
-- 테이블: job_goal
-- --------------------------------------------------------------------
INSERT INTO job_goal
(goal_id, user_id, goal_type, category_id, univ_id, major_id, expected_date, status, created_date, created_nm, modified_date, modified_nm, del_yn)
VALUES
    (1, 1, 'J01', 32, NULL, NULL, '2027-03', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N'),
    (2, 1, 'J01', 33, NULL, NULL, '2027-06', 'DRAFT', NOW(), 'jiwon', NULL, NULL, 'N'),
    (3, 1, 'J02', 101, NULL, NULL, '2027-09', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N'),
    (4, 1, 'J02', 109, NULL, NULL, '2027-12', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N'),
    (5, 1, 'J03', NULL, 1, 14, '2027-03', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N'),
    (6, 1, 'J03', NULL, 1, 4, '2027-03', 'DRAFT', NOW(), 'jiwon', NULL, NULL, 'N');



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
(`history_id`, `account_id`, `pay_round`, `pay_amount`, `paid_date`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES

-- 회원 1 (ACTIVE): 총 55만원 (30 + 25) / 5개월 납입 중 (26년 1월 입대, 26년 3월 개설 => 납입가능 회차는 17회차)
-- [회원 1] account_id = 1 (ACTIVE: 5회차 / 30만원 고정)
(1, 1, 1, 300000, '2026-03-10', '2026-03-10 10:00:00', 'seokyun', NULL, NULL, 'N'),
(2, 1, 2, 300000, '2026-04-10', '2026-04-10 10:00:00', 'seokyun', NULL, NULL, 'N'),
(3, 1, 3, 300000, '2026-05-10', '2026-05-10 10:00:00', 'seokyun', NULL, NULL, 'N'),
(4, 1, 4, 300000, '2026-06-10', '2026-06-10 10:00:00', 'seokyun', NULL, NULL, 'N'),
(5, 1, 5, 300000, '2026-07-10', '2026-07-10 10:00:00', 'seokyun', NULL, NULL, 'N'),

-- [회원 1] account_id = 2 (ACTIVE: 5회차 / 25만원 고정)
(6, 2, 1, 250000, '2026-03-10', '2026-03-10 10:05:00', 'seokyun', NULL, NULL, 'N'),
(7, 2, 2, 250000, '2026-04-10', '2026-04-10 10:05:00', 'seokyun', NULL, NULL, 'N'),
(8, 2, 3, 250000, '2026-05-10', '2026-05-10 10:05:00', 'seokyun', NULL, NULL, 'N'),
(9, 2, 4, 250000, '2026-06-10', '2026-06-10 10:05:00', 'seokyun', NULL, NULL, 'N'),
(10, 2, 5, 250000, '2026-07-10', '2026-07-10 10:05:00', 'seokyun', NULL, NULL, 'N'),

-- 회원 2 (ACTIVE): 계좌 3은 일정하게 납입, 계좌 4는 중간에 증액 (25년 11월 입대, 25년 11월 개설 => 납입가능 회차 18회차)
-- [회원 2] account_id = 3 (ACTIVE: 18회차 / 30만원 고정)
(11, 3, 1, 300000, '2025-12-15', '2025-12-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(12, 3, 2, 300000, '2026-01-15', '2026-01-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(13, 3, 3, 300000, '2026-02-15', '2026-02-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(14, 3, 4, 300000, '2026-03-15', '2026-03-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(15, 3, 5, 300000, '2026-04-15', '2026-04-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(16, 3, 6, 300000, '2026-05-15', '2026-05-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(17, 3, 7, 300000, '2026-06-15', '2026-06-15 14:30:00', 'seokyun', NULL, NULL, 'N'),
(18, 3, 8, 300000, '2026-07-15', '2026-07-15 14:30:00', 'seokyun', NULL, NULL, 'N'),

-- [회원 2] account_id = 4 (ACTIVE: 납입 금액 변동 시나리오 / 1~7회차: 10만원, 8~회차: 25만원)
(19, 4, 1, 100000, '2025-12-15', '2025-12-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(20, 4, 2, 100000, '2026-01-15', '2026-01-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(21, 4, 3, 100000, '2026-02-15', '2026-02-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(22, 4, 4, 100000, '2026-03-15', '2026-03-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(23, 4, 5, 100000, '2026-04-15', '2026-04-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(24, 4, 6, 100000, '2026-05-15', '2026-05-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(25, 4, 7, 100000, '2026-06-15', '2026-06-15 14:35:00', 'seokyun', NULL, NULL, 'N'),
(26, 4, 8, 250000, '2026-07-15', '2026-07-15 14:35:00', 'seokyun', NULL, NULL, 'N'),

-- 회원 3 (TERMINATED): 총 45만원 (30 + 15) / 3개월 후 중도 해지 (26년 2월 입대, 26년 2월 개설 => 납입가능 회차 18회차)
-- [회원 3] account_id = 5 (TERMINATED: 2회차 / 30만원 고정)
(27, 5, 1, 300000, '2026-03-20', '2026-03-20 09:00:00', 'seokyun', NULL, NULL, 'Y'),
(28, 5, 2, 300000, '2026-04-20', '2026-04-20 09:00:00', 'seokyun', NULL, NULL, 'Y'),

-- [회원 3] account_id = 6 (TERMINATED: 2회차 / 15만원 고정)
(29, 6, 1, 150000, '2026-03-20', '2026-03-20 09:05:00', 'seokyun', NULL, NULL, 'Y'),
(30, 6, 2, 150000, '2026-04-20', '2026-04-20 09:05:00', 'seokyun', NULL, NULL, 'Y');


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
-- [지원] 진로/목표별 선택 자격증·어학
-- 테이블: job_goal_qualification
-- --------------------------------------------------------------------
INSERT INTO job_goal_qualification (goal_qual_id, goal_id, qual_id, selected_cost, created_date, created_nm, modified_date, modified_nm, del_yn)
VALUES
    (1, 1, 1, 42000, NOW(), 'jiwon', NULL, NULL, 'N'),
    (2, 1, 9, 26200, NOW(), 'jiwon', NULL, NULL, 'N'),
    (3, 3, 12, 27000, NOW(), 'jiwon', NULL, NULL, 'N'),
    (4, 5, 8, 34000, NOW(), 'jiwon', NULL, NULL, 'N'),
    (5, 5, 9, 26200, NOW(), 'jiwon', NULL, NULL, 'N');


-- --------------------------------------------------------------------
-- [지원] 진로/목표별 선택 인강
-- 테이블: job_goal_course
-- --------------------------------------------------------------------
INSERT INTO job_goal_course (goal_course_id, goal_id, course_id, selected_cost, created_date, created_nm, modified_date, modified_nm, del_yn)
VALUES
    (1, 1, 1, 88000, NOW(), 'jiwon', NULL, NULL, 'N'),
    (2, 1, 6, 116800, NOW(), 'jiwon', NULL, NULL, 'N'),
    (3, 3, 9, 780000, NOW(), 'jiwon', NULL, NULL, 'N'),
    (4, 5, 17, 1170000, NOW(), 'jiwon', NULL, NULL, 'N');


SET FOREIGN_KEY_CHECKS = 1;