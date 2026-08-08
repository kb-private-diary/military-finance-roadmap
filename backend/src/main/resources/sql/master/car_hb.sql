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

