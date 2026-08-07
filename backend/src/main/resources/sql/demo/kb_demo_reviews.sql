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




SET FOREIGN_KEY_CHECKS = 1;
