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

SET FOREIGN_KEY_CHECKS = 1;
