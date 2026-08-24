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
 '김호국', '010-5555-0001', 1, 3, '수도방위사령부', 'AD01',
 '2025-09-01', '2027-02-28', 'local', 'ACTIVE', NULL,
 NOW(), 'hobin', NULL, NULL, 'N'),

-- 전역자 · 육군 병장 (2025-11-30 전역 완료, 이미 전역 후 상태)
('vet@kbthink.com', '$2a$10$HbpaEJl9AV82dWtFjtftUOBEkwHjoXScs2bDTbpP7QgKitPYHyKM.',
 '최전역', '010-5555-0002', 1, 4, '제9보병사단', 'AD09',
 '2024-06-01', '2025-11-30', 'local', 'ACTIVE', NULL,
 NOW(), 'hobin', NULL, NULL, 'N');



-- --------------------------------------------------------------------
--  [석윤] 대시보드/ 휴가(vacation, vacation_history)
--  테이블: vacation(부여), vacation_history(사용내역)
--  전제: user_id는 AUTO_INCREMENT라 아래 user 테스트 데이터가 순서대로 먼저 들어갔을 때
--        cpl@kbthink.com -> id=1, vet@kbthink.com -> id=2 로 채번된다고 가정함.
--        (INSERT INTO `user` ... VALUES ('cpl@kbthink.com', ...), ('vet@kbthink.com', ...))
--  잔여일수 = vacation.vacation_day - SUM(vacation_history.used_day). 카테고리 구분 없이 동일 규칙.
-- --------------------------------------------------------------------
INSERT INTO `vacation`
(`vacation_id`, `user_id`, `vacation_cate`, `vacation_name`, `vacation_get`, `vacation_day`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
-- 회원 1: 현역 상병(cpl@kbthink.com, 입대 2025-09-01 / 전역예정 2027-02-28, 오늘 기준 복무중)
-- 아직 복무중이라 정기휴가는 일부만 사용(잔여 있음), 다만 5개 카테고리는 전역자와 동일하게 전부 최소 1건씩 확보
(1, 1, 'REGULAR', '정기휴가', '2025-09-01', 24, NOW(), 'seokyun', NULL, NULL, 'N'),
(2, 1, 'CONSOLATION', '신병위로휴가', '2025-10-15', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
(3, 1, 'PETITION', '자격증시험', '2026-03-01', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
(4, 1, 'ETC', '경조사휴가', '2026-05-01', 1, NOW(), 'seokyun', NULL, NULL, 'N'),
(5, 1, 'REWARD', '체력우수 포상휴가', '2026-07-20', 2, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 미사용(잔여 2일)

-- 회원 2: 전역자 병장(vet@kbthink.com, 입대 2024-06-01 / 전역 2025-11-30, 이미 전역 완료)
-- 전역 완료 시나리오라 (a) 5개 카테고리(REGULAR/CONSOLATION/REWARD/PETITION/ETC) 전부 최소 1건씩 존재,
-- (b) REGULAR는 총 부여(24일)만큼 사용내역 합계도 정확히 24일로 맞춰 잔여 0으로 마감
(6, 2, 'REGULAR', '정기휴가', '2024-06-01', 24, NOW(), 'seokyun', NULL, NULL, 'N'),
(7, 2, 'CONSOLATION', '신병위로휴가', '2024-07-01', 3, NOW(), 'seokyun', NULL, NULL, 'N'),
(8, 2, 'PETITION', '자격증시험', '2025-01-15', 2, NOW(), 'seokyun', NULL, NULL, 'N'),
(9, 2, 'REWARD', '사격우수 포상휴가', '2025-02-01', 4, NOW(), 'seokyun', NULL, NULL, 'N'),
(10, 2, 'ETC', '경조사휴가', '2025-09-01', 2, NOW(), 'seokyun', NULL, NULL, 'N');

INSERT INTO `vacation_history`
(`history_id`, `vacation_id`, `used_date`, `used_day`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
-- 회원 1
(1, 2, '2025-10-15', 3, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 신병위로휴가 전부 사용
(2, 1, '2026-02-01', 5, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 정기휴가 1차, 5일 사용
(3, 3, '2026-03-01', 2, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 청원휴가 전부 사용
(4, 4, '2026-05-01', 1, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 경조사휴가 전부 사용
(5, 1, '2026-06-15', 6, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 정기휴가 2차, 6일 사용 (누적 11일, 잔여 13일)

-- 회원 2 (REGULAR 합계 8+8+8=24일 -> 잔여 0)
(6, 7, '2024-07-01', 3, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 신병위로휴가 전부 사용
(7, 6, '2024-10-01', 8, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 정기휴가 1차, 8일 사용
(8, 8, '2025-01-15', 2, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 청원휴가 전부 사용
(9, 9, '2025-02-01', 4, NOW(), 'seokyun', NULL, NULL, 'N'),   -- 포상휴가 전부 사용
(10, 6, '2025-05-01', 8, NOW(), 'seokyun', NULL, NULL, 'N'),  -- 정기휴가 2차, 8일 사용
(11, 10, '2025-09-01', 2, NOW(), 'seokyun', NULL, NULL, 'N'), -- 경조사휴가 전부 사용
(12, 6, '2025-10-01', 8, NOW(), 'seokyun', NULL, NULL, 'N');  -- 정기휴가 3차, 8일 사용 (합계 24일, 잔여 0)




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



-- --------------------------------------------------------------------
--  [석윤] 웹푸시/ 발송이력(push_history)
--  테이블: push_history
--  전제: user_id는 위 user 테스트 데이터 기준 cpl@kbthink.com -> id=1(현역), vet@kbthink.com -> id=2(전역자)
--  category는 프론트 아이콘 매핑용 자유 문자열(WebPushPage.vue CATEGORY_ICON_MAP: SAVING/VACATION만 아이콘 매핑되고 나머지는 기본 아이콘)
-- --------------------------------------------------------------------
INSERT INTO `push_history`
(`history_id`, `user_id`, `title`, `body`, `category`, `status`, `sent_at`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES
-- 회원 1: 현역 상병 - 휴가추가, 적금 납입완료
(1, 1, '휴가가 추가되었습니다', '체력우수 포상휴가 2일이 등록되었습니다.', 'VACATION', 'SUCCESS', '2026-07-20 10:00:00', NOW(), 'seokyun', NULL, NULL, 'N'),
(2, 1, '적금 납입이 완료되었습니다', '이번 달 장병내일준비적금 납입이 완료되었습니다.', 'SAVING', 'SUCCESS', '2026-08-01 09:00:00', NOW(), 'seokyun', NULL, NULL, 'N'),

-- 회원 2: 전역자 병장 - 전역 축하, 적금 수령완료
(3, 2, '전역을 축하드립니다', '테스트전역자님, 전역을 진심으로 축하드립니다!', 'DISCHARGE', 'SUCCESS', '2025-11-30 09:00:00', NOW(), 'seokyun', NULL, NULL, 'N'),
(4, 2, '적금을 수령하셨습니다', '장병내일준비적금 만기 수령금이 입금되었습니다.', 'SAVING', 'SUCCESS', '2025-12-01 09:00:00', NOW(), 'seokyun', NULL, NULL, 'N');


-- ====================================================================
-- [지원] 진로 테스트 데이터
-- ====================================================================

-- --------------------------------------------------------------------
-- [지원] 진로 목표
-- 테이블: job_goal
-- 전제: user_id는 공통 테스트 회원 기준
--       회원1(id=1), 회원2(id=2)
-- --------------------------------------------------------------------

INSERT INTO `job_goal`
(`goal_id`, `user_id`, `goal_type`, `category_id`, `univ_id`, `major_id`, `expected_date`, `status`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES

-- 회원1(현역)
-- 취업 : 웹개발
-- 회원1(현역)
-- 기존 취업 목표 : 데이터엔지니어·데이터분석·DBA
(1, 1, 'J01', 33, NULL, NULL, '2027-03', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N'),

-- 회원2(전역)
-- 공무원 : 군무원 행정직
(2, 2, 'J02', 109, NULL, NULL, '2027-12', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N'),

-- 회원2(전역)
-- 편입 : 가천대학교 > 전기·전자·반도체·통신
(3, 2, 'J03', NULL, 1, 4, '2027-03', 'CONFIRMED', NOW(), 'jiwon', NULL, NULL, 'N');


-- --------------------------------------------------------------------
-- [지원] 진로/목표별 선택 자격증·어학
-- 테이블: job_goal_qualification
-- --------------------------------------------------------------------

INSERT INTO `job_goal_qualification`
(`goal_qual_id`, `goal_id`, `qual_id`, `selected_cost`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES

-- 회원1(현역)
-- 취업 : 데이터엔지니어·데이터분석·DBA
-- 선택 자격증 : 데이터분석준전문가(ADsP)
(1, 1, 60, 50000, NOW(), 'jiwon', NULL, NULL, 'N'),

-- 회원2(전역)
-- 공무원 : 군무원 행정직
-- 선택 자격증 : 한국사능력검정시험
(2, 2, 103, 27000, NOW(), 'jiwon', NULL, NULL, 'N'),

-- 회원2(전역)
-- 편입 : 가천대학교 > 전기·전자·반도체·통신
-- 선택 어학 : TOEIC
(3, 3, 106, 26200, NOW(), 'jiwon', NULL, NULL, 'N');


-- --------------------------------------------------------------------
-- [지원] 진로/목표별 선택 인강
-- 테이블: job_goal_course
-- --------------------------------------------------------------------

INSERT INTO `job_goal_course`
(`goal_course_id`, `goal_id`, `course_id`, `selected_cost`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`)
VALUES

-- 회원1(현역)
-- 취업 : 데이터엔지니어·데이터분석·DBA
-- 선택 인강 : ADsP 자격증 대비 인강
(1, 1, 34, 49500, NOW(), 'jiwon', NULL, NULL, 'N'),

-- 회원2(전역)
-- 공무원 : 군무원 행정직
-- 선택 인강 : 공단기 27대비 9급 군무원 환급 직렬패스
(2, 2, 78, 700000, NOW(), 'jiwon', NULL, NULL, 'N'),

-- 회원2(전역)
-- 편입 : 가천대학교 > 전기·전자·반도체·통신
-- 선택 인강 : 2027+2028 김영패스 원더 [자연]
(3, 3, 92, 1470000, NOW(), 'jiwon', NULL, NULL, 'N');



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
-- ⭐ [데모 시연용] user1의 8월분(2026-08-10, 상병 120만)을 일부러 뺐다 → 월급 배치로 "입금되는 순간"을 시연한다.
--    POST /api/openbanking/salary/run-batch 실행 시 배치가 "8월 급여 없네" → 8월분 1건을 자동 입금(id 11은 결번, auto_increment로 새 id 부여).
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

-- ######################################################################
--  [데모추가] user1(id=1) 후회소비 시연 데이터 (created_nm='demo_hb')
--  목적: ①소비점호(미점호=review 없는 지출) 태깅거리 확보 ②대시보드 캘린더 알록달록
--  기준일: 오늘 2026-08-08.  spending_id 22~53 / review_id 20~41 (기존 21·19 뒤 연속)
--  재실행 안전: 위 DELETE 블록이 user_id IN (1,2) 기준으로 지우므로 이 행들도 함께 정리됨
--  (spending 먼저 → spending_review 나중: FK spending_id 정합 유지)
-- ######################################################################

-- ── 1) 소비점호용 '미점호' : 이번달(2026-08) 지출 10건, spending_review 없음 ──
--  현역 일상소비(편의점·배달·카페·게임·PX) - 아래 spending_review에 안 넣어 미점호로 남김
INSERT INTO `spending`
(`spending_id`, `user_id`, `merchant_name`, `category`, `amount`, `spent_at`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(22, 1, 'GS25 위수지역점',   'CONVENIENCE', 8500,  '2026-08-01 12:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(23, 1, '배달의민족',        'DELIVERY',    19000, '2026-08-01 19:40:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(24, 1, '스타벅스',          'ETC',         6300,  '2026-08-02 15:10:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(25, 1, '넥슨',              'GAME',        30000, '2026-08-03 22:15:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(26, 1, 'CU 부대앞점',       'CONVENIENCE', 5400,  '2026-08-04 18:20:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(27, 1, '요기요',            'DELIVERY',    22000, '2026-08-05 20:05:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(28, 1, '국군복지단 PX',     'PX',          15000, '2026-08-06 13:40:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(29, 1, '쿠팡이츠',          'DELIVERY',    17500, '2026-08-07 21:00:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(30, 1, '세븐일레븐 부대점', 'CONVENIENCE', 9900,  '2026-08-08 12:10:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(31, 1, 'APPLE 앱스토어',    'GAME',        12000, '2026-08-08 23:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),

-- ── 2) 캘린더용 '후회(REGRET)' : 이번달 10일 분산, 금액 4레벨 골고루 ──
--  ~1만: 08-02·04·07 / ~3만: 08-03·06·14 / ~5만: 08-10·12 / 5만+: 08-01·08
--  (08-10·12·14는 오늘 이후지만 캘린더 채색용으로 의도 배치 - 부모 지시 예시 준수)
(32, 1, '넥슨',       'GAME',     60000, '2026-08-01 23:20:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(33, 1, '배달의민족', 'DELIVERY',  9000, '2026-08-02 20:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(34, 1, '넥슨',       'GAME',     32000, '2026-08-03 21:40:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(35, 1, '쿠팡이츠',   'DELIVERY', 12000, '2026-08-04 19:50:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(36, 1, '요기요',     'DELIVERY', 28000, '2026-08-06 20:25:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(37, 1, 'APPLE 앱스토어', 'GAME',  8500, '2026-08-07 22:45:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(38, 1, '넥슨',       'GAME',     95000, '2026-08-08 22:00:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(39, 1, '배달의민족', 'DELIVERY', 48000, '2026-08-10 20:15:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(40, 1, '넥슨',       'GAME',     50000, '2026-08-12 23:10:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(41, 1, '쿠팡이츠',   'DELIVERY', 30000, '2026-08-14 19:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),

-- ── 3) 만족(SATISFIED 5) / 애매(SOSO 3) : 이번달, 비율 자연스럽게 ──
(42, 1, '국군복지단 PX', 'PX',          20000, '2026-08-02 14:00:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(43, 1, '교보문고',      'ETC',         18000, '2026-08-05 16:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(44, 1, '스타벅스',      'ETC',         5800,  '2026-08-06 10:20:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(45, 1, '국군복지단 PX', 'PX',          12000, '2026-08-09 13:15:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(46, 1, 'KTX 서울-부산', 'VACATION',    47000, '2026-08-11 09:40:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(47, 1, 'CU 부대앞점',   'CONVENIENCE', 7000,  '2026-08-03 18:00:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(48, 1, '배달의민족',    'DELIVERY',    21000, '2026-08-07 20:40:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(49, 1, '스타벅스',      'ETC',         6500,  '2026-08-13 15:50:00', NOW(), 'demo_hb', NULL, NULL, 'N'),

-- ── 4) 지난달(2026-07) : 월별 비교 그래프용 4건 ──
(50, 1, '넥슨',          'GAME',     40000, '2026-07-05 22:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(51, 1, '배달의민족',    'DELIVERY', 25000, '2026-07-12 20:10:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(52, 1, '국군복지단 PX', 'PX',       15000, '2026-07-18 13:20:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(53, 1, '스타벅스',      'ETC',      6000,  '2026-07-25 16:00:00', NOW(), 'demo_hb', NULL, NULL, 'N');

-- ── 회고 태깅(spending_review) : 위 spending 22~31(미점호)은 제외, 32~53만 태깅 ──
--  reviewed_at = spent_at 로 맞춤(기존 파일 규칙). FK: 위 spending INSERT 뒤라 정합.
INSERT INTO `spending_review`
(`review_id`, `spending_id`, `review_type`, `reviewed_at`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
-- 캘린더용 REGRET 10건 (spending 32~41)
(20, 32, 'REGRET',    '2026-08-01 23:20:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(21, 33, 'REGRET',    '2026-08-02 20:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(22, 34, 'REGRET',    '2026-08-03 21:40:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(23, 35, 'REGRET',    '2026-08-04 19:50:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(24, 36, 'REGRET',    '2026-08-06 20:25:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(25, 37, 'REGRET',    '2026-08-07 22:45:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(26, 38, 'REGRET',    '2026-08-08 22:00:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(27, 39, 'REGRET',    '2026-08-10 20:15:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(28, 40, 'REGRET',    '2026-08-12 23:10:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(29, 41, 'REGRET',    '2026-08-14 19:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
-- 만족(SATISFIED) 5건 (spending 42~46)
(30, 42, 'SATISFIED', '2026-08-02 14:00:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(31, 43, 'SATISFIED', '2026-08-05 16:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(32, 44, 'SATISFIED', '2026-08-06 10:20:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(33, 45, 'SATISFIED', '2026-08-09 13:15:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(34, 46, 'SATISFIED', '2026-08-11 09:40:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
-- 애매(SOSO) 3건 (spending 47~49)
(35, 47, 'SOSO',      '2026-08-03 18:00:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(36, 48, 'SOSO',      '2026-08-07 20:40:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(37, 49, 'SOSO',      '2026-08-13 15:50:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
-- 지난달(2026-07) 태깅 4건 (spending 50~53)
(38, 50, 'REGRET',    '2026-07-05 22:30:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(39, 51, 'REGRET',    '2026-07-12 20:10:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(40, 52, 'SATISFIED', '2026-07-18 13:20:00', NOW(), 'demo_hb', NULL, NULL, 'N'),
(41, 53, 'SOSO',      '2026-07-25 16:00:00', NOW(), 'demo_hb', NULL, NULL, 'N');

-- [데모추가 건수] spending +32건(id 22~53) / spending_review +22건(id 20~41)
--   · 미점호(리뷰無): 10건(22~31)  · REGRET: 12건(캘린더10 + 7월2)  · SATISFIED: 6건  · SOSO: 4건

-- ── [데모추가2, 08-09] 캘린더 색 더 다양화 + 미점호 넉넉히 20건 (created_nm='demo_cal_*') ──
--  demo_hb 위에 얹어: 8월 8일 다양한 레벨 + 7월(월이동 데모) + 미점호 20건
INSERT INTO `spending`
(`spending_id`, `user_id`, `merchant_name`, `category`, `amount`, `spent_at`,
 `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
-- ① 이번달(2026-08) 후회소비 - 날짜별 레벨 다양
(54, 1, '김밥천국',   'FOOD',        8000,  '2026-08-01 12:30:00', NOW(), 'demo_cal_regret',     NULL, NULL, 'N'),
(55, 1, '무신사',     'SHOPPING',    25000, '2026-08-02 15:10:00', NOW(), 'demo_cal_regret',     NULL, NULL, 'N'),
(56, 1, 'CGV 서면',   'CULTURE',     45000, '2026-08-03 19:00:00', NOW(), 'demo_cal_regret',     NULL, NULL, 'N'),
(57, 1, '배달의민족', 'DELIVERY',    62000, '2026-08-04 20:30:00', NOW(), 'demo_cal_regret',     NULL, NULL, 'N'),
(58, 1, '메가커피',   'CAFE',        6500,  '2026-08-05 09:20:00', NOW(), 'demo_cal_regret',     NULL, NULL, 'N'),
(59, 1, '카카오T',    'TRANSPORT',   18000, '2026-08-06 18:00:00', NOW(), 'demo_cal_regret',     NULL, NULL, 'N'),
(60, 1, '쿠팡',       'SHOPPING',    55000, '2026-08-07 21:00:00', NOW(), 'demo_cal_regret',     NULL, NULL, 'N'),
(61, 1, '요기요',     'DELIVERY',    38000, '2026-08-08 13:40:00', NOW(), 'demo_cal_regret',     NULL, NULL, 'N'),
-- ② 지난달(2026-07) 후회소비 - 월 이동 데모용
(62, 1, '스타벅스',   'CAFE',        28000, '2026-07-02 14:00:00', NOW(), 'demo_cal_regret_jul', NULL, NULL, 'N'),
(63, 1, '편의점 CU',  'CONVENIENCE', 9000,  '2026-07-05 22:10:00', NOW(), 'demo_cal_regret_jul', NULL, NULL, 'N'),
(64, 1, '무신사',     'SHOPPING',    55000, '2026-07-09 16:30:00', NOW(), 'demo_cal_regret_jul', NULL, NULL, 'N'),
(65, 1, '배달의민족', 'DELIVERY',    40000, '2026-07-13 20:00:00', NOW(), 'demo_cal_regret_jul', NULL, NULL, 'N'),
(66, 1, '넷플릭스',   'CULTURE',     15000, '2026-07-16 10:00:00', NOW(), 'demo_cal_regret_jul', NULL, NULL, 'N'),
(67, 1, '쿠팡',       'SHOPPING',    48000, '2026-07-20 19:20:00', NOW(), 'demo_cal_regret_jul', NULL, NULL, 'N'),
(68, 1, '메가커피',   'CAFE',        7000,  '2026-07-24 09:00:00', NOW(), 'demo_cal_regret_jul', NULL, NULL, 'N'),
(69, 1, '요기요',     'DELIVERY',    33000, '2026-07-27 21:30:00', NOW(), 'demo_cal_regret_jul', NULL, NULL, 'N'),
(70, 1, 'CGV 서면',   'CULTURE',     60000, '2026-07-30 18:00:00', NOW(), 'demo_cal_regret_jul', NULL, NULL, 'N'),
-- ③ 미점호 20건 (소비점호 대기, spending_review 없음)
(71, 1, '군마트 PX',  'PX',          8500,  '2026-08-01 17:30:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(72, 1, 'GS25',       'CONVENIENCE', 4800,  '2026-08-01 08:15:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(73, 1, '스타벅스',   'CAFE',        6300,  '2026-08-02 10:00:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(74, 1, '맘스터치',   'FOOD',        11000, '2026-08-02 19:20:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(75, 1, '배달의민족', 'DELIVERY',    23000, '2026-08-03 20:40:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(76, 1, '스팀',       'GAME',        33000, '2026-08-03 23:10:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(77, 1, 'CU',         'CONVENIENCE', 3900,  '2026-08-04 07:50:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(78, 1, '군마트 PX',  'PX',          12500, '2026-08-04 18:00:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(79, 1, '메가커피',   'CAFE',        4500,  '2026-08-05 09:30:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(80, 1, '무신사',     'SHOPPING',    41000, '2026-08-05 15:00:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(81, 1, '카카오T',    'TRANSPORT',   9800,  '2026-08-05 21:00:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(82, 1, '요기요',     'DELIVERY',    19500, '2026-08-06 20:00:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(83, 1, 'GS25',       'CONVENIENCE', 5200,  '2026-08-06 12:30:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(84, 1, '넥슨',       'GAME',        29000, '2026-08-06 22:40:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(85, 1, '김밥천국',   'FOOD',        7500,  '2026-08-07 12:00:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(86, 1, '스타벅스',   'CAFE',        11800, '2026-08-07 16:20:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(87, 1, '쿠팡',       'SHOPPING',    36000, '2026-08-07 21:30:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(88, 1, '군마트 PX',  'PX',          6700,  '2026-08-08 08:40:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(89, 1, 'CGV 서면',   'CULTURE',     15000, '2026-08-08 14:00:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N'),
(90, 1, '배달의민족', 'DELIVERY',    21000, '2026-08-08 20:10:00', NOW(), 'demo_cal_untagged',   NULL, NULL, 'N');

-- ①② 후회소비에 REGRET 리뷰 부여 (review_id 자동, created_nm으로 매칭 / 미점호(③)는 제외)
INSERT INTO `spending_review` (`spending_id`, `review_type`, `reviewed_at`, `created_date`, `created_nm`, `del_yn`)
SELECT s.`spending_id`, 'REGRET', s.`spent_at`, NOW(), 'demo_cal', 'N'
  FROM `spending` s
 WHERE s.`user_id` = 1
   AND s.`created_nm` IN ('demo_cal_regret', 'demo_cal_regret_jul')
   AND s.`del_yn` = 'N'
   AND NOT EXISTS (SELECT 1 FROM `spending_review` r WHERE r.`spending_id` = s.`spending_id` AND r.`del_yn` = 'N');

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
-- user1: 자취는 미등록 상태로 시작 → 시연 때 직접 step1~5로 등록해 정상 로드맵(확정매물 포함) 생성.
--   (기존 데모는 confirmed_listing_id=NULL인 CONFIRMED라 상세가 비어 제거함. 매물은 동적 적재라 고정 연결 불가)
-- user2: 서울 REGION, 월예산 100만, DRAFT(이어보기 데모)
(2, 2, '서울 자취방 찾기', 'REGION', NULL, NULL, 1000000, 'YEAR', 12, 'DRAFT',     NULL, NOW(), 'demo', NULL, NULL, 'N');

INSERT INTO `rent_goal_region`
(`region_id`, `goal_id`, `region_code`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
-- user2 서울 법정동 1개 (user1은 자취 미등록이라 지역 없음)
(3, 2, '1168010100', NOW(), 'demo', NULL, NULL, 'N');

-- [건수 요약] saving_account 4 / saving_history 60 / income 27 (user1 8월분은 시연용 제외) / spending 21 / spending_review 19 / openbanking_link 6 / rent_goal 1 (user2 DRAFT만, user1은 시연 때 직접 등록) / rent_goal_region 1

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
 4, NOW(), 'jotaeseok', 'N');


-- --------------------------------------------------------------------
--  [태석] 여행 / 예상여행경비(travel_cost)
--  테이블: travel_cost
-- --------------------------------------------------------------------
INSERT INTO travel_cost (cost_id, goal_id, flight_cost, hotel_cost, living_cost, total_cost, remaining_budget, created_date, created_nm, del_yn)

VALUES
    (1, 1,  450000, 240000,  90692,  780692,   19308, NOW(), 'jotaeseok', 'N'),
    (2, 2,  380000, 520000, 313288, 1213288,  786712, NOW(), 'jotaeseok', 'N'),
    (3, 3,   87000, 360000, 373497,  627000,  873000, NOW(), 'jotaeseok', 'N');



-- ######################################################################
--  [태석] 저축 비교 페이지 테스트 인원 100명
-- ######################################################################

-- ── 회원 100명 (전원 상병) ──────────────────────────────────
--  군종·부대: 육군 52 / 해군 18 / 공군 20 / 해병대 10, 총 12개 부대
--  복무 8~15개월차로 흩어 놓아 적금 납입 회차가 서로 다릅니다.
--  password 는 자리표시자라 이 계정들로는 로그인할 수 없습니다(통계 모수 전용).
INSERT INTO `user`
(`user_id`, `password`, `name`, `phone`, `type_id`, `rank_id`,
 `unit_name`, `unit_code`, `enlist_date`, `discharge_date`,
 `login_provider`, `status`, `created_date`, `created_nm`, `del_yn`)
VALUES
    ('soldier001@kbthink.com', '$2a$10$socialTestDataPlaceholder', '김민준', '010-7000-0001', 1, 3, '수도방위사령부', 'AC01', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier002@kbthink.com', '$2a$10$socialTestDataPlaceholder', '이민준', '010-7000-0002', 1, 3, '수도방위사령부', 'AC01', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier003@kbthink.com', '$2a$10$socialTestDataPlaceholder', '박민준', '010-7000-0003', 1, 3, '수도방위사령부', 'AC01', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier004@kbthink.com', '$2a$10$socialTestDataPlaceholder', '최민준', '010-7000-0004', 1, 3, '수도방위사령부', 'AC01', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier005@kbthink.com', '$2a$10$socialTestDataPlaceholder', '정민준', '010-7000-0005', 1, 3, '수도방위사령부', 'AC01', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier006@kbthink.com', '$2a$10$socialTestDataPlaceholder', '강민준', '010-7000-0006', 1, 3, '수도방위사령부', 'AC01', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier007@kbthink.com', '$2a$10$socialTestDataPlaceholder', '조민준', '010-7000-0007', 1, 3, '수도방위사령부', 'AC01', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier008@kbthink.com', '$2a$10$socialTestDataPlaceholder', '윤민준', '010-7000-0008', 1, 3, '수도방위사령부', 'AC01', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier009@kbthink.com', '$2a$10$socialTestDataPlaceholder', '장민준', '010-7000-0009', 1, 3, '수도방위사령부', 'AC01', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier010@kbthink.com', '$2a$10$socialTestDataPlaceholder', '임민준', '010-7000-0010', 1, 3, '수도방위사령부', 'AC01', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier011@kbthink.com', '$2a$10$socialTestDataPlaceholder', '한민준', '010-7000-0011', 1, 3, '수도방위사령부', 'AC01', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier012@kbthink.com', '$2a$10$socialTestDataPlaceholder', '오민준', '010-7000-0012', 1, 3, '수도방위사령부', 'AC01', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier013@kbthink.com', '$2a$10$socialTestDataPlaceholder', '서민준', '010-7000-0013', 1, 3, '수도방위사령부', 'AC01', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier014@kbthink.com', '$2a$10$socialTestDataPlaceholder', '신민준', '010-7000-0014', 1, 3, '수도방위사령부', 'AC01', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier015@kbthink.com', '$2a$10$socialTestDataPlaceholder', '권민준', '010-7000-0015', 1, 3, '수도방위사령부', 'AC01', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier016@kbthink.com', '$2a$10$socialTestDataPlaceholder', '황민준', '010-7000-0016', 1, 3, '수도방위사령부', 'AC01', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier017@kbthink.com', '$2a$10$socialTestDataPlaceholder', '안민준', '010-7000-0017', 1, 3, '수도방위사령부', 'AC01', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier018@kbthink.com', '$2a$10$socialTestDataPlaceholder', '송민준', '010-7000-0018', 1, 3, '수도방위사령부', 'AC01', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier019@kbthink.com', '$2a$10$socialTestDataPlaceholder', '류민준', '010-7000-0019', 1, 3, '육군특수전사령부', 'AC02', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier020@kbthink.com', '$2a$10$socialTestDataPlaceholder', '전민준', '010-7000-0020', 1, 3, '육군특수전사령부', 'AC02', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier021@kbthink.com', '$2a$10$socialTestDataPlaceholder', '김서준', '010-7000-0021', 1, 3, '육군특수전사령부', 'AC02', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier022@kbthink.com', '$2a$10$socialTestDataPlaceholder', '이서준', '010-7000-0022', 1, 3, '육군특수전사령부', 'AC02', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier023@kbthink.com', '$2a$10$socialTestDataPlaceholder', '박서준', '010-7000-0023', 1, 3, '육군특수전사령부', 'AC02', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier024@kbthink.com', '$2a$10$socialTestDataPlaceholder', '최서준', '010-7000-0024', 1, 3, '육군특수전사령부', 'AC02', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier025@kbthink.com', '$2a$10$socialTestDataPlaceholder', '정서준', '010-7000-0025', 1, 3, '육군특수전사령부', 'AC02', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier026@kbthink.com', '$2a$10$socialTestDataPlaceholder', '강서준', '010-7000-0026', 1, 3, '육군특수전사령부', 'AC02', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier027@kbthink.com', '$2a$10$socialTestDataPlaceholder', '조서준', '010-7000-0027', 1, 3, '육군특수전사령부', 'AC02', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier028@kbthink.com', '$2a$10$socialTestDataPlaceholder', '윤서준', '010-7000-0028', 1, 3, '육군특수전사령부', 'AC02', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier029@kbthink.com', '$2a$10$socialTestDataPlaceholder', '장서준', '010-7000-0029', 1, 3, '육군특수전사령부', 'AC02', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier030@kbthink.com', '$2a$10$socialTestDataPlaceholder', '임서준', '010-7000-0030', 1, 3, '육군특수전사령부', 'AC02', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier031@kbthink.com', '$2a$10$socialTestDataPlaceholder', '한서준', '010-7000-0031', 1, 3, '육군특수전사령부', 'AC02', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier032@kbthink.com', '$2a$10$socialTestDataPlaceholder', '오서준', '010-7000-0032', 1, 3, '육군특수전사령부', 'AC02', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier033@kbthink.com', '$2a$10$socialTestDataPlaceholder', '서서준', '010-7000-0033', 1, 3, '육군특수전사령부', 'AC02', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier034@kbthink.com', '$2a$10$socialTestDataPlaceholder', '신서준', '010-7000-0034', 1, 3, '육군특수전사령부', 'AC02', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier035@kbthink.com', '$2a$10$socialTestDataPlaceholder', '권서준', '010-7000-0035', 1, 3, '육군특수전사령부', 'AC02', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier036@kbthink.com', '$2a$10$socialTestDataPlaceholder', '황서준', '010-7000-0036', 1, 3, '육군항공사령부', 'AC03', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier037@kbthink.com', '$2a$10$socialTestDataPlaceholder', '안서준', '010-7000-0037', 1, 3, '육군항공사령부', 'AC03', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier038@kbthink.com', '$2a$10$socialTestDataPlaceholder', '송서준', '010-7000-0038', 1, 3, '육군항공사령부', 'AC03', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier039@kbthink.com', '$2a$10$socialTestDataPlaceholder', '류서준', '010-7000-0039', 1, 3, '육군항공사령부', 'AC03', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier040@kbthink.com', '$2a$10$socialTestDataPlaceholder', '전서준', '010-7000-0040', 1, 3, '육군항공사령부', 'AC03', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier041@kbthink.com', '$2a$10$socialTestDataPlaceholder', '김도윤', '010-7000-0041', 1, 3, '육군항공사령부', 'AC03', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier042@kbthink.com', '$2a$10$socialTestDataPlaceholder', '이도윤', '010-7000-0042', 1, 3, '육군항공사령부', 'AC03', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier043@kbthink.com', '$2a$10$socialTestDataPlaceholder', '박도윤', '010-7000-0043', 1, 3, '육군항공사령부', 'AC03', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier044@kbthink.com', '$2a$10$socialTestDataPlaceholder', '최도윤', '010-7000-0044', 1, 3, '육군항공사령부', 'AC03', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier045@kbthink.com', '$2a$10$socialTestDataPlaceholder', '정도윤', '010-7000-0045', 1, 3, '육군항공사령부', 'AC03', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier046@kbthink.com', '$2a$10$socialTestDataPlaceholder', '강도윤', '010-7000-0046', 1, 3, '육군항공사령부', 'AC03', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier047@kbthink.com', '$2a$10$socialTestDataPlaceholder', '조도윤', '010-7000-0047', 1, 3, '육군항공사령부', 'AC03', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier048@kbthink.com', '$2a$10$socialTestDataPlaceholder', '윤도윤', '010-7000-0048', 1, 3, '육군항공사령부', 'AC03', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier049@kbthink.com', '$2a$10$socialTestDataPlaceholder', '장도윤', '010-7000-0049', 1, 3, '육군항공사령부', 'AC03', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier050@kbthink.com', '$2a$10$socialTestDataPlaceholder', '임도윤', '010-7000-0050', 1, 3, '육군항공사령부', 'AC03', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier051@kbthink.com', '$2a$10$socialTestDataPlaceholder', '한도윤', '010-7000-0051', 1, 3, '육군항공사령부', 'AC03', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier052@kbthink.com', '$2a$10$socialTestDataPlaceholder', '오도윤', '010-7000-0052', 1, 3, '육군항공사령부', 'AC03', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier053@kbthink.com', '$2a$10$socialTestDataPlaceholder', '서도윤', '010-7000-0053', 2, 3, '해군작전사령부', 'NC01', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier054@kbthink.com', '$2a$10$socialTestDataPlaceholder', '신도윤', '010-7000-0054', 2, 3, '해군작전사령부', 'NC01', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier055@kbthink.com', '$2a$10$socialTestDataPlaceholder', '권도윤', '010-7000-0055', 2, 3, '해군작전사령부', 'NC01', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier056@kbthink.com', '$2a$10$socialTestDataPlaceholder', '황도윤', '010-7000-0056', 2, 3, '해군작전사령부', 'NC01', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier057@kbthink.com', '$2a$10$socialTestDataPlaceholder', '안도윤', '010-7000-0057', 2, 3, '해군작전사령부', 'NC01', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier058@kbthink.com', '$2a$10$socialTestDataPlaceholder', '송도윤', '010-7000-0058', 2, 3, '해군작전사령부', 'NC01', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier059@kbthink.com', '$2a$10$socialTestDataPlaceholder', '류도윤', '010-7000-0059', 2, 3, '해군항공사령부', 'NC02', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier060@kbthink.com', '$2a$10$socialTestDataPlaceholder', '전도윤', '010-7000-0060', 2, 3, '해군항공사령부', 'NC02', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier061@kbthink.com', '$2a$10$socialTestDataPlaceholder', '김예준', '010-7000-0061', 2, 3, '해군항공사령부', 'NC02', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier062@kbthink.com', '$2a$10$socialTestDataPlaceholder', '이예준', '010-7000-0062', 2, 3, '해군항공사령부', 'NC02', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier063@kbthink.com', '$2a$10$socialTestDataPlaceholder', '박예준', '010-7000-0063', 2, 3, '해군항공사령부', 'NC02', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier064@kbthink.com', '$2a$10$socialTestDataPlaceholder', '최예준', '010-7000-0064', 2, 3, '해군항공사령부', 'NC02', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier065@kbthink.com', '$2a$10$socialTestDataPlaceholder', '정예준', '010-7000-0065', 2, 3, '진해해양사령부', 'NC03', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier066@kbthink.com', '$2a$10$socialTestDataPlaceholder', '강예준', '010-7000-0066', 2, 3, '진해해양사령부', 'NC03', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier067@kbthink.com', '$2a$10$socialTestDataPlaceholder', '조예준', '010-7000-0067', 2, 3, '진해해양사령부', 'NC03', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier068@kbthink.com', '$2a$10$socialTestDataPlaceholder', '윤예준', '010-7000-0068', 2, 3, '진해해양사령부', 'NC03', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier069@kbthink.com', '$2a$10$socialTestDataPlaceholder', '장예준', '010-7000-0069', 2, 3, '진해해양사령부', 'NC03', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier070@kbthink.com', '$2a$10$socialTestDataPlaceholder', '임예준', '010-7000-0070', 2, 3, '진해해양사령부', 'NC03', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier071@kbthink.com', '$2a$10$socialTestDataPlaceholder', '한예준', '010-7000-0071', 3, 3, '공군작전사령부', 'FC01', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier072@kbthink.com', '$2a$10$socialTestDataPlaceholder', '오예준', '010-7000-0072', 3, 3, '공군작전사령부', 'FC01', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier073@kbthink.com', '$2a$10$socialTestDataPlaceholder', '서예준', '010-7000-0073', 3, 3, '공군작전사령부', 'FC01', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier074@kbthink.com', '$2a$10$socialTestDataPlaceholder', '신예준', '010-7000-0074', 3, 3, '공군작전사령부', 'FC01', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier075@kbthink.com', '$2a$10$socialTestDataPlaceholder', '권예준', '010-7000-0075', 3, 3, '공군작전사령부', 'FC01', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier076@kbthink.com', '$2a$10$socialTestDataPlaceholder', '황예준', '010-7000-0076', 3, 3, '공군작전사령부', 'FC01', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier077@kbthink.com', '$2a$10$socialTestDataPlaceholder', '안예준', '010-7000-0077', 3, 3, '공군작전사령부', 'FC01', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier078@kbthink.com', '$2a$10$socialTestDataPlaceholder', '송예준', '010-7000-0078', 3, 3, '공중전투사령부', 'FC02', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier079@kbthink.com', '$2a$10$socialTestDataPlaceholder', '류예준', '010-7000-0079', 3, 3, '공중전투사령부', 'FC02', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier080@kbthink.com', '$2a$10$socialTestDataPlaceholder', '전예준', '010-7000-0080', 3, 3, '공중전투사령부', 'FC02', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier081@kbthink.com', '$2a$10$socialTestDataPlaceholder', '김시우', '010-7000-0081', 3, 3, '공중전투사령부', 'FC02', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier082@kbthink.com', '$2a$10$socialTestDataPlaceholder', '이시우', '010-7000-0082', 3, 3, '공중전투사령부', 'FC02', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier083@kbthink.com', '$2a$10$socialTestDataPlaceholder', '박시우', '010-7000-0083', 3, 3, '공중전투사령부', 'FC02', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier084@kbthink.com', '$2a$10$socialTestDataPlaceholder', '최시우', '010-7000-0084', 3, 3, '공중전투사령부', 'FC02', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier085@kbthink.com', '$2a$10$socialTestDataPlaceholder', '정시우', '010-7000-0085', 3, 3, '공군미사일방어사령부', 'FC03', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier086@kbthink.com', '$2a$10$socialTestDataPlaceholder', '강시우', '010-7000-0086', 3, 3, '공군미사일방어사령부', 'FC03', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier087@kbthink.com', '$2a$10$socialTestDataPlaceholder', '조시우', '010-7000-0087', 3, 3, '공군미사일방어사령부', 'FC03', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier088@kbthink.com', '$2a$10$socialTestDataPlaceholder', '윤시우', '010-7000-0088', 3, 3, '공군미사일방어사령부', 'FC03', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier089@kbthink.com', '$2a$10$socialTestDataPlaceholder', '장시우', '010-7000-0089', 3, 3, '공군미사일방어사령부', 'FC03', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier090@kbthink.com', '$2a$10$socialTestDataPlaceholder', '임시우', '010-7000-0090', 3, 3, '공군미사일방어사령부', 'FC03', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier091@kbthink.com', '$2a$10$socialTestDataPlaceholder', '한시우', '010-7000-0091', 4, 3, '제6해병여단', 'MB06', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier092@kbthink.com', '$2a$10$socialTestDataPlaceholder', '오시우', '010-7000-0092', 4, 3, '제6해병여단', 'MB06', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier093@kbthink.com', '$2a$10$socialTestDataPlaceholder', '서시우', '010-7000-0093', 4, 3, '제6해병여단', 'MB06', '2025-08-06', '2027-02-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier094@kbthink.com', '$2a$10$socialTestDataPlaceholder', '신시우', '010-7000-0094', 4, 3, '제6해병여단', 'MB06', '2025-07-06', '2027-01-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier095@kbthink.com', '$2a$10$socialTestDataPlaceholder', '권시우', '010-7000-0095', 4, 3, '제9해병여단', 'MB09', '2025-06-06', '2026-12-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier096@kbthink.com', '$2a$10$socialTestDataPlaceholder', '황시우', '010-7000-0096', 4, 3, '제9해병여단', 'MB09', '2025-05-06', '2026-11-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier097@kbthink.com', '$2a$10$socialTestDataPlaceholder', '안시우', '010-7000-0097', 4, 3, '제9해병여단', 'MB09', '2025-12-06', '2027-06-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier098@kbthink.com', '$2a$10$socialTestDataPlaceholder', '송시우', '010-7000-0098', 4, 3, '수도군단', 'MB90', '2025-11-06', '2027-05-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier099@kbthink.com', '$2a$10$socialTestDataPlaceholder', '류시우', '010-7000-0099', 4, 3, '수도군단', 'MB90', '2025-10-06', '2027-04-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N'),
    ('soldier100@kbthink.com', '$2a$10$socialTestDataPlaceholder', '전시우', '010-7000-0100', 4, 3, '수도군단', 'MB90', '2025-09-06', '2027-03-06', 'local', 'ACTIVE', NOW(), 'TEST-SOCIAL', 'N');

-- ── 군적금 계좌 100건 ───────────────────────────────────────
--  월 납입액 540,000 ~ 100,000 (상병 월급 1,200,000 대비 45.0% ~ 8.3%)
--  상위 88명은 5,000원 간격으로 전부 다른 금액,
--  하위 12명은 최소 납입액 100,000 에 몰려 동점 처리도 함께 검증됩니다.
--  curr_amount = monthly_save x monthly_count 로 정합성을 맞췄습니다.
INSERT INTO `saving_account`
(`user_id`, `bank_code`, `monthly_save`, `monthly_count`, `curr_amount`,
 `account_status`, `created_date`, `created_nm`, `del_yn`)
SELECT
    member.id, source.bank_code, source.monthly_save, source.monthly_count,
    source.curr_amount, 'ACTIVE', source.opened_at, 'TEST-SOCIAL', 'N'
FROM (
         SELECT 'soldier001@kbthink.com' AS email, '004' AS bank_code, 540000 AS monthly_save, 6 AS monthly_count, 3240000 AS curr_amount, '2026-01-06 09:00:00' AS opened_at
         UNION ALL SELECT 'soldier002@kbthink.com', '003', 535000, 7, 3745000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier003@kbthink.com', '088', 530000, 8, 4240000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier004@kbthink.com', '011', 525000, 9, 4725000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier005@kbthink.com', '004', 520000, 10, 5200000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier006@kbthink.com', '003', 515000, 11, 5665000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier007@kbthink.com', '088', 510000, 12, 6120000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier008@kbthink.com', '011', 505000, 13, 6565000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier009@kbthink.com', '004', 500000, 6, 3000000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier010@kbthink.com', '003', 495000, 7, 3465000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier011@kbthink.com', '088', 490000, 8, 3920000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier012@kbthink.com', '011', 485000, 9, 4365000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier013@kbthink.com', '004', 480000, 10, 4800000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier014@kbthink.com', '003', 475000, 11, 5225000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier015@kbthink.com', '088', 470000, 12, 5640000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier016@kbthink.com', '011', 465000, 13, 6045000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier017@kbthink.com', '004', 460000, 6, 2760000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier018@kbthink.com', '003', 455000, 7, 3185000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier019@kbthink.com', '088', 450000, 8, 3600000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier020@kbthink.com', '011', 445000, 9, 4005000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier021@kbthink.com', '004', 440000, 10, 4400000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier022@kbthink.com', '003', 435000, 11, 4785000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier023@kbthink.com', '088', 430000, 12, 5160000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier024@kbthink.com', '011', 425000, 13, 5525000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier025@kbthink.com', '004', 420000, 6, 2520000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier026@kbthink.com', '003', 415000, 7, 2905000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier027@kbthink.com', '088', 410000, 8, 3280000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier028@kbthink.com', '011', 405000, 9, 3645000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier029@kbthink.com', '004', 400000, 10, 4000000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier030@kbthink.com', '003', 395000, 11, 4345000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier031@kbthink.com', '088', 390000, 12, 4680000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier032@kbthink.com', '011', 385000, 13, 5005000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier033@kbthink.com', '004', 380000, 6, 2280000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier034@kbthink.com', '003', 375000, 7, 2625000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier035@kbthink.com', '088', 370000, 8, 2960000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier036@kbthink.com', '011', 365000, 9, 3285000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier037@kbthink.com', '004', 360000, 10, 3600000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier038@kbthink.com', '003', 355000, 11, 3905000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier039@kbthink.com', '088', 350000, 12, 4200000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier040@kbthink.com', '011', 345000, 13, 4485000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier041@kbthink.com', '004', 340000, 6, 2040000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier042@kbthink.com', '003', 335000, 7, 2345000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier043@kbthink.com', '088', 330000, 8, 2640000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier044@kbthink.com', '011', 325000, 9, 2925000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier045@kbthink.com', '004', 320000, 10, 3200000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier046@kbthink.com', '003', 315000, 11, 3465000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier047@kbthink.com', '088', 310000, 12, 3720000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier048@kbthink.com', '011', 305000, 13, 3965000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier049@kbthink.com', '004', 300000, 6, 1800000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier050@kbthink.com', '003', 295000, 7, 2065000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier051@kbthink.com', '088', 290000, 8, 2320000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier052@kbthink.com', '011', 285000, 9, 2565000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier053@kbthink.com', '004', 280000, 10, 2800000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier054@kbthink.com', '003', 275000, 11, 3025000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier055@kbthink.com', '088', 270000, 12, 3240000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier056@kbthink.com', '011', 265000, 13, 3445000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier057@kbthink.com', '004', 260000, 6, 1560000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier058@kbthink.com', '003', 255000, 7, 1785000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier059@kbthink.com', '088', 250000, 8, 2000000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier060@kbthink.com', '011', 245000, 9, 2205000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier061@kbthink.com', '004', 240000, 10, 2400000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier062@kbthink.com', '003', 235000, 11, 2585000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier063@kbthink.com', '088', 230000, 12, 2760000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier064@kbthink.com', '011', 225000, 13, 2925000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier065@kbthink.com', '004', 220000, 6, 1320000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier066@kbthink.com', '003', 215000, 7, 1505000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier067@kbthink.com', '088', 210000, 8, 1680000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier068@kbthink.com', '011', 205000, 9, 1845000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier069@kbthink.com', '004', 200000, 10, 2000000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier070@kbthink.com', '003', 195000, 11, 2145000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier071@kbthink.com', '088', 190000, 12, 2280000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier072@kbthink.com', '011', 185000, 13, 2405000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier073@kbthink.com', '004', 180000, 6, 1080000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier074@kbthink.com', '003', 175000, 7, 1225000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier075@kbthink.com', '088', 170000, 8, 1360000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier076@kbthink.com', '011', 165000, 9, 1485000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier077@kbthink.com', '004', 160000, 10, 1600000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier078@kbthink.com', '003', 155000, 11, 1705000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier079@kbthink.com', '088', 150000, 12, 1800000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier080@kbthink.com', '011', 145000, 13, 1885000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier081@kbthink.com', '004', 140000, 6, 840000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier082@kbthink.com', '003', 135000, 7, 945000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier083@kbthink.com', '088', 130000, 8, 1040000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier084@kbthink.com', '011', 125000, 9, 1125000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier085@kbthink.com', '004', 120000, 10, 1200000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier086@kbthink.com', '003', 115000, 11, 1265000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier087@kbthink.com', '088', 110000, 12, 1320000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier088@kbthink.com', '011', 105000, 13, 1365000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier089@kbthink.com', '004', 100000, 6, 600000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier090@kbthink.com', '003', 100000, 7, 700000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier091@kbthink.com', '088', 100000, 8, 800000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier092@kbthink.com', '011', 100000, 9, 900000, '2025-10-06 09:00:00'
         UNION ALL SELECT 'soldier093@kbthink.com', '004', 100000, 10, 1000000, '2025-09-06 09:00:00'
         UNION ALL SELECT 'soldier094@kbthink.com', '003', 100000, 11, 1100000, '2025-08-06 09:00:00'
         UNION ALL SELECT 'soldier095@kbthink.com', '088', 100000, 12, 1200000, '2025-07-06 09:00:00'
         UNION ALL SELECT 'soldier096@kbthink.com', '011', 100000, 13, 1300000, '2025-06-06 09:00:00'
         UNION ALL SELECT 'soldier097@kbthink.com', '004', 100000, 6, 600000, '2026-01-06 09:00:00'
         UNION ALL SELECT 'soldier098@kbthink.com', '003', 100000, 7, 700000, '2025-12-06 09:00:00'
         UNION ALL SELECT 'soldier099@kbthink.com', '088', 100000, 8, 800000, '2025-11-06 09:00:00'
         UNION ALL SELECT 'soldier100@kbthink.com', '011', 100000, 9, 900000, '2025-10-06 09:00:00'
     ) source
         JOIN `user` member ON member.user_id = source.email;

-- ── 로드맵 목표 91건 ────────────────────────────────────────
--  관심도 차트 분포용. 회원 1명당 1건, 전부 CONFIRMED.
--  여행 36 / 진로 25 / 자동차 18 / 자취 12

INSERT INTO `travel_goal`
(`user_id`, `title`, `departure`, `destination`, `is_domestic`, `style`,
 `start_date`, `end_date`, `total_budget`, `status`,
 `created_date`, `created_nm`, `del_yn`)
SELECT
    member.id, '전역 후 여행', '서울', source.destination, source.is_domestic,
    'common', '2027-03-05', '2027-03-08', source.total_budget, 'CONFIRMED',
    NOW(), 'TEST-SOCIAL', 'N'
FROM (
         SELECT 'soldier001@kbthink.com' AS email, '부산' AS destination, 1 AS is_domestic, 800000 AS total_budget
         UNION ALL SELECT 'soldier002@kbthink.com', '강릉', 1, 900000
         UNION ALL SELECT 'soldier003@kbthink.com', '제주', 1, 1000000
         UNION ALL SELECT 'soldier004@kbthink.com', '오사카', 0, 1100000
         UNION ALL SELECT 'soldier005@kbthink.com', '다낭', 0, 1200000
         UNION ALL SELECT 'soldier006@kbthink.com', '부산', 1, 800000
         UNION ALL SELECT 'soldier007@kbthink.com', '강릉', 1, 900000
         UNION ALL SELECT 'soldier008@kbthink.com', '제주', 1, 1000000
         UNION ALL SELECT 'soldier009@kbthink.com', '오사카', 0, 1100000
         UNION ALL SELECT 'soldier010@kbthink.com', '다낭', 0, 1200000
         UNION ALL SELECT 'soldier011@kbthink.com', '부산', 1, 800000
         UNION ALL SELECT 'soldier012@kbthink.com', '강릉', 1, 900000
         UNION ALL SELECT 'soldier013@kbthink.com', '제주', 1, 1000000
         UNION ALL SELECT 'soldier014@kbthink.com', '오사카', 0, 1100000
         UNION ALL SELECT 'soldier015@kbthink.com', '다낭', 0, 1200000
         UNION ALL SELECT 'soldier016@kbthink.com', '부산', 1, 800000
         UNION ALL SELECT 'soldier017@kbthink.com', '강릉', 1, 900000
         UNION ALL SELECT 'soldier018@kbthink.com', '제주', 1, 1000000
         UNION ALL SELECT 'soldier019@kbthink.com', '오사카', 0, 1100000
         UNION ALL SELECT 'soldier020@kbthink.com', '다낭', 0, 1200000
         UNION ALL SELECT 'soldier021@kbthink.com', '부산', 1, 800000
         UNION ALL SELECT 'soldier022@kbthink.com', '강릉', 1, 900000
         UNION ALL SELECT 'soldier023@kbthink.com', '제주', 1, 1000000
         UNION ALL SELECT 'soldier024@kbthink.com', '오사카', 0, 1100000
         UNION ALL SELECT 'soldier025@kbthink.com', '다낭', 0, 1200000
         UNION ALL SELECT 'soldier026@kbthink.com', '부산', 1, 800000
         UNION ALL SELECT 'soldier027@kbthink.com', '강릉', 1, 900000
         UNION ALL SELECT 'soldier028@kbthink.com', '제주', 1, 1000000
         UNION ALL SELECT 'soldier029@kbthink.com', '오사카', 0, 1100000
         UNION ALL SELECT 'soldier030@kbthink.com', '다낭', 0, 1200000
         UNION ALL SELECT 'soldier031@kbthink.com', '부산', 1, 800000
         UNION ALL SELECT 'soldier032@kbthink.com', '강릉', 1, 900000
         UNION ALL SELECT 'soldier033@kbthink.com', '제주', 1, 1000000
         UNION ALL SELECT 'soldier034@kbthink.com', '오사카', 0, 1100000
         UNION ALL SELECT 'soldier035@kbthink.com', '다낭', 0, 1200000
         UNION ALL SELECT 'soldier036@kbthink.com', '부산', 1, 800000
         UNION ALL SELECT 'soldier037@kbthink.com', '강릉', 1, 900000
         UNION ALL SELECT 'soldier038@kbthink.com', '제주', 1, 1000000
         UNION ALL SELECT 'soldier039@kbthink.com', '오사카', 0, 1100000
         UNION ALL SELECT 'soldier040@kbthink.com', '다낭', 0, 1200000
         UNION ALL SELECT 'soldier041@kbthink.com', '부산', 1, 800000
         UNION ALL SELECT 'soldier042@kbthink.com', '강릉', 1, 900000
         UNION ALL SELECT 'soldier043@kbthink.com', '제주', 1, 1000000
         UNION ALL SELECT 'soldier044@kbthink.com', '오사카', 0, 1100000
         UNION ALL SELECT 'soldier045@kbthink.com', '다낭', 0, 1200000
     ) source
         JOIN `user` member ON member.user_id = source.email
         JOIN `city_cost` city
           ON city.city = source.destination
          AND city.del_yn = 'N';

INSERT INTO `job_goal`
(`user_id`, `goal_type`, `expected_date`, `status`,
 `created_date`, `created_nm`, `del_yn`)
SELECT
    member.id, source.goal_type, '2027-09', 'CONFIRMED',
    NOW(), 'TEST-SOCIAL', 'N'
FROM (
         SELECT 'soldier046@kbthink.com' AS email, 'J01' AS goal_type
         UNION ALL SELECT 'soldier047@kbthink.com', 'J02'
         UNION ALL SELECT 'soldier048@kbthink.com', 'J03'
         UNION ALL SELECT 'soldier049@kbthink.com', 'J01'
         UNION ALL SELECT 'soldier050@kbthink.com', 'J02'
         UNION ALL SELECT 'soldier051@kbthink.com', 'J03'
         UNION ALL SELECT 'soldier052@kbthink.com', 'J01'
         UNION ALL SELECT 'soldier053@kbthink.com', 'J02'
         UNION ALL SELECT 'soldier054@kbthink.com', 'J03'
         UNION ALL SELECT 'soldier055@kbthink.com', 'J01'
         UNION ALL SELECT 'soldier056@kbthink.com', 'J02'
         UNION ALL SELECT 'soldier057@kbthink.com', 'J03'
         UNION ALL SELECT 'soldier058@kbthink.com', 'J01'
         UNION ALL SELECT 'soldier059@kbthink.com', 'J02'
         UNION ALL SELECT 'soldier060@kbthink.com', 'J03'
         UNION ALL SELECT 'soldier061@kbthink.com', 'J01'
         UNION ALL SELECT 'soldier062@kbthink.com', 'J02'
         UNION ALL SELECT 'soldier063@kbthink.com', 'J03'
         UNION ALL SELECT 'soldier064@kbthink.com', 'J01'
         UNION ALL SELECT 'soldier065@kbthink.com', 'J02'
         UNION ALL SELECT 'soldier066@kbthink.com', 'J03'
         UNION ALL SELECT 'soldier067@kbthink.com', 'J01'
         UNION ALL SELECT 'soldier068@kbthink.com', 'J02'
         UNION ALL SELECT 'soldier069@kbthink.com', 'J03'
         UNION ALL SELECT 'soldier070@kbthink.com', 'J01'
     ) source
         JOIN `user` member ON member.user_id = source.email;

INSERT INTO `car_goal`
(`user_id`, `budget`, `is_new`, `experience_years`,
 `target_date`, `region`, `status`, `created_date`, `created_nm`, `del_yn`)
SELECT
    member.id, source.budget, source.is_new, source.experience_years,
    '2027-10-01', '서울', 'CONFIRMED', NOW(), 'TEST-SOCIAL', 'N'
FROM (
         SELECT 'soldier071@kbthink.com' AS email, 12000000 AS budget, 0 AS is_new, 1 AS experience_years
         UNION ALL SELECT 'soldier072@kbthink.com', 15000000, 1, 2
         UNION ALL SELECT 'soldier073@kbthink.com', 18000000, 0, 3
         UNION ALL SELECT 'soldier074@kbthink.com', 21000000, 1, 1
         UNION ALL SELECT 'soldier075@kbthink.com', 12000000, 0, 2
         UNION ALL SELECT 'soldier076@kbthink.com', 15000000, 1, 3
         UNION ALL SELECT 'soldier077@kbthink.com', 18000000, 0, 1
         UNION ALL SELECT 'soldier078@kbthink.com', 21000000, 1, 2
         UNION ALL SELECT 'soldier079@kbthink.com', 12000000, 0, 3
         UNION ALL SELECT 'soldier080@kbthink.com', 15000000, 1, 1
         UNION ALL SELECT 'soldier081@kbthink.com', 18000000, 0, 2
         UNION ALL SELECT 'soldier082@kbthink.com', 21000000, 1, 3
         UNION ALL SELECT 'soldier083@kbthink.com', 12000000, 0, 1
         UNION ALL SELECT 'soldier084@kbthink.com', 15000000, 1, 2
         UNION ALL SELECT 'soldier085@kbthink.com', 18000000, 0, 3
         UNION ALL SELECT 'soldier086@kbthink.com', 21000000, 1, 1
         UNION ALL SELECT 'soldier087@kbthink.com', 12000000, 0, 2
         UNION ALL SELECT 'soldier088@kbthink.com', 15000000, 1, 3
     ) source
         JOIN `user` member ON member.user_id = source.email;

INSERT INTO `rent_goal`
(`user_id`, `title`, `selection_mode`, `monthly_budget`,
 `residence_preset`, `residence_months`, `status`,
 `created_date`, `created_nm`, `del_yn`)
SELECT
    member.id, '전역 후 자취', 'REGION', source.monthly_budget,
    source.residence_preset, source.residence_months, 'CONFIRMED',
    NOW(), 'TEST-SOCIAL', 'N'
FROM (
         SELECT 'soldier089@kbthink.com' AS email, 500000 AS monthly_budget, 'SEMESTER' AS residence_preset, 6 AS residence_months
         UNION ALL SELECT 'soldier090@kbthink.com', 550000, 'YEAR', 12
         UNION ALL SELECT 'soldier091@kbthink.com', 600000, 'GRADUATE', 24
         UNION ALL SELECT 'soldier092@kbthink.com', 650000, 'SEMESTER', 6
         UNION ALL SELECT 'soldier093@kbthink.com', 500000, 'YEAR', 12
         UNION ALL SELECT 'soldier094@kbthink.com', 550000, 'GRADUATE', 24
         UNION ALL SELECT 'soldier095@kbthink.com', 600000, 'SEMESTER', 6
         UNION ALL SELECT 'soldier096@kbthink.com', 650000, 'YEAR', 12
         UNION ALL SELECT 'soldier097@kbthink.com', 500000, 'GRADUATE', 24
         UNION ALL SELECT 'soldier098@kbthink.com', 550000, 'SEMESTER', 6
         UNION ALL SELECT 'soldier099@kbthink.com', 600000, 'YEAR', 12
         UNION ALL SELECT 'soldier100@kbthink.com', 650000, 'GRADUATE', 24
     ) source
         JOIN `user` member ON member.user_id = source.email;


SET FOREIGN_KEY_CHECKS = 1;
