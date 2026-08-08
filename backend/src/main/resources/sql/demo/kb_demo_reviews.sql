-- ######################################################################
--  테스트용 고정 회원 계정 2개
--  로그인 비밀번호는 둘 다 "test1234!" 입니다 (해시는 기존 hobin 계정과 동일한 값 재사용).
-- ######################################################################

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO `user`
(`user_id`, `password`, `name`, `phone`, `type_id`, `rank_id`, `unit_name`, `unit_code`,
 `enlist_date`, `discharge_date`, `login_provider`, `status`, `withdrawn_at`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
-- 현역 · 육군 상병 (2025-09-01 입대, 오늘 기준 약 11개월차 → 상병 구간)
('cpl@kbthink.com', '$2a$10$HbpaEJl9AV82dWtFjtftUOBEkwHjoXScs2bDTbpP7QgKitPYHyKM.',
 '테스트상병', '010-5555-0001', 1, 3, '제1보병사단', 'AD01',
 '2025-09-01', '2027-02-28', 'local', 'ACTIVE', NULL,
 NOW(), 'hobin', NULL, NULL, 'N'),

-- 전역자 · 육군 병장 (2025-11-30 전역 완료, 이미 전역 후 상태)
('vet@kbthink.com', '$2a$10$HbpaEJl9AV82dWtFjtftUOBEkwHjoXScs2bDTbpP7QgKitPYHyKM.',
 '테스트전역자', '010-5555-0002', 1, 4, '제9보병사단', 'AD09',
 '2024-06-01', '2025-11-30', 'local', 'ACTIVE', NULL,
 NOW(), 'hobin', NULL, NULL, 'N');



-- --------------------------------------------------------------------
--  [석윤] 대시보드/ 휴가(vacation)
--  테이블: vacation
--  전제: user_id는 AUTO_INCREMENT라 아래 user 테스트 데이터가 순서대로 먼저 들어갔을 때
--        cpl@kbthink.com -> id=1, vet@kbthink.com -> id=2 로 채번된다고 가정함.
--        (INSERT INTO `user` ... VALUES ('cpl@kbthink.com', ...), ('vet@kbthink.com', ...))
-- --------------------------------------------------------------------
INSERT INTO `vacation`
(`vacation_id`, `user_id`, `vacation_cate`, `vacation_name`, `vacation_get`, `vacation_day`, `vacation_state`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
-- 회원 1: 현역 상병(cpl@kbthink.com, 입대 2025-09-01 / 전역예정 2027-02-28, 오늘 기준 복무중)
-- REGULAR는 마스터(총 부여, state=FALSE) 1건 + 사용내역(state=TRUE) N건으로 관리 (DashboardServiceImpl 규칙)
-- 아직 복무중이라 정기휴가는 일부만 사용(잔여 있음), 다만 5개 카테고리는 전역자와 동일하게 전부 최소 1건씩 확보
(1, 1, 'REGULAR', '정기휴가', '2025-09-01', 24, FALSE, NOW(), 'seokyun', NULL, NULL, 'N'),           -- 총 부여 휴가
(2, 1, 'CONSOLATION', '신병위로휴가', '2025-10-15', 3, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),      -- 자대배치 후 사용
(3, 1, 'REGULAR', '정기휴가 사용', '2026-02-01', 5, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),         -- 1차, 5일 사용
(4, 1, 'PETITION', '자격증시험', '2026-03-01', 2, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),
(5, 1, 'ETC', '경조사휴가', '2026-05-01', 1, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),
(6, 1, 'REGULAR', '정기휴가 사용', '2026-06-15', 6, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),         -- 2차, 6일 사용 (누적 11일, 잔여 13일)
(7, 1, 'REWARD', '체력우수 포상휴가', '2026-07-20', 2, false, NOW(), 'seokyun', NULL, NULL, 'N'),       -- 최근 사용

-- 회원 2: 전역자 병장(vet@kbthink.com, 입대 2024-06-01 / 전역 2025-11-30, 이미 전역 완료)
-- 전역 완료 시나리오라 (a) 5개 카테고리(REGULAR/CONSOLATION/REWARD/PETITION/ETC) 전부 최소 1건씩 존재,
-- (b) REGULAR는 총 부여(24일)만큼 사용내역 합계도 정확히 24일로 맞춰 잔여 0으로 마감
(8, 2, 'REGULAR', '정기휴가', '2024-06-01', 24, FALSE, NOW(), 'seokyun', NULL, NULL, 'N'),            -- 총 부여 휴가
(9, 2, 'CONSOLATION', '신병위로휴가', '2024-07-01', 3, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),       -- 자대배치 후 사용
(10, 2, 'REGULAR', '정기휴가 사용', '2024-10-01', 8, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),         -- 1차, 8일 사용
(11, 2, 'PETITION', '자격증시험', '2025-01-15', 2, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),
(12, 2, 'REWARD', '사격우수 포상휴가', '2025-02-01', 4, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),
(13, 2, 'REGULAR', '정기휴가 사용', '2025-05-01', 8, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),         -- 2차, 8일 사용
(14, 2, 'ETC', '경조사휴가', '2025-09-01', 2, TRUE, NOW(), 'seokyun', NULL, NULL, 'N'),
(15, 2, 'REGULAR', '정기휴가 사용', '2025-10-01', 8, TRUE, NOW(), 'seokyun', NULL, NULL, 'N');         -- 3차, 8일 사용 (합계 24일, 잔여 0)




-- --------------------------------------------------------------------
--  [에스더] 챗봇 / 대화 히스토리(chat_session, chat_message)
--  테이블: chat_session, chat_message
--  전제: user_id는 AUTO_INCREMENT라 위 user 테스트 데이터가 순서대로 먼저 들어갔을 때
--        cpl@kbthink.com -> id=1, vet@kbthink.com -> id=2 로 채번된다고 가정함(석윤님 파트와 동일 전제).
--  목적: "이전기록" 버튼을 눌렀을 때 최근 3일치 대화가 찍히고 위로 스크롤하면 더 나오는 걸
--        보여주기 위한 데모용 - 실제 기능 동작 자체는 발표 때 라이브로 시연하므로, 두 회원의
--        대화 내용은 굳이 다르게 안 만들고 동일한 3일치를 각자 세션에 그대로 채워 넣는다.
--  세션: 유저당 세션을 하루 단위로 새로 안 만들고 계속 재사용하는 서비스 정책에 맞춰
--        회원당 세션 1개만 만들고, 그 안에 날짜가 다른 메시지들을 채운다.
-- --------------------------------------------------------------------
INSERT INTO `chat_session`
(`session_id`, `user_id`, `title`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
(1, 1, NULL, '2026-08-06 09:00:00', 'eseudeo', NULL, NULL, 'N'),  -- 회원 1: 현역 상병(cpl@kbthink.com)
(2, 2, NULL, '2026-08-06 09:00:00', 'eseudeo', NULL, NULL, 'N');  -- 회원 2: 전역자 병장(vet@kbthink.com)

INSERT INTO `chat_message`
(`message_id`, `session_id`, `role`, `content`, `source`, `source_detail`, `is_ai_generated`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
-- 회원 1 (session_id=1, 현역) - 1일차 2026-08-06
(1, 1, 'user', '장병내일준비적금은 누가 가입할 수 있습니까?', NULL, NULL, FALSE, '2026-08-06 09:00:00', 'eseudeo', NULL, NULL, 'N'),
(2, 1, 'bot', '장병내일준비적금은 현역병, 상근예비역, 의무경찰 등 복무 중인 병 계급이면 가입할 수 있습니다. 자세한 대상 요건은 은행 창구에서 한 번 더 확인하시기 바랍니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 장병내일준비적금 (2026년 3월 기준)', TRUE, '2026-08-06 09:00:05', 'eseudeo', NULL, NULL, 'N'),
(3, 1, 'user', '한 달에 최대 얼마까지 넣을 수 있습니까?', NULL, NULL, FALSE, '2026-08-06 13:00:00', 'eseudeo', NULL, NULL, 'N'),
(4, 1, 'bot', '장병내일준비적금은 월 최대 40만원까지 납입할 수 있습니다. 여러 은행에 나눠 가입해도 합산 한도가 적용된다는 점 참고하시기 바랍니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 장병내일준비적금 (2026년 3월 기준)', TRUE, '2026-08-06 13:00:04', 'eseudeo', NULL, NULL, 'N'),
(5, 1, 'user', '중도해지하면 손해봅니까?', NULL, NULL, FALSE, '2026-08-06 20:00:00', 'eseudeo', NULL, NULL, 'N'),
(6, 1, 'bot', '중도해지 시에는 우대금리와 정부 지원 이자 혜택을 받지 못하고 기본금리만 적용되므로, 가능하면 만기까지 유지하시는 것이 유리하지 말입니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 장병내일준비적금 (2026년 3월 기준)', TRUE, '2026-08-06 20:00:06', 'eseudeo', NULL, NULL, 'N'),

-- 회원 1 (session_id=1, 현역) - 2일차 2026-08-07
(7, 1, 'user', '청년미래적금 정부기여금은 얼마나 받을 수 있습니까?', NULL, NULL, FALSE, '2026-08-07 09:00:00', 'eseudeo', NULL, NULL, 'N'),
(8, 1, 'bot', '청년미래적금은 납입액에 비례해 정부기여금이 매칭 지급되며, 소득 구간에 따라 지급 비율이 달라집니다. 정확한 금액은 가입 시 안내되는 개인별 한도를 확인하시기 바랍니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년미래적금 (2026년 3월 기준)', TRUE, '2026-08-07 09:00:05', 'eseudeo', NULL, NULL, 'N'),
(9, 1, 'user', '청년도약계좌에서 갈아탈 수 있습니까?', NULL, NULL, FALSE, '2026-08-07 13:00:00', 'eseudeo', NULL, NULL, 'N'),
(10, 1, 'bot', '네, 청년도약계좌 가입자는 일정 조건을 충족하면 청년미래적금으로 갈아탈 수 있습니다. 전환 시 기존 납입 기간이 인정되는지는 은행에 문의하시기 바랍니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년미래적금 (2026년 3월 기준)', TRUE, '2026-08-07 13:00:04', 'eseudeo', NULL, NULL, 'N'),
(11, 1, 'user', '청년주택드림청약통장 비과세 요건이 뭡니까?', NULL, NULL, FALSE, '2026-08-07 20:00:00', 'eseudeo', NULL, NULL, 'N'),
(12, 1, 'bot', '청년주택드림청약통장은 무주택 세대주 등 일정 요건을 충족하고 일정 기간 이상 유지하면 이자소득에 대해 비과세 혜택을 받을 수 있습니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년주택드림청약통장 (2026년 3월 기준)', TRUE, '2026-08-07 20:00:06', 'eseudeo', NULL, NULL, 'N'),

-- 회원 1 (session_id=1, 현역) - 3일차 2026-08-08 (오늘)
(13, 1, 'user', '청약 당첨되면 어떻게 됩니까?', NULL, NULL, FALSE, '2026-08-08 09:00:00', 'eseudeo', NULL, NULL, 'N'),
(14, 1, 'bot', '청약에 당첨되면 청약통장은 자동으로 해지되며, 이후 분양 계약 절차에 따라 진행하시면 됩니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년주택드림청약통장 (2026년 3월 기준)', TRUE, '2026-08-08 09:00:05', 'eseudeo', NULL, NULL, 'N'),
(15, 1, 'user', '기존 청약통장에서 바꿀 수 있습니까?', NULL, NULL, FALSE, '2026-08-08 13:00:00', 'eseudeo', NULL, NULL, 'N'),
(16, 1, 'bot', '기존 주택청약종합저축 가입자는 일정 조건을 충족하면 청년주택드림청약통장으로 전환할 수 있습니다. 전환 시 기존 가입 기간과 납입 실적이 인정됩니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년주택드림청약통장 (2026년 3월 기준)', TRUE, '2026-08-08 13:00:04', 'eseudeo', NULL, NULL, 'N'),
(17, 1, 'user', '비과세가 무엇입니까?', NULL, NULL, FALSE, '2026-08-08 20:00:00', 'eseudeo', NULL, NULL, 'N'),
-- 정책용어사전(일반 용어 정의) 출처는 실제 서비스에서도 출처 캡션을 안 붙이므로 source/source_detail을 비워둔다(gemini.py _build_source_detail과 동일 규칙)
(18, 1, 'bot', '비과세란 이자소득 등에 대해 세금을 부과하지 않는 것을 말합니다. 일반적으로 이자소득에는 15.4%의 세금이 부과되지만, 비과세 요건을 충족하면 이 세금이 면제됩니다.', NULL, NULL, TRUE, '2026-08-08 20:00:06', 'eseudeo', NULL, NULL, 'N'),

-- 회원 2 (session_id=2, 전역) - 회원 1과 동일한 3일치 대화를 그대로 재사용(위 목적 설명 참고)
(19, 2, 'user', '장병내일준비적금은 누가 가입할 수 있습니까?', NULL, NULL, FALSE, '2026-08-06 09:00:00', 'eseudeo', NULL, NULL, 'N'),
(20, 2, 'bot', '장병내일준비적금은 현역병, 상근예비역, 의무경찰 등 복무 중인 병 계급이면 가입할 수 있습니다. 자세한 대상 요건은 은행 창구에서 한 번 더 확인하시기 바랍니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 장병내일준비적금 (2026년 3월 기준)', TRUE, '2026-08-06 09:00:05', 'eseudeo', NULL, NULL, 'N'),
(21, 2, 'user', '한 달에 최대 얼마까지 넣을 수 있습니까?', NULL, NULL, FALSE, '2026-08-06 13:00:00', 'eseudeo', NULL, NULL, 'N'),
(22, 2, 'bot', '장병내일준비적금은 월 최대 40만원까지 납입할 수 있습니다. 여러 은행에 나눠 가입해도 합산 한도가 적용된다는 점 참고하시기 바랍니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 장병내일준비적금 (2026년 3월 기준)', TRUE, '2026-08-06 13:00:04', 'eseudeo', NULL, NULL, 'N'),
(23, 2, 'user', '중도해지하면 손해봅니까?', NULL, NULL, FALSE, '2026-08-06 20:00:00', 'eseudeo', NULL, NULL, 'N'),
(24, 2, 'bot', '중도해지 시에는 우대금리와 정부 지원 이자 혜택을 받지 못하고 기본금리만 적용되므로, 가능하면 만기까지 유지하시는 것이 유리하지 말입니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 장병내일준비적금 (2026년 3월 기준)', TRUE, '2026-08-06 20:00:06', 'eseudeo', NULL, NULL, 'N'),

(25, 2, 'user', '청년미래적금 정부기여금은 얼마나 받을 수 있습니까?', NULL, NULL, FALSE, '2026-08-07 09:00:00', 'eseudeo', NULL, NULL, 'N'),
(26, 2, 'bot', '청년미래적금은 납입액에 비례해 정부기여금이 매칭 지급되며, 소득 구간에 따라 지급 비율이 달라집니다. 정확한 금액은 가입 시 안내되는 개인별 한도를 확인하시기 바랍니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년미래적금 (2026년 3월 기준)', TRUE, '2026-08-07 09:00:05', 'eseudeo', NULL, NULL, 'N'),
(27, 2, 'user', '청년도약계좌에서 갈아탈 수 있습니까?', NULL, NULL, FALSE, '2026-08-07 13:00:00', 'eseudeo', NULL, NULL, 'N'),
(28, 2, 'bot', '네, 청년도약계좌 가입자는 일정 조건을 충족하면 청년미래적금으로 갈아탈 수 있습니다. 전환 시 기존 납입 기간이 인정되는지는 은행에 문의하시기 바랍니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년미래적금 (2026년 3월 기준)', TRUE, '2026-08-07 13:00:04', 'eseudeo', NULL, NULL, 'N'),
(29, 2, 'user', '청년주택드림청약통장 비과세 요건이 뭡니까?', NULL, NULL, FALSE, '2026-08-07 20:00:00', 'eseudeo', NULL, NULL, 'N'),
(30, 2, 'bot', '청년주택드림청약통장은 무주택 세대주 등 일정 요건을 충족하고 일정 기간 이상 유지하면 이자소득에 대해 비과세 혜택을 받을 수 있습니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년주택드림청약통장 (2026년 3월 기준)', TRUE, '2026-08-07 20:00:06', 'eseudeo', NULL, NULL, 'N'),

(31, 2, 'user', '청약 당첨되면 어떻게 됩니까?', NULL, NULL, FALSE, '2026-08-08 09:00:00', 'eseudeo', NULL, NULL, 'N'),
(32, 2, 'bot', '청약에 당첨되면 청약통장은 자동으로 해지되며, 이후 분양 계약 절차에 따라 진행하시면 됩니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년주택드림청약통장 (2026년 3월 기준)', TRUE, '2026-08-08 09:00:05', 'eseudeo', NULL, NULL, 'N'),
(33, 2, 'user', '기존 청약통장에서 바꿀 수 있습니까?', NULL, NULL, FALSE, '2026-08-08 13:00:00', 'eseudeo', NULL, NULL, 'N'),
(34, 2, 'bot', '기존 주택청약종합저축 가입자는 일정 조건을 충족하면 청년주택드림청약통장으로 전환할 수 있습니다. 전환 시 기존 가입 기간과 납입 실적이 인정됩니다.', 'Gemini AI (정책 문서 기반 생성)', 'KB국민은행 상품안내 · 청년주택드림청약통장 (2026년 3월 기준)', TRUE, '2026-08-08 13:00:04', 'eseudeo', NULL, NULL, 'N'),
(35, 2, 'user', '비과세가 무엇입니까?', NULL, NULL, FALSE, '2026-08-08 20:00:00', 'eseudeo', NULL, NULL, 'N'),
(36, 2, 'bot', '비과세란 이자소득 등에 대해 세금을 부과하지 않는 것을 말합니다. 일반적으로 이자소득에는 15.4%의 세금이 부과되지만, 비과세 요건을 충족하면 이 세금이 면제됩니다.', NULL, NULL, TRUE, '2026-08-08 20:00:06', 'eseudeo', NULL, NULL, 'N');




-- ######################################################################
--  [수연] 오픈뱅킹·적금·자취 (user1·user2 마이데이터 트랜잭션 데이터)
--  테이블: saving_account, saving_history, income, spending, spending_review,
--          openbanking_link, rent_goal, rent_goal_region
--  전제: 위 user(id=1,2)·vacation·chat 이 이미 INSERT 됨(여기서 재INSERT 안 함).
--  기준일: 오늘 2026-08-08
--   - user1(id=1): 현역 육군 상병, 입대 2025-09-01 / 전역예정 2027-02-28 (복무 12개월차)
--   - user2(id=2): 전역 육군 병장, 입대 2024-06-01 / 전역 2025-11-30 (복무 18개월)
--  감사컬럼: created_date=NOW(), created_nm='demo', del_yn='N' (만기계좌도 N 유지)
--  재실행 대비: 각 대상 테이블에서 user_id IN (1,2) 관련 행 DELETE 후 INSERT
-- ######################################################################

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

-- [건수 요약] saving_account 4 / saving_history 60 / income 28 / spending 21 / spending_review 19 / openbanking_link 6 / rent_goal 2 / rent_goal_region 3



SET FOREIGN_KEY_CHECKS = 1;
