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
INSERT INTO `military_rank`
    (`rank_id`, `rank_name`, `rank_salary`, `service_months`, `image_url`,
     `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, '이병', 750000, 1, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(2, '일병', 900000, 3, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(3, '상병', 1200000, 9, NULL, NOW(), 'seokyun', NULL, NULL, 'N'),
(4, '병장', 1500000, 15, NULL, NOW(), 'seokyun', NULL, NULL, 'N');


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


