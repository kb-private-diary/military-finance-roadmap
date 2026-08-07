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




SET FOREIGN_KEY_CHECKS = 1;
