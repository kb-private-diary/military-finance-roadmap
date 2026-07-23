SET FOREIGN_KEY_CHECKS = 0;

/*
총 테이블 갯수: 52개

[테이블 구분]
- 회원/공통: user, military_types, military_rank, badge, user_badge, terms, terms_agreement, vacation
- 예적금/금융: bank_category, saving_product, card_product, saving_account, saving_history, policy_product
- 목표/로드맵 공통: roadmap_category, user_bookmark
- 여행 목표: travel_goal, travel_cost, city_cost, travel_package, travel_insurance
- 진로 목표: job_goal, job_code, prep_item_criteria, job_plan, service_criteria, service_selection
- 자동차 목표: car_goal, car_model, car_type, car_insurance, car_ev, car_tax_prepay, car_tax
- 자취/부동산 목표: rent_goal, rent_goal_region, region_code, rent_listing, region_fee_stat, rent_recommend, housing_loan, loan_recommend
- 마이데이터/소비: openbanking_link, spending, spending_review, merchant_category, saving_challenge
- 챗봇/알림: chat_session, chat_message, chat_feedback, kakao_token, notification
*/

DROP TABLE IF EXISTS `military_types`;
CREATE TABLE `military_types` (
  `type_id` INT PRIMARY KEY NOT NULL COMMENT '군종ID',
  `type_name` VARCHAR(20) NOT NULL COMMENT '군종명',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `car_type`;
CREATE TABLE `car_type` (
  `code` INT PRIMARY KEY NOT NULL COMMENT '차종코드',
  `name` VARCHAR(20) NOT NULL COMMENT '차종이름',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `badge`;
CREATE TABLE `badge` (
  `badge_id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '뱃지 ID',
  `badge_name` VARCHAR(20) NOT NULL COMMENT '뱃지명',
  `image_url` VARCHAR(500) COMMENT '뱃지 이미지',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `military_rank`;
CREATE TABLE `military_rank` (
  `rank_id` INT PRIMARY KEY NOT NULL COMMENT '계급ID (1: 이병, 2: 일병, 3: 상병, 4: 병장)',
  `rank_name` VARCHAR(30) NOT NULL COMMENT '계급 이름',
  `rank_salary` BIGINT NOT NULL COMMENT '계급 월급',
  `image_url` VARCHAR(500) COMMENT '계급 이미지',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시 (삭제 시 삭제일시로 기록)',
  `modified_nm` VARCHAR(50) COMMENT '수정자 (삭제 시 삭제자로 기록)',
  `del_yn` CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제여부: IN (''Y'',''N'') / 물리 삭제 없이 ''Y''로 UPDATE, 삭제자·삭제일시는 수정자·수정일시에 기록'
);

DROP TABLE IF EXISTS `user_badge`;
CREATE TABLE `user_badge` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '매핑 고유번호',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호 (TBL_USER.id 참조)',
  `badge_id` INT NOT NULL COMMENT '뱃지고유번호 (TBL_BADGE.badge_id 참조)',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시 (삭제 시 삭제일시로 기록)',
  `modified_nm` VARCHAR(50) COMMENT '수정자 (삭제 시 삭제자로 기록)',
  `del_yn` CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제여부: IN (''Y'',''N'') / 물리 삭제 없이 ''Y''로 UPDATE, 삭제자·삭제일시는 수정자·수정일시에 기록'
);

DROP TABLE IF EXISTS `bank_category`;
CREATE TABLE `bank_category` (
  `bank_code` CHAR(3) PRIMARY KEY NOT NULL COMMENT '은행코드',
  `bank_name` VARCHAR(10) NOT NULL COMMENT '은행명',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `saving_product`;
CREATE TABLE `saving_product` (
  `saving_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '상품ID',
  `product_type` VARCHAR(20) NOT NULL COMMENT '상품유형',
  `is_active` BOOLEAN NOT NULL COMMENT '판매여부',
  `fin_prdt_cd` VARCHAR(50) NOT NULL COMMENT '금감원 상품코드',
  `kor_co_nm` VARCHAR(30) NOT NULL COMMENT '금융회사명',
  `product_name` VARCHAR(100) NOT NULL COMMENT '상품명',
  `join_member` TEXT NOT NULL COMMENT '가입대상',
  `min_limit` BIGINT COMMENT '최소납입한도',
  `max_limit` BIGINT COMMENT '최대납입한도',
  `etc_note` TEXT COMMENT '기타유의사항',
  `intr_rate_type` CHAR(1) NOT NULL COMMENT '이자계산방식',
  `rsrv_type` VARCHAR(10) NOT NULL COMMENT '적립방식',
  `save_trm` INT NOT NULL COMMENT '저축기간',
  `spcl_cnd` TEXT COMMENT '우대조건설명',
  `basic_rate` DECIMAL(4,2) NOT NULL COMMENT '기본금리',
  `max_rate` DECIMAL(4,2) NOT NULL COMMENT '최고우대금리',
  `is_tax_exempt` BOOLEAN NOT NULL COMMENT '과세여부',
  `gov_match_rate` DECIMAL(5,2) NOT NULL COMMENT '정부매칭 기여금 비율',
  `product_link` VARCHAR(500) COMMENT '상품상세링크 URL',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `card_product`;
CREATE TABLE `card_product` (
  `card_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '상품번호',
  `category` INT COMMENT '로드맵카테고리',
  `card_name` VARCHAR(100) NOT NULL COMMENT '상품명',
  `card_type` VARCHAR(20) COMMENT '상품유형',
  `card_qual` VARCHAR(50) COMMENT '자격조건라벨',
  `card_desc` VARCHAR(300) COMMENT '혜택요약',
  `card_url` VARCHAR(500) COMMENT '자세히 보기 URL',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `roadmap_category`;
CREATE TABLE `roadmap_category` (
  `category_id` INT PRIMARY KEY NOT NULL COMMENT '카테고리ID',
  `category_name` VARCHAR(20) NOT NULL COMMENT '카테고리명',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `user_bookmark`;
CREATE TABLE `user_bookmark` (
  `bookmark_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '관심항목ID',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `category_id` INT NOT NULL COMMENT '로드맵카테고리ID',
  `goal_id` BIGINT NOT NULL COMMENT '목표ID',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `terms`;
CREATE TABLE `terms` (
  `terms_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '약관ID',
  `name` VARCHAR(100) NOT NULL COMMENT '약관명',
  `required` BOOLEAN NOT NULL COMMENT '필수/선택 여부',
  `content` TEXT NOT NULL COMMENT '약관 내용',
  `version` VARCHAR(10) NOT NULL COMMENT '약관 버젼',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `terms_agreement`;
CREATE TABLE `terms_agreement` (
  `agreement_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '약관동의이력ID',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `terms_id` BIGINT NOT NULL COMMENT '약관ID',
  `agreed` BOOLEAN NOT NULL COMMENT '동의여부',
  `agreed_date` DATETIME NOT NULL COMMENT '동의 날짜',
  `terms_version` VARCHAR(10) COMMENT '동의당시약관버전',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '회원 고유번호',
  `user_id` VARCHAR(50) UNIQUE NOT NULL COMMENT '회원아이디',
  `password` VARCHAR(200) COMMENT '비밀번호',
  `name` VARCHAR(50) COMMENT '이름',
  `phone` VARCHAR(20) COMMENT '전화번호',
  `type_id` INT COMMENT '군종ID',
  `rank_id` INT COMMENT '계급',
  `unit_name` VARCHAR(50) COMMENT '부대정보',
  `unit_code` VARCHAR(20) COMMENT '부대코드',
  `enlist_date` DATE COMMENT '입대일',
  `discharge_date` DATE COMMENT '전역예정일',
  `login_provider` VARCHAR(20) COMMENT '로그인방식',
  `status` VARCHAR(20) COMMENT '계정상태',
  `withdrawn_at` DATETIME COMMENT '탈퇴일시',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `saving_account`;
CREATE TABLE `saving_account` (
  `account_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '계좌ID',
  `user_id` BIGINT NOT NULL COMMENT '회원ID',
  `bank_code` CHAR(3) NOT NULL COMMENT '은행ID',
  `monthly_save` BIGINT NOT NULL COMMENT '현재 한달 납입금',
  `monthly_count` INT NOT NULL COMMENT '납입개월수',
  `curr_amount` BIGINT NOT NULL COMMENT '누적납입금',
  `account_status` VARCHAR(20) NOT NULL COMMENT '적금 상태',
  `created_date` DATETIME NOT NULL COMMENT '생성날짜',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정날짜',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `saving_history`;
CREATE TABLE `saving_history` (
  `history_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '히스토리ID',
  `account_id` BIGINT NOT NULL COMMENT '계좌ID',
  `pay_round` INT NOT NULL COMMENT '납입회차',
  `pay_amount` BIGINT NOT NULL COMMENT '해당회차 납입금',
  `created_date` DATETIME NOT NULL COMMENT '생성날짜',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정날짜',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `vacation`;
CREATE TABLE `vacation` (
  `vacation_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '휴가ID',
  `user_id` BIGINT NOT NULL COMMENT '회원ID',
  `vacation_cate` VARCHAR(20) NOT NULL COMMENT '휴가 카테고리',
  `vacation_name` VARCHAR(100) NOT NULL COMMENT '휴가 이름',
  `vacation_get` DATE NOT NULL COMMENT '휴가 획득날짜',
  `vacation_day` INT NOT NULL COMMENT '휴가일수',
  `vacation_state` BOOLEAN NOT NULL COMMENT '사용여부',
  `created_date` DATETIME NOT NULL COMMENT '생성날짜',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정날짜',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `policy_product`;
CREATE TABLE `policy_product` (
  `policy_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '정책상품ID',
  `policy_name` VARCHAR(50) NOT NULL COMMENT '정책 상품명',
  `policy_status` BOOLEAN NOT NULL COMMENT '정책판매상태',
  `benefits` TEXT NOT NULL COMMENT '주요 혜택',
  `join_member` TEXT NOT NULL COMMENT '가입 대상',
  `min_limit` BIGINT COMMENT '월 최소 납입금액',
  `max_limit` BIGINT COMMENT '월 최대 납입금액',
  `save_trm_note` VARCHAR(100) COMMENT '가입기간 표기 텍스트',
  `policy_link` VARCHAR(500) COMMENT '자세히 보기 URL',
  `has_calculator` BOOLEAN NOT NULL COMMENT '이익 계산기 노출 여부',
  `calc_period_months` INT COMMENT '계산기 기준 개월 수',
  `max_rate` DECIMAL(5,2) COMMENT '최고 우대금리 (%)',
  `normal_match_rate` DECIMAL(5,2) COMMENT '일반형 매칭비율 (%)',
  `prefer_match_rate` DECIMAL(5,2) COMMENT '우대형 매칭비율 (%)',
  `created_date` DATETIME NOT NULL COMMENT '생성날짜',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정날짜',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `travel_goal`;
CREATE TABLE `travel_goal` (
  `goal_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '목표 ID',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `title` VARCHAR(100) NOT NULL COMMENT '플랜명',
  `departure` VARCHAR(50) NOT NULL COMMENT '출발지',
  `destination` VARCHAR(50) NOT NULL COMMENT '도착지',
  `is_domestic` BOOLEAN COMMENT '국내외구분',
  `style` VARCHAR(20) COMMENT '여행스타일',
  `start_date` DATE COMMENT '출발일',
  `end_date` DATE COMMENT '도착일',
  `total_budget` BIGINT COMMENT '총예산',
  `places` JSON COMMENT '관심 여행지 목록',
  `benefits` JSON COMMENT '선택 혜택 목록',
  `package_id` BIGINT COMMENT '상품 ID',
  `status` VARCHAR(20) COMMENT '진행상태',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `travel_cost`;
CREATE TABLE `travel_cost` (
  `cost_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '결과 ID',
  `goal_id` BIGINT NOT NULL COMMENT '목표 ID',
  `flight_cost` BIGINT COMMENT '항공/교통비',
  `hotel_cost` BIGINT COMMENT '숙박비',
  `living_cost` BIGINT COMMENT '물가(식비+현지 경비)',
  `total_cost` BIGINT COMMENT '총 예상 경비',
  `remaining_budget` BIGINT COMMENT '잔여예산/부족한 예산',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `city_cost`;
CREATE TABLE `city_cost` (
  `city_cost_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '물가 ID',
  `country` VARCHAR(50) COMMENT '나라명',
  `city` VARCHAR(50) COMMENT '도시명',
  `saving_cost` BIGINT COMMENT '절약형 물가',
  `common_cost` BIGINT COMMENT '일반형 물가',
  `premium_cost` BIGINT COMMENT '프리미엄 물가',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `travel_package`;
CREATE TABLE `travel_package` (
  `package_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '상품 ID',
  `goods_code` VARCHAR(20) COMMENT '상품코드',
  `country` VARCHAR(50) COMMENT '국가',
  `region_name` VARCHAR(100) COMMENT '지역명',
  `name` VARCHAR(300) COMMENT '상품명',
  `image_url` VARCHAR(300) COMMENT '여행지 이미지',
  `description` VARCHAR(500) COMMENT '간단 설명',
  `min_price` BIGINT COMMENT '최저가',
  `departure_period` VARCHAR(100) COMMENT '출발기간',
  `crawled_at` DATETIME NOT NULL COMMENT '수집일시',
  `is_active` BOOLEAN NOT NULL COMMENT '유효여부',
  `detail_url` VARCHAR(300) COMMENT '상세URL',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `travel_insurance`;
CREATE TABLE `travel_insurance` (
  `insurance_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '보험 ID',
  `title` VARCHAR(50) NOT NULL COMMENT '보험 명',
  `insurance_inf` VARCHAR(500) COMMENT '보험 정보',
  `insurance_period` INT NOT NULL COMMENT '보험 기간',
  `insurance_url` VARCHAR(500) COMMENT '자세히 보기 URL',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `job_goal`;
CREATE TABLE `job_goal` (
  `goal_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '목표ID',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `goal_type` CHAR(3) NOT NULL COMMENT '목표유형',
  `job_code_id` BIGINT NOT NULL COMMENT '희망직무직렬학과ID',
  `expected_date` VARCHAR(7) NOT NULL COMMENT '목표예상시기',
  `status` VARCHAR(20) COMMENT '진행상태',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `job_code`;
CREATE TABLE `job_code` (
  `job_code_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '희망직무직렬학과ID',
  `goal_type` CHAR(3) NOT NULL COMMENT '목표유형',
  `code_name` VARCHAR(100) NOT NULL COMMENT '코드명',
  `info_url` VARCHAR(500) COMMENT '상세정보URL',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `job_interested_type`;
CREATE TABLE `job_interested_type` (
    `interested_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '관심항목ID',
    `goal_id` BIGINT NOT NULL COMMENT '목표ID',
    `item_type` CHAR(3) NOT NULL COMMENT '항목구분',
    `created_date` DATETIME NOT NULL COMMENT '생성일시',
    `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
    `modified_date` DATETIME COMMENT '수정일시',
    `modified_nm` VARCHAR(50) COMMENT '수정자',
    `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `prep_item_criteria`;
CREATE TABLE `prep_item_criteria` (
  `prep_crit_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '기준항목ID',
  `goal_type` CHAR(3) NOT NULL COMMENT '목표유형',
  `item_type` CHAR(3) NOT NULL COMMENT '항목구분',
  `job_code_id` BIGINT NOT NULL COMMENT '희망직무직렬학과ID',
  `item_name` VARCHAR(200) NOT NULL COMMENT '항목명',
  `info_url` VARCHAR(500) COMMENT '상세정보URL',
  `apply_url` VARCHAR(500) COMMENT '접수URL',
  `amount` BIGINT COMMENT '금액',
  `amount_source` CHAR(3) NOT NULL COMMENT '금액출처',
  `fee_detail` VARCHAR(200) COMMENT '비용 상세 내역',
  `external_code` VARCHAR(50) COMMENT '외부 API 코드',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `job_plan`;
CREATE TABLE `job_plan` (
  `plan_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '준비항목ID',
  `goal_id` BIGINT NOT NULL COMMENT '목표ID',
  `item_type` CHAR(3) NOT NULL COMMENT '항목구분',
  `item_name` VARCHAR(200) NOT NULL COMMENT '항목명',
  `info_url` VARCHAR(500) COMMENT '상세정보URL',
  `apply_url` VARCHAR(500) COMMENT '접수URL',
  `amount` BIGINT COMMENT '금액',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `service_criteria`;
CREATE TABLE `service_criteria` (
  `svc_crit_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '서비스ID',
  `goal_type` CHAR(3) NOT NULL COMMENT '목표유형',
  `service_type` CHAR(3) NOT NULL COMMENT '서비스구분',
  `service_name` VARCHAR(200) NOT NULL COMMENT '서비스명',
  `service_desc` VARCHAR(500) COMMENT '서비스설명',
  `use_time` CHAR(3) NOT NULL COMMENT '이용시점',
  `info_url` VARCHAR(500) COMMENT '상세정보URL',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `service_selection`;
CREATE TABLE `service_selection` (
  `selection_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '선택서비스ID',
  `goal_id` BIGINT NOT NULL COMMENT '목표ID',
  `svc_crit_id` BIGINT COMMENT '서비스ID',
  `card_id` BIGINT COMMENT '카드상품ID',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `car_goal`;
CREATE TABLE `car_goal` (
  `goal_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '목표ID',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `budget` BIGINT COMMENT '예산',
  `car_type_code` INT COMMENT '차종선호',
  `is_new` BOOLEAN COMMENT '신차중고구분',
  `target_date` DATE COMMENT '목표구매시기',
  `region` VARCHAR(20) COMMENT '거주지역',
  `selected_model_id` BIGINT COMMENT '선택차량ID',
  `selected_year` INT COMMENT '선택연식',
  `status` VARCHAR(20) COMMENT '진행상태',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `car_model`;
CREATE TABLE `car_model` (
  `model_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '모델ID',
  `manufacturer` VARCHAR(30) COMMENT '제조사',
  `model_name` VARCHAR(50) COMMENT '모델명',
  `car_type_code` INT COMMENT '차종코드',
  `fuel_type` VARCHAR(20) COMMENT '연료구분',
  `base_price` BIGINT COMMENT '기본트림가격',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `car_insurance`;
CREATE TABLE `car_insurance` (
  `insurance_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '기준ID',
  `car_type_code` INT COMMENT '차종코드',
  `experience_bracket` VARCHAR(20) COMMENT '운전경력구간',
  `estimated_premium_min` BIGINT COMMENT '예상보험료최소',
  `estimated_premium_max` BIGINT COMMENT '예상보험료최대',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `car_ev`;
CREATE TABLE `car_ev` (
  `subsidy_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '기준ID',
  `region` VARCHAR(20) NOT NULL COMMENT '거주지역',
  `national_subsidy` BIGINT COMMENT '국비보조금',
  `local_subsidy` BIGINT COMMENT '지방비보조금',
  `base_year` INT NOT NULL COMMENT '기준연도',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `car_tax_prepay`;
CREATE TABLE `car_tax_prepay` (
  `prepay_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '기준ID',
  `year` INT COMMENT '적용연도',
  `prepay_month` VARCHAR(10) COMMENT '연납신청월',
  `discount_rate` DECIMAL(4,2) COMMENT '할인율',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `car_tax`;
CREATE TABLE `car_tax` (
  `tax_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '기준ID',
  `car_type_code` INT COMMENT '차종코드',
  `acquisition_tax_rate` DECIMAL(4,2) COMMENT '취득세율',
  `bond_exempt_engine_cc` INT COMMENT '공채매입면제배기량기준',
  `region` VARCHAR(20) COMMENT '적용지역',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `rent_goal`;
CREATE TABLE `rent_goal` (
  `goal_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '목표번호',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `title` VARCHAR(50) COMMENT '목표이름',
  `trade_type` VARCHAR(10) NOT NULL COMMENT '거래유형',
  `estate_type` VARCHAR(15) COMMENT '매물종류',
  `max_deposit` BIGINT NOT NULL COMMENT '보증금한도',
  `max_monthly` BIGINT NOT NULL COMMENT '월세한도',
  `room_count` VARCHAR(10) COMMENT '방개수조건',
  `expected_fee` BIGINT COMMENT '예상관리비',
  `residence_term` VARCHAR(5) NOT NULL COMMENT '거주기간',
  `current_asset` BIGINT COMMENT '현재자산',
  `target_date` DATE COMMENT '입주목표시기',
  `status` VARCHAR(12) NOT NULL COMMENT '진행상태',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `rent_goal_region`;
CREATE TABLE `rent_goal_region` (
  `region_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '지역번호',
  `goal_id` BIGINT NOT NULL COMMENT '목표번호',
  `region_code` VARCHAR(10) NOT NULL COMMENT '법정동코드',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `region_code`;
CREATE TABLE `region_code` (
  `region_code` VARCHAR(10) PRIMARY KEY NOT NULL COMMENT '법정동코드',
  `sido_name` VARCHAR(20) NOT NULL COMMENT '시도명',
  `sigungu_name` VARCHAR(30) COMMENT '시군구명',
  `umd_name` VARCHAR(30) COMMENT '읍면동명',
  `sigungu_code` VARCHAR(5) NOT NULL COMMENT '시군구코드',
  `is_abolished` CHAR(1) NOT NULL COMMENT '폐지여부',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `rent_listing`;
CREATE TABLE `rent_listing` (
  `listing_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '매물번호',
  `estate_type` VARCHAR(15) NOT NULL COMMENT '매물종류',
  `sigungu_code` VARCHAR(5) NOT NULL COMMENT '시군구코드',
  `region_code` VARCHAR(10) COMMENT '법정동코드',
  `umd_name` VARCHAR(30) COMMENT '법정동명',
  `jibun` VARCHAR(20) COMMENT '지번',
  `building_name` VARCHAR(100) COMMENT '건물명',
  `built_year` INT COMMENT '건축년도',
  `floor` INT COMMENT '층',
  `area_sqm` DECIMAL(6,2) COMMENT '전용면적',
  `deposit` BIGINT COMMENT '보증금',
  `monthly_rent` BIGINT NOT NULL COMMENT '월세',
  `deal_date` DATE COMMENT '계약일',
  `latitude` DECIMAL(10,7) COMMENT '위도',
  `longitude` DECIMAL(10,7) COMMENT '경도',
  `base_date` DATE COMMENT '기준일자',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `region_fee_stat`;
CREATE TABLE `region_fee_stat` (
  `stat_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '통계번호',
  `region_code` VARCHAR(10) NOT NULL COMMENT '법정동코드',
  `base_month` VARCHAR(7) NOT NULL COMMENT '기준년월',
  `mgmt_fee_per_sqm` BIGINT COMMENT '면적당공용관리비',
  `elec_fee_per_sqm` BIGINT COMMENT '면적당전기료',
  `water_fee_per_sqm` BIGINT COMMENT '면적당수도료',
  `heat_fee_per_sqm` BIGINT COMMENT '면적당난방비',
  `sample_count` INT NOT NULL COMMENT '표본단지수',
  `base_date` DATE COMMENT '데이터기준일',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `rent_recommend`;
CREATE TABLE `rent_recommend` (
  `rec_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '추천번호',
  `goal_id` BIGINT NOT NULL COMMENT '목표번호',
  `listing_id` BIGINT NOT NULL COMMENT '매물번호',
  `snapshot` JSON NOT NULL COMMENT '매물스냅샷',
  `rank_order` INT NOT NULL COMMENT '추천순위',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `housing_loan`;
CREATE TABLE `housing_loan` (
  `loan_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '상품번호',
  `name` VARCHAR(100) NOT NULL COMMENT '상품명',
  `product_type` VARCHAR(10) NOT NULL COMMENT '상품구분',
  `loan_type` VARCHAR(10) COMMENT '대출유형',
  `provider` VARCHAR(50) COMMENT '취급기관',
  `rate_summary` VARCHAR(100) COMMENT '금리요약',
  `loan_limit` BIGINT COMMENT '대출한도',
  `min_age` INT COMMENT '최소연령',
  `max_age` INT COMMENT '최대연령',
  `veteran_benefit` VARCHAR(200) COMMENT '군필우대내용',
  `join_condition` VARCHAR(200) COMMENT '가입조건',
  `detail` TEXT COMMENT '상세설명',
  `external_url` VARCHAR(300) COMMENT '외부링크',
  `is_kb` BOOLEAN NOT NULL COMMENT 'KB상품여부',
  `base_date` DATE COMMENT '정보기준일',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `loan_recommend`;
CREATE TABLE `loan_recommend` (
  `rec_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '추천번호',
  `goal_id` BIGINT NOT NULL COMMENT '목표번호',
  `loan_id` BIGINT NOT NULL COMMENT '상품번호',
  `snapshot` JSON NOT NULL COMMENT '상품스냅샷',
  `rank_order` INT NOT NULL COMMENT '추천순위',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `session_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '세션ID',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `title` VARCHAR(200) COMMENT '대화제목',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `message_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '메시지ID',
  `session_id` BIGINT NOT NULL COMMENT '세션ID',
  `role` VARCHAR(10) NOT NULL COMMENT '발신자구분',
  `content` TEXT NOT NULL COMMENT '메시지내용',
  `source` VARCHAR(100) COMMENT '출처문서',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `chat_feedback`;
CREATE TABLE `chat_feedback` (
  `feedback_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '피드백ID',
  `session_id` BIGINT NOT NULL COMMENT '세션ID',
  `message_id` BIGINT COMMENT '메시지ID',
  `feedback` VARCHAR(10) NOT NULL COMMENT '피드백',
  `reason` VARCHAR(100) COMMENT '피드백사유',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `openbanking_link`;
CREATE TABLE `openbanking_link` (
  `link_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '연동번호',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호 (회원당 N건 — 은행별 계좌)',
  `access_token` VARCHAR(300) NOT NULL COMMENT '액세스토큰',
  `refresh_token` VARCHAR(300) NOT NULL COMMENT '리프레시토큰',
  `fintech_use_num` VARCHAR(30) UNIQUE NOT NULL COMMENT '핀테크이용번호 (계좌당 고유 — 중복 연동 방지)',
  `bank_code` CHAR(3) COMMENT '은행코드',
  `account_id` BIGINT COMMENT '연동된 군적금 계좌 (적금계좌만 채움, 입출금계좌는 NULL)',
  `account_num_masked` VARCHAR(20) COMMENT '마스킹 계좌번호 (표시용)',
  `expires_at` DATETIME COMMENT '토큰만료일시',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `spending`;
CREATE TABLE `spending` (
  `spending_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '지출번호',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `merchant_name` VARCHAR(100) NOT NULL COMMENT '가맹점명',
  `category` VARCHAR(15) COMMENT '카테고리',
  `amount` BIGINT NOT NULL COMMENT '지출금액',
  `spent_at` DATETIME NOT NULL COMMENT '결제일시',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `spending_review`;
CREATE TABLE `spending_review` (
  `review_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '회고번호',
  `spending_id` BIGINT NOT NULL COMMENT '지출번호',
  `review_type` VARCHAR(10) NOT NULL COMMENT '회고구분',
  `reviewed_at` DATETIME NOT NULL COMMENT '태깅일시',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `merchant_category`;
CREATE TABLE `merchant_category` (
  `mapping_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '매핑번호',
  `keyword` VARCHAR(50) NOT NULL COMMENT '키워드',
  `category` VARCHAR(15) NOT NULL COMMENT '카테고리',
  `priority` INT NOT NULL COMMENT '우선순위',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `saving_challenge`;
CREATE TABLE `saving_challenge` (
  `challenge_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '목표번호',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `target_month` VARCHAR(7) NOT NULL COMMENT '대상월',
  `base_amount` BIGINT NOT NULL COMMENT '기준금액',
  `target_amount` BIGINT NOT NULL COMMENT '목표금액',
  `result_amount` BIGINT COMMENT '실제금액',
  `is_achieved` BOOLEAN COMMENT '달성여부',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `kakao_token`;
CREATE TABLE `kakao_token` (
  `token_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '토큰번호',
  `user_id` BIGINT UNIQUE NOT NULL COMMENT '회원고유번호 (회원당 1건)',
  `access_token` VARCHAR(300) NOT NULL COMMENT '액세스토큰',
  `refresh_token` VARCHAR(300) NOT NULL COMMENT '리프레시토큰',
  `expires_at` DATETIME COMMENT '토큰만료일시',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `noti_id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '알림번호',
  `user_id` BIGINT NOT NULL COMMENT '회원고유번호',
  `notify_type` VARCHAR(10) NOT NULL COMMENT '알림종류',
  `content` VARCHAR(200) COMMENT '발송내용',
  `is_read` BOOLEAN NOT NULL COMMENT '확인여부',
  `sent_at` DATETIME NOT NULL COMMENT '발송일시',
  `created_date` DATETIME NOT NULL COMMENT '생성일시',
  `created_nm` VARCHAR(50) NOT NULL COMMENT '생성자',
  `modified_date` DATETIME COMMENT '수정일시',
  `modified_nm` VARCHAR(50) COMMENT '수정자',
  `del_yn` CHAR(1) NOT NULL COMMENT '삭제여부'
);

CREATE UNIQUE INDEX `car_ev_index_0` ON `car_ev` (`region`, `base_year`);

ALTER TABLE `military_types` COMMENT = '군종(육군/해군/공군 등) 공통 코드';

ALTER TABLE `car_type` COMMENT = '경차/준중형/SUV 등 차종 구분 공통 코드 테이블';

ALTER TABLE `badge` COMMENT = '업적뱃지';

ALTER TABLE `bank_category` COMMENT = '은행 코드 공통 테이블';

ALTER TABLE `saving_product` COMMENT = '금감원 API 기반 KB 예·적금 상품 정보';

ALTER TABLE `card_product` COMMENT = '로드맵 추천용 KB 카드 상품 정보';

ALTER TABLE `roadmap_category` COMMENT = '로드맵 카테고리(여행/진로/자동차/자취) 공통 코드';

ALTER TABLE `user_bookmark` COMMENT = '회원이 관심 등록한 카테고리별 목표(관심항목)';

ALTER TABLE `terms` COMMENT = '회원 가입에 필요한 동의서';

ALTER TABLE `terms_agreement` COMMENT = '회원이 언제 약관에 동의하였는지 기록하는 테이블';

ALTER TABLE `user` COMMENT = '서비스에 가입한 이용자(장병/전역예비역) 정보';

ALTER TABLE `saving_account` COMMENT = '회원의 군적금 계좌 및 납입 현황';

ALTER TABLE `travel_goal` COMMENT = '회원이 등록한 여행 목표 정보';

ALTER TABLE `travel_cost` COMMENT = '여행 목표별 예상 경비 계산 결과';

ALTER TABLE `city_cost` COMMENT = '도시별 하루 물가 참조 데이터';

ALTER TABLE `travel_package` COMMENT = '크롤링 수집 여행 패키지 상품 정보';

ALTER TABLE `travel_insurance` COMMENT = '여행자 보험 상품 참조 데이터';

ALTER TABLE `job_goal` COMMENT = '목표 등록 화면 및 로드맵/메인페이지 조회에 사용되는 사용자별 진로 목표 데이터';

ALTER TABLE `job_code` COMMENT = '관리자가 사전 큐레이션하여 등록하는 드롭다운 선택지 기준 데이터';

ALTER TABLE `job_interested_type` COMMENT = '목표 등록 시 희망 준비항목 카테고리 복수선택 저장';

ALTER TABLE `prep_item_criteria` COMMENT = '관리자가 사전 조사·등록하는 준비항목 추천 기준 데이터';

ALTER TABLE `job_plan` COMMENT = '준비비용 계산 및 로드맵 조회에 활용되는 사용자별 확정 준비항목 데이터';

ALTER TABLE `service_criteria` COMMENT = '목표유형별로 사용자에게 노출할 정부 정책 및 KB 서비스 기준 정보 (관리자 사전 등록 데이터)';

ALTER TABLE `service_selection` COMMENT = '로드맵 저장 시 사용자가 선택한 정부 제도·KB 서비스 항목을 저장';

ALTER TABLE `car_goal` COMMENT = '회원이 등록한 자동차 구매 목표 정보';

ALTER TABLE `car_model` COMMENT = '신차 추천 및 중고차 시세 계산(연차별 정률감가)의 기준이 되는 신차 정보';

ALTER TABLE `car_insurance` COMMENT = '차종·운전경력별 예상 보험료 추정 기준 데이터';

ALTER TABLE `car_ev` COMMENT = '지역별 전기차 구매보조금(국비+지방비) 참조 데이터';

ALTER TABLE `car_tax_prepay` COMMENT = '신청월·연도별 자동차세 연납 할인율 참조 데이터';

ALTER TABLE `car_tax` COMMENT = '차종·배기량·지역별 취득세율 및 공채매입 기준 참조 데이터';

ALTER TABLE `rent_goal` COMMENT = '전역 후 월세(자취) 주거에 대한 사용자의 목표 정보';

ALTER TABLE `rent_goal_region` COMMENT = '목표별 희망 지역(읍·면·동) 정보';

ALTER TABLE `region_code` COMMENT = '지역 선택 및 매물 조회에 사용하는 법정동 코드 마스터';

ALTER TABLE `rent_listing` COMMENT = '월세 실거래 매물 정보';

ALTER TABLE `region_fee_stat` COMMENT = '지역 단위 평균 관리비 참고 통계';

ALTER TABLE `rent_recommend` COMMENT = '목표별 매물 추천 결과 이력 (로드맵 게시판)';

ALTER TABLE `housing_loan` COMMENT = '청년 월세·보증금 대출 상품 (정책 + KB 자체)';

ALTER TABLE `loan_recommend` COMMENT = '목표별 금융상품 추천 결과 이력 (로드맵 게시판)';

ALTER TABLE `chat_session` COMMENT = '회원별 챗봇 대화 세션';

ALTER TABLE `chat_message` COMMENT = '세션별 질문·답변 메시지';

ALTER TABLE `chat_feedback` COMMENT = '챗봇 답변에 대한 만족도 피드백';

ALTER TABLE `openbanking_link` COMMENT = '오픈뱅킹 인증 토큰 및 연동 계좌 정보';

ALTER TABLE `spending` COMMENT = '오픈뱅킹 거래내역에서 적재한 지출(출금) 정보';

ALTER TABLE `spending_review` COMMENT = '지출 건에 대한 만족/후회 태깅';

ALTER TABLE `merchant_category` COMMENT = '가맹점명 키워드와 소비 카테고리 매핑';

ALTER TABLE `saving_challenge` COMMENT = '월 단위 후회 소비 절감 목표 및 달성 결과';

ALTER TABLE `kakao_token` COMMENT = '카카오 알림 발송용 사용자 토큰';

ALTER TABLE `notification` COMMENT = '카카오 알림 발송 이력';

ALTER TABLE `card_product` ADD FOREIGN KEY (`category`) REFERENCES `roadmap_category` (`category_id`);

ALTER TABLE `user_bookmark` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `user_bookmark` ADD FOREIGN KEY (`category_id`) REFERENCES `roadmap_category` (`category_id`);

ALTER TABLE `terms_agreement` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `terms_agreement` ADD FOREIGN KEY (`terms_id`) REFERENCES `terms` (`terms_id`);

ALTER TABLE `user` ADD FOREIGN KEY (`type_id`) REFERENCES `military_types` (`type_id`);

ALTER TABLE `saving_account` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `saving_account` ADD FOREIGN KEY (`bank_code`) REFERENCES `bank_category` (`bank_code`);

ALTER TABLE `saving_history` ADD FOREIGN KEY (`account_id`) REFERENCES `saving_account` (`account_id`);

ALTER TABLE `vacation` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `travel_goal` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `travel_goal` ADD FOREIGN KEY (`package_id`) REFERENCES `travel_package` (`package_id`);

ALTER TABLE `travel_cost` ADD FOREIGN KEY (`goal_id`) REFERENCES `travel_goal` (`goal_id`);

ALTER TABLE `job_goal` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `job_goal` ADD FOREIGN KEY (`job_code_id`) REFERENCES `job_code` (`job_code_id`);

ALTER TABLE `job_interested_type` ADD FOREIGN KEY (`goal_id`) REFERENCES `job_goal` (`goal_id`);

ALTER TABLE `prep_item_criteria` ADD FOREIGN KEY (`job_code_id`) REFERENCES `job_code` (`job_code_id`);

ALTER TABLE `job_plan` ADD FOREIGN KEY (`goal_id`) REFERENCES `job_goal` (`goal_id`);

ALTER TABLE `service_selection` ADD FOREIGN KEY (`goal_id`) REFERENCES `job_goal` (`goal_id`);

ALTER TABLE `service_selection` ADD FOREIGN KEY (`svc_crit_id`) REFERENCES `service_criteria` (`svc_crit_id`);

ALTER TABLE `service_selection` ADD FOREIGN KEY (`card_id`) REFERENCES `card_product` (`card_id`);

ALTER TABLE `car_goal` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `car_goal` ADD FOREIGN KEY (`car_type_code`) REFERENCES `car_type` (`code`);

ALTER TABLE `car_goal` ADD FOREIGN KEY (`selected_model_id`) REFERENCES `car_model` (`model_id`);

ALTER TABLE `car_model` ADD FOREIGN KEY (`car_type_code`) REFERENCES `car_type` (`code`);

ALTER TABLE `car_insurance` ADD FOREIGN KEY (`car_type_code`) REFERENCES `car_type` (`code`);

ALTER TABLE `car_tax` ADD FOREIGN KEY (`car_type_code`) REFERENCES `car_type` (`code`);

ALTER TABLE `rent_goal` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `rent_goal_region` ADD FOREIGN KEY (`goal_id`) REFERENCES `rent_goal` (`goal_id`);

ALTER TABLE `rent_goal_region` ADD FOREIGN KEY (`region_code`) REFERENCES `region_code` (`region_code`);

ALTER TABLE `rent_listing` ADD FOREIGN KEY (`region_code`) REFERENCES `region_code` (`region_code`);

ALTER TABLE `region_fee_stat` ADD FOREIGN KEY (`region_code`) REFERENCES `region_code` (`region_code`);

ALTER TABLE `rent_recommend` ADD FOREIGN KEY (`goal_id`) REFERENCES `rent_goal` (`goal_id`);

ALTER TABLE `rent_recommend` ADD FOREIGN KEY (`listing_id`) REFERENCES `rent_listing` (`listing_id`);

ALTER TABLE `loan_recommend` ADD FOREIGN KEY (`goal_id`) REFERENCES `rent_goal` (`goal_id`);

ALTER TABLE `loan_recommend` ADD FOREIGN KEY (`loan_id`) REFERENCES `housing_loan` (`loan_id`);

ALTER TABLE `chat_session` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `chat_message` ADD FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`session_id`);

ALTER TABLE `chat_feedback` ADD FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`session_id`);

ALTER TABLE `chat_feedback` ADD FOREIGN KEY (`message_id`) REFERENCES `chat_message` (`message_id`);

ALTER TABLE `openbanking_link` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `openbanking_link` ADD FOREIGN KEY (`bank_code`) REFERENCES `bank_category` (`bank_code`);

ALTER TABLE `spending` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `spending_review` ADD FOREIGN KEY (`spending_id`) REFERENCES `spending` (`spending_id`);

ALTER TABLE `saving_challenge` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `kakao_token` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `notification` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `user_badge` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `user_badge` ADD FOREIGN KEY (`badge_id`) REFERENCES `badge` (`badge_id`);

ALTER TABLE `user` ADD FOREIGN KEY (`rank_id`) REFERENCES `military_rank` (`rank_id`);


SET FOREIGN_KEY_CHECKS = 1;
