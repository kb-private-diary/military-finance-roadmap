-- ######################################################################
--  [수연] 데모 / user1·user2 오픈뱅킹(마이데이터) 트랜잭션 데이터
--  테이블: saving_account, saving_history, income, spending, spending_review,
--          openbanking_link, rent_goal, rent_goal_region
--  전제: kb_demo_reviews.sql 가 먼저 로드되어 user(id=1,2)·vacation 존재.
--        user/vacation/chat 은 여기서 다시 INSERT 하지 않는다(중복 방지).
--  기준일: 오늘 2026-08-08
--   - user1(id=1): 현역 육군 상병, 입대 2025-09-01 / 전역예정 2027-02-28 (복무 12개월차)
--   - user2(id=2): 전역 육군 병장, 입대 2024-06-01 / 전역 2025-11-30 (복무 18개월)
--  감사컬럼: created_date=NOW(), created_nm='demo', del_yn='N' (만기계좌도 N 유지)
--  재실행 대비: 각 대상 테이블에서 user_id IN (1,2) 관련 행 DELETE 후 INSERT
-- ######################################################################

SET FOREIGN_KEY_CHECKS = 0;

-- ── 재실행 대비 정리(자식 → 부모 순서) ────────────────────────────────
DELETE FROM `spending_review` WHERE `spending_id` IN (SELECT `spending_id` FROM `spending` WHERE `user_id` IN (1,2));
DELETE FROM `spending`        WHERE `user_id` IN (1,2);
DELETE FROM `income`          WHERE `user_id` IN (1,2);
DELETE FROM `saving_history`  WHERE `account_id` IN (SELECT `account_id` FROM `saving_account` WHERE `user_id` IN (1,2));
DELETE FROM `openbanking_link` WHERE `user_id` IN (1,2);
DELETE FROM `saving_account`  WHERE `user_id` IN (1,2);
DELETE FROM `rent_goal_region` WHERE `goal_id` IN (SELECT `goal_id` FROM `rent_goal` WHERE `user_id` IN (1,2));
DELETE FROM `rent_goal`       WHERE `user_id` IN (1,2);

-- ── 군적금 계좌(saving_account) ───────────────────────────────────────
--  curr_amount = Σ saving_history.pay_amount (반드시 일치)
--  만기계산 입력: open_date + saving_history + user.discharge_date + bank_code(004/088)
INSERT INTO `saving_account`
(`account_id`, `user_id`, `bank_code`, `monthly_save`, `monthly_count`, `curr_amount`, `account_status`, `open_date`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
-- user1: KB(004)·신한(088) 각 월20만 × 12회 = 240만, ACTIVE
(1, 1, '004', 200000, 12, 2400000, 'ACTIVE',  '2025-09-10', NOW(), 'demo', NULL, NULL, 'N'),
(2, 1, '088', 200000, 12, 2400000, 'ACTIVE',  '2025-09-10', NOW(), 'demo', NULL, NULL, 'N'),
-- user2: KB(004) 월30만 × 18 = 540만 / 신한(088) 월10만 × 18 = 180만, MATURED(만기)
(3, 2, '004', 300000, 18, 5400000, 'MATURED', '2024-06-10', NOW(), 'demo', NULL, NULL, 'N'),
(4, 2, '088', 100000, 18, 1800000, 'MATURED', '2024-06-10', NOW(), 'demo', NULL, NULL, 'N');

-- ── 군적금 납입내역(saving_history) ───────────────────────────────────
INSERT INTO `saving_history`
(`history_id`, `account_id`, `pay_round`, `pay_amount`, `paid_date`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 1, 1, 200000, '2025-09-10', NOW(), 'demo', NULL, NULL, 'N'),
(2, 1, 2, 200000, '2025-10-10', NOW(), 'demo', NULL, NULL, 'N'),
(3, 1, 3, 200000, '2025-11-10', NOW(), 'demo', NULL, NULL, 'N'),
(4, 1, 4, 200000, '2025-12-10', NOW(), 'demo', NULL, NULL, 'N'),
(5, 1, 5, 200000, '2026-01-10', NOW(), 'demo', NULL, NULL, 'N'),
(6, 1, 6, 200000, '2026-02-10', NOW(), 'demo', NULL, NULL, 'N'),
(7, 1, 7, 200000, '2026-03-10', NOW(), 'demo', NULL, NULL, 'N'),
(8, 1, 8, 200000, '2026-04-10', NOW(), 'demo', NULL, NULL, 'N'),
(9, 1, 9, 200000, '2026-05-10', NOW(), 'demo', NULL, NULL, 'N'),
(10, 1, 10, 200000, '2026-06-10', NOW(), 'demo', NULL, NULL, 'N'),
(11, 1, 11, 200000, '2026-07-10', NOW(), 'demo', NULL, NULL, 'N'),
(12, 1, 12, 200000, '2026-08-10', NOW(), 'demo', NULL, NULL, 'N'),
(13, 2, 1, 200000, '2025-09-10', NOW(), 'demo', NULL, NULL, 'N'),
(14, 2, 2, 200000, '2025-10-10', NOW(), 'demo', NULL, NULL, 'N'),
(15, 2, 3, 200000, '2025-11-10', NOW(), 'demo', NULL, NULL, 'N'),
(16, 2, 4, 200000, '2025-12-10', NOW(), 'demo', NULL, NULL, 'N'),
(17, 2, 5, 200000, '2026-01-10', NOW(), 'demo', NULL, NULL, 'N'),
(18, 2, 6, 200000, '2026-02-10', NOW(), 'demo', NULL, NULL, 'N'),
(19, 2, 7, 200000, '2026-03-10', NOW(), 'demo', NULL, NULL, 'N'),
(20, 2, 8, 200000, '2026-04-10', NOW(), 'demo', NULL, NULL, 'N'),
(21, 2, 9, 200000, '2026-05-10', NOW(), 'demo', NULL, NULL, 'N'),
(22, 2, 10, 200000, '2026-06-10', NOW(), 'demo', NULL, NULL, 'N'),
(23, 2, 11, 200000, '2026-07-10', NOW(), 'demo', NULL, NULL, 'N'),
(24, 2, 12, 200000, '2026-08-10', NOW(), 'demo', NULL, NULL, 'N'),
(25, 3, 1, 300000, '2024-06-10', NOW(), 'demo', NULL, NULL, 'N'),
(26, 3, 2, 300000, '2024-07-10', NOW(), 'demo', NULL, NULL, 'N'),
(27, 3, 3, 300000, '2024-08-10', NOW(), 'demo', NULL, NULL, 'N'),
(28, 3, 4, 300000, '2024-09-10', NOW(), 'demo', NULL, NULL, 'N'),
(29, 3, 5, 300000, '2024-10-10', NOW(), 'demo', NULL, NULL, 'N'),
(30, 3, 6, 300000, '2024-11-10', NOW(), 'demo', NULL, NULL, 'N'),
(31, 3, 7, 300000, '2024-12-10', NOW(), 'demo', NULL, NULL, 'N'),
(32, 3, 8, 300000, '2025-01-10', NOW(), 'demo', NULL, NULL, 'N'),
(33, 3, 9, 300000, '2025-02-10', NOW(), 'demo', NULL, NULL, 'N'),
(34, 3, 10, 300000, '2025-03-10', NOW(), 'demo', NULL, NULL, 'N'),
(35, 3, 11, 300000, '2025-04-10', NOW(), 'demo', NULL, NULL, 'N'),
(36, 3, 12, 300000, '2025-05-10', NOW(), 'demo', NULL, NULL, 'N'),
(37, 3, 13, 300000, '2025-06-10', NOW(), 'demo', NULL, NULL, 'N'),
(38, 3, 14, 300000, '2025-07-10', NOW(), 'demo', NULL, NULL, 'N'),
(39, 3, 15, 300000, '2025-08-10', NOW(), 'demo', NULL, NULL, 'N'),
(40, 3, 16, 300000, '2025-09-10', NOW(), 'demo', NULL, NULL, 'N'),
(41, 3, 17, 300000, '2025-10-10', NOW(), 'demo', NULL, NULL, 'N'),
(42, 3, 18, 300000, '2025-11-10', NOW(), 'demo', NULL, NULL, 'N'),
(43, 4, 1, 100000, '2024-06-10', NOW(), 'demo', NULL, NULL, 'N'),
(44, 4, 2, 100000, '2024-07-10', NOW(), 'demo', NULL, NULL, 'N'),
(45, 4, 3, 100000, '2024-08-10', NOW(), 'demo', NULL, NULL, 'N'),
(46, 4, 4, 100000, '2024-09-10', NOW(), 'demo', NULL, NULL, 'N'),
(47, 4, 5, 100000, '2024-10-10', NOW(), 'demo', NULL, NULL, 'N'),
(48, 4, 6, 100000, '2024-11-10', NOW(), 'demo', NULL, NULL, 'N'),
(49, 4, 7, 100000, '2024-12-10', NOW(), 'demo', NULL, NULL, 'N'),
(50, 4, 8, 100000, '2025-01-10', NOW(), 'demo', NULL, NULL, 'N'),
(51, 4, 9, 100000, '2025-02-10', NOW(), 'demo', NULL, NULL, 'N'),
(52, 4, 10, 100000, '2025-03-10', NOW(), 'demo', NULL, NULL, 'N'),
(53, 4, 11, 100000, '2025-04-10', NOW(), 'demo', NULL, NULL, 'N'),
(54, 4, 12, 100000, '2025-05-10', NOW(), 'demo', NULL, NULL, 'N'),
(55, 4, 13, 100000, '2025-06-10', NOW(), 'demo', NULL, NULL, 'N'),
(56, 4, 14, 100000, '2025-07-10', NOW(), 'demo', NULL, NULL, 'N'),
(57, 4, 15, 100000, '2025-08-10', NOW(), 'demo', NULL, NULL, 'N'),
(58, 4, 16, 100000, '2025-09-10', NOW(), 'demo', NULL, NULL, 'N'),
(59, 4, 17, 100000, '2025-10-10', NOW(), 'demo', NULL, NULL, 'N'),
(60, 4, 18, 100000, '2025-11-10', NOW(), 'demo', NULL, NULL, 'N');

-- ── 급여 수입(income) : 매월 10일 09:00, 진급 반영 ────────────────────
--  계급 급여: 이병750,000 / 일병900,000 / 상병1,200,000 / 병장1,500,000
--  진급 경계: military_rank.service_months (이병1/일병3/상병9/병장15), 입대월=1개월차
INSERT INTO `income`
(`income_id`, `user_id`, `source`, `category`, `amount`, `received_at`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 1, '국군재정관리단', 'SALARY', 750000, '2025-10-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(2, 1, '국군재정관리단', 'SALARY', 900000, '2025-11-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(3, 1, '국군재정관리단', 'SALARY', 900000, '2025-12-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(4, 1, '국군재정관리단', 'SALARY', 900000, '2026-01-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(5, 1, '국군재정관리단', 'SALARY', 900000, '2026-02-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(6, 1, '국군재정관리단', 'SALARY', 900000, '2026-03-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(7, 1, '국군재정관리단', 'SALARY', 900000, '2026-04-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(8, 1, '국군재정관리단', 'SALARY', 1200000, '2026-05-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(9, 1, '국군재정관리단', 'SALARY', 1200000, '2026-06-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(10, 1, '국군재정관리단', 'SALARY', 1200000, '2026-07-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(11, 1, '국군재정관리단', 'SALARY', 1200000, '2026-08-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(12, 2, '국군재정관리단', 'SALARY', 750000, '2024-07-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(13, 2, '국군재정관리단', 'SALARY', 900000, '2024-08-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(14, 2, '국군재정관리단', 'SALARY', 900000, '2024-09-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(15, 2, '국군재정관리단', 'SALARY', 900000, '2024-10-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(16, 2, '국군재정관리단', 'SALARY', 900000, '2024-11-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(17, 2, '국군재정관리단', 'SALARY', 900000, '2024-12-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(18, 2, '국군재정관리단', 'SALARY', 900000, '2025-01-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(19, 2, '국군재정관리단', 'SALARY', 1200000, '2025-02-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(20, 2, '국군재정관리단', 'SALARY', 1200000, '2025-03-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(21, 2, '국군재정관리단', 'SALARY', 1200000, '2025-04-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(22, 2, '국군재정관리단', 'SALARY', 1200000, '2025-05-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(23, 2, '국군재정관리단', 'SALARY', 1200000, '2025-06-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(24, 2, '국군재정관리단', 'SALARY', 1200000, '2025-07-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(25, 2, '국군재정관리단', 'SALARY', 1500000, '2025-08-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(26, 2, '국군재정관리단', 'SALARY', 1500000, '2025-09-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(27, 2, '국군재정관리단', 'SALARY', 1500000, '2025-10-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(28, 2, '국군재정관리단', 'SALARY', 1500000, '2025-11-10 09:00:00', NOW(), 'demo', NULL, NULL, 'N');

-- ── 지출(spending) : 휴가월에만 발생, 비휴가월은 소비 없음 ──────────────
--  가맹점명은 merchant_category 키워드와 매칭(PX/DELIVERY/GAME/VACATION/CONVENIENCE)
INSERT INTO `spending`
(`spending_id`, `user_id`, `merchant_name`, `category`, `amount`, `spent_at`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 1, '국군복지단 PX', 'PX', 45000, '2025-10-16 14:20:00', NOW(), 'demo', NULL, NULL, 'N'),
(2, 1, 'GS25 위수지역점', 'CONVENIENCE', 12000, '2025-10-17 19:05:00', NOW(), 'demo', NULL, NULL, 'N'),
(3, 1, 'KTX 서울-부산', 'VACATION', 47000, '2026-02-02 10:10:00', NOW(), 'demo', NULL, NULL, 'N'),
(4, 1, '배달의민족', 'DELIVERY', 28000, '2026-02-03 20:40:00', NOW(), 'demo', NULL, NULL, 'N'),
(5, 1, '넥슨', 'GAME', 50000, '2026-02-04 22:30:00', NOW(), 'demo', NULL, NULL, 'N'),
(6, 1, 'KTX 부산-서울', 'VACATION', 47000, '2026-06-16 09:30:00', NOW(), 'demo', NULL, NULL, 'N'),
(7, 1, '쿠팡이츠', 'DELIVERY', 32000, '2026-06-17 21:15:00', NOW(), 'demo', NULL, NULL, 'N'),
(8, 1, '넥슨', 'GAME', 150000, '2026-06-18 23:50:00', NOW(), 'demo', NULL, NULL, 'N'),
(9, 1, '국군복지단 PX', 'PX', 38000, '2026-07-21 13:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(10, 1, '요기요', 'DELIVERY', 24000, '2026-07-22 20:20:00', NOW(), 'demo', NULL, NULL, 'N'),
(11, 2, '국군복지단 PX', 'PX', 40000, '2024-07-05 15:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(12, 2, 'CU 부대앞점', 'CONVENIENCE', 10000, '2024-07-06 18:30:00', NOW(), 'demo', NULL, NULL, 'N'),
(13, 2, 'KTX 서울-부산', 'VACATION', 45000, '2024-10-02 10:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(14, 2, '배달의민족', 'DELIVERY', 30000, '2024-10-03 20:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(15, 2, '넥슨', 'GAME', 80000, '2024-10-04 22:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(16, 2, 'KTX 부산-서울', 'VACATION', 45000, '2025-02-02 11:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(17, 2, '쿠팡이츠', 'DELIVERY', 27000, '2025-02-03 21:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(18, 2, '배달의민족', 'DELIVERY', 35000, '2025-05-02 20:30:00', NOW(), 'demo', NULL, NULL, 'N'),
(19, 2, '넥슨', 'GAME', 120000, '2025-05-03 23:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(20, 2, 'KTX 서울-부산', 'VACATION', 47000, '2025-10-02 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(21, 2, '요기요', 'DELIVERY', 26000, '2025-10-03 20:00:00', NOW(), 'demo', NULL, NULL, 'N');

-- ── 소비 회고 태깅(spending_review) : 게임/배달=REGRET, KTX/PX=SATISFIED ─
INSERT INTO `spending_review`
(`review_id`, `spending_id`, `review_type`, `reviewed_at`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 1, 'SATISFIED', '2025-10-16 14:20:00', NOW(), 'demo', NULL, NULL, 'N'),
(2, 3, 'SATISFIED', '2026-02-02 10:10:00', NOW(), 'demo', NULL, NULL, 'N'),
(3, 4, 'REGRET', '2026-02-03 20:40:00', NOW(), 'demo', NULL, NULL, 'N'),
(4, 5, 'REGRET', '2026-02-04 22:30:00', NOW(), 'demo', NULL, NULL, 'N'),
(5, 6, 'SATISFIED', '2026-06-16 09:30:00', NOW(), 'demo', NULL, NULL, 'N'),
(6, 7, 'REGRET', '2026-06-17 21:15:00', NOW(), 'demo', NULL, NULL, 'N'),
(7, 8, 'REGRET', '2026-06-18 23:50:00', NOW(), 'demo', NULL, NULL, 'N'),
(8, 9, 'SATISFIED', '2026-07-21 13:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(9, 10, 'REGRET', '2026-07-22 20:20:00', NOW(), 'demo', NULL, NULL, 'N'),
(10, 11, 'SATISFIED', '2024-07-05 15:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(11, 13, 'SATISFIED', '2024-10-02 10:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(12, 14, 'REGRET', '2024-10-03 20:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(13, 15, 'REGRET', '2024-10-04 22:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(14, 16, 'SATISFIED', '2025-02-02 11:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(15, 17, 'REGRET', '2025-02-03 21:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(16, 18, 'REGRET', '2025-05-02 20:30:00', NOW(), 'demo', NULL, NULL, 'N'),
(17, 19, 'REGRET', '2025-05-03 23:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(18, 20, 'SATISFIED', '2025-10-02 09:00:00', NOW(), 'demo', NULL, NULL, 'N'),
(19, 21, 'REGRET', '2025-10-03 20:00:00', NOW(), 'demo', NULL, NULL, 'N');

-- ── 오픈뱅킹 연동(openbanking_link) : 계좌당 1행, fintech_use_num 전부 유니크 ─
--  적금계좌는 account_id 채움, 입출금계좌는 account_id NULL
INSERT INTO `openbanking_link`
(`link_id`, `user_id`, `access_token`, `refresh_token`, `fintech_use_num`, `bank_code`, `account_id`, `account_num_masked`, `expires_at`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1, 1, 'DEMO-ACCESS-U1-KB-ADEPOSIT', 'DEMO-REFRESH-U1-KB-ADEPOSIT', 'FINTECH-U1-KB-0001', '004', 1, '004-01-****-1111', '2027-12-31 23:59:59', NOW(), 'demo', NULL, NULL, 'N'),
(2, 1, 'DEMO-ACCESS-U1-SH-ADEPOSIT', 'DEMO-REFRESH-U1-SH-ADEPOSIT', 'FINTECH-U1-SH-0002', '088', 2, '088-02-****-2222', '2027-12-31 23:59:59', NOW(), 'demo', NULL, NULL, 'N'),
(3, 1, 'DEMO-ACCESS-U1-KB-CHECKING', 'DEMO-REFRESH-U1-KB-CHECKING', 'FINTECH-U1-KB-0003', '004', NULL, '004-03-****-3333', '2027-12-31 23:59:59', NOW(), 'demo', NULL, NULL, 'N'),
(4, 2, 'DEMO-ACCESS-U2-KB-ADEPOSIT', 'DEMO-REFRESH-U2-KB-ADEPOSIT', 'FINTECH-U2-KB-0004', '004', 3, '004-04-****-4444', '2027-12-31 23:59:59', NOW(), 'demo', NULL, NULL, 'N'),
(5, 2, 'DEMO-ACCESS-U2-SH-ADEPOSIT', 'DEMO-REFRESH-U2-SH-ADEPOSIT', 'FINTECH-U2-SH-0005', '088', 4, '088-05-****-5555', '2027-12-31 23:59:59', NOW(), 'demo', NULL, NULL, 'N'),
(6, 2, 'DEMO-ACCESS-U2-KB-CHECKING', 'DEMO-REFRESH-U2-KB-CHECKING', 'FINTECH-U2-KB-0006', '004', NULL, '004-06-****-6666', '2027-12-31 23:59:59', NOW(), 'demo', NULL, NULL, 'N');

-- ── 자취 목표(rent_goal) + 지역(rent_goal_region) ─────────────────────
--  rent_goal_region.region_code = region_code 마스터의 법정동 10자리
INSERT INTO `rent_goal`
(`goal_id`, `user_id`, `title`, `selection_mode`, `school_id`, `commute_radius_km`,
 `monthly_budget`, `residence_preset`, `residence_months`, `status`, `confirmed_listing_id`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
-- user1: 부산 REGION, 월예산 70만, 거주 12개월, CONFIRMED
(1, 1, '부산 자취방 찾기', 'REGION', NULL, NULL, 700000,  'YEAR', 12, 'CONFIRMED', NULL, NOW(), 'demo', NULL, NULL, 'N'),
-- user2: 서울 REGION, 월예산 100만, DRAFT
(2, 2, '서울 자취방 찾기', 'REGION', NULL, NULL, 1000000, 'YEAR', 12, 'DRAFT',     NULL, NOW(), 'demo', NULL, NULL, 'N');

INSERT INTO `rent_goal_region`
(`region_id`, `goal_id`, `region_code`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
-- user1 부산 법정동 2개
(1, 1, '2620010100', NOW(), 'demo', NULL, NULL, 'N'),
(2, 1, '2644010300', NOW(), 'demo', NULL, NULL, 'N'),
-- user2 서울 법정동 1개
(3, 2, '1168010100', NOW(), 'demo', NULL, NULL, 'N');

-- (로더 kb_seed.sql 가 끝에서 FOREIGN_KEY_CHECKS=1 로 복구하므로 여기선 재활성화하지 않음)

-- [건수 요약] saving_account 4 / saving_history 60 / income 28 / spending 21 / spending_review 19 / openbanking_link 6 / rent_goal 2 / rent_goal_region 3
