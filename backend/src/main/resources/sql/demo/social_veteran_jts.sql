-- =====================================================================
-- social_veteran_jts.sql
-- 전역자 저축 비교 페이지 통계 검증용 데이터 100명
--
-- 구성
--   - 전역자 회원 100명: 로그인용 전역자 부대를 포함한 4개 군종/13개 부대에 순환 배치
--   - 군적금 계좌 100개: 만기 25개, 중도해지 75개
--   - 실제 납입 이력: 계좌별 납입회차만큼 생성
--   - 로드맵 관심도: 여행 40명, 진로 30명, 자동차 20명, 자취 10명
--
-- 재실행 시 동일 이메일, 계좌, 납입회차는 중복 생성하지 않는다.
-- 이 계정들은 통계 모수 전용이며 자리표시자 비밀번호로 로그인할 수 없다.
-- =====================================================================

DROP TEMPORARY TABLE IF EXISTS tmp_social_veteran_unit;

CREATE TEMPORARY TABLE tmp_social_veteran_unit (
    unit_order INT PRIMARY KEY,
    type_id INT NOT NULL,
    unit_name VARCHAR(50) NOT NULL,
    unit_code VARCHAR(20) NOT NULL,
    matured_limit INT NOT NULL
);

INSERT INTO tmp_social_veteran_unit
    (unit_order, type_id, unit_name, unit_code, matured_limit)
VALUES
    (1, 1, '제9보병사단', 'AD09', 1),
    (2, 1, '수도방위사령부', 'AC01', 2),
    (3, 1, '육군특수전사령부', 'AC02', 3),
    (4, 1, '육군항공사령부', 'AC03', 4),
    (5, 2, '해군작전사령부', 'NC01', 5),
    (6, 2, '해군항공사령부', 'NC02', 3),
    (7, 2, '진해해양사령부', 'NC03', 2),
    (8, 3, '공군작전사령부', 'FC01', 1),
    (9, 3, '공중전투사령부', 'FC02', 1),
    (10, 3, '공군미사일방어사령부', 'FC03', 1),
    (11, 4, '제6해병여단', 'MB06', 1),
    (12, 4, '제9해병여단', 'MB09', 1),
    (13, 4, '수도군단', 'MB90', 0);

-- ── 전역자 회원 100명 ─────────────────────────────────────────
INSERT INTO `user`
(`user_id`, `password`, `name`, `phone`, `type_id`, `rank_id`,
 `unit_name`, `unit_code`, `enlist_date`, `discharge_date`,
 `login_provider`, `status`, `created_date`, `created_nm`, `del_yn`)
WITH RECURSIVE veteran_sequence AS (
    SELECT 1 AS sequence_no
    UNION ALL
    SELECT sequence_no + 1
    FROM veteran_sequence
    WHERE sequence_no < 100
)
SELECT
    CONCAT('veteran', LPAD(sequence.sequence_no, 3, '0'), '@kbthink.com'),
    '$2a$10$socialVeteranDataPlaceholder',
    CONCAT('전역테스트', LPAD(sequence.sequence_no, 3, '0')),
    CONCAT('010-7100-', LPAD(sequence.sequence_no, 4, '0')),
    unit.type_id,
    4,
    unit.unit_name,
    unit.unit_code,
    '2023-12-01',
    '2025-06-30',
    'local',
    'ACTIVE',
    NOW(),
    'TEST-SOCIAL-VETERAN',
    'N'
FROM veteran_sequence sequence
JOIN tmp_social_veteran_unit unit
  ON unit.unit_order = MOD(sequence.sequence_no - 1, 13) + 1
WHERE NOT EXISTS (
      SELECT 1
      FROM `user` existing_member
      WHERE existing_member.user_id = CONCAT(
          'veteran', LPAD(sequence.sequence_no, 3, '0'), '@kbthink.com')
  );

-- 시드를 다시 실행해도 최신 부대 분포가 기존 테스트 회원에게 반영되게 한다.
UPDATE `user` member
JOIN tmp_social_veteran_unit unit
  ON unit.unit_order = MOD(
      CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1,
      13
  ) + 1
SET member.type_id = unit.type_id,
    member.rank_id = 4,
    member.unit_name = unit.unit_name,
    member.unit_code = unit.unit_code,
    member.enlist_date = '2023-12-01',
    member.discharge_date = '2025-06-30',
    member.modified_date = NOW(),
    member.modified_nm = 'TEST-SOCIAL-VETERAN'
WHERE member.created_nm = 'TEST-SOCIAL-VETERAN'
  AND member.user_id LIKE 'veteran%@kbthink.com'
  AND member.del_yn = 'N';

-- ── 전역자 군적금 계좌 100개 ───────────────────────────────────
-- 부대별 만기 인원을 다르게 배치한 25명은 만기, 나머지 75명은 5~17회 납입 후 중도해지.
-- 월 납입액은 15만~55만원으로 분산한다.
INSERT INTO saving_account
(`user_id`, `bank_code`, `monthly_save`, `monthly_count`, `curr_amount`,
 `account_status`, `open_date`, `created_date`, `created_nm`, `del_yn`)
SELECT
    source.user_id,
    CASE MOD(source.sequence_no - 1, 4)
        WHEN 0 THEN '004'
        WHEN 1 THEN '003'
        WHEN 2 THEN '088'
        ELSE '011'
    END,
    source.monthly_save,
    source.monthly_count,
    source.monthly_save * source.monthly_count,
    CASE
        WHEN source.cycle_no <= source.matured_limit THEN 'MATURED'
        ELSE 'TERMINATED'
    END,
    '2024-01-01',
    NOW(),
    'TEST-SOCIAL-VETERAN',
    'N'
FROM (
    SELECT
        member.id AS user_id,
        CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) AS sequence_no,
        150000
            + MOD(CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1, 9)
              * 50000 AS monthly_save,
        CASE
            WHEN FLOOR(
                (CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1) / 13
            ) + 1 <= unit.matured_limit
                THEN 18
            ELSE 5
                + MOD(CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1, 13)
        END AS monthly_count,
        FLOOR(
            (CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1) / 13
        ) + 1 AS cycle_no,
        unit.matured_limit
    FROM `user` member
    JOIN tmp_social_veteran_unit unit
      ON unit.unit_order = MOD(
          CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1,
          13
      ) + 1
    WHERE member.created_nm = 'TEST-SOCIAL-VETERAN'
      AND member.user_id LIKE 'veteran%@kbthink.com'
      AND member.del_yn = 'N'
) source
WHERE NOT EXISTS (
    SELECT 1
    FROM saving_account existing_account
    WHERE existing_account.user_id = source.user_id
      AND existing_account.created_nm = 'TEST-SOCIAL-VETERAN'
);

-- 재실행 시에도 계좌 값과 상태를 위 분포로 맞춘다.
UPDATE saving_account account
JOIN `user` member ON member.id = account.user_id
JOIN tmp_social_veteran_unit unit
  ON unit.unit_order = MOD(
      CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1,
      13
  ) + 1
SET account.monthly_save = 150000
        + MOD(CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1, 9)
          * 50000,
    account.monthly_count = CASE
        WHEN FLOOR(
            (CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1) / 13
        ) + 1 <= unit.matured_limit THEN 18
        ELSE 5
            + MOD(CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1, 13)
    END,
    account.account_status = CASE
        WHEN FLOOR(
            (CAST(SUBSTRING(member.user_id, 8, 3) AS UNSIGNED) - 1) / 13
        ) + 1 <= unit.matured_limit THEN 'MATURED'
        ELSE 'TERMINATED'
    END,
    account.open_date = '2024-01-01',
    account.modified_date = NOW(),
    account.modified_nm = 'TEST-SOCIAL-VETERAN'
WHERE account.created_nm = 'TEST-SOCIAL-VETERAN'
  AND member.created_nm = 'TEST-SOCIAL-VETERAN';

UPDATE saving_account account
SET account.curr_amount = account.monthly_save * account.monthly_count
WHERE account.created_nm = 'TEST-SOCIAL-VETERAN';

-- 납입회차가 줄어든 경우 이전 실행에서 생성된 초과 이력만 정리한다.
DELETE history
FROM saving_history history
JOIN saving_account account ON account.account_id = history.account_id
WHERE history.created_nm = 'TEST-SOCIAL-VETERAN'
  AND account.created_nm = 'TEST-SOCIAL-VETERAN'
  AND history.pay_round > account.monthly_count;

-- ── 계좌별 실제 납입 이력 ─────────────────────────────────────
INSERT INTO saving_history
(`account_id`, `pay_round`, `pay_amount`, `paid_date`,
 `created_date`, `created_nm`, `del_yn`)
SELECT
    account.account_id,
    payment_round.pay_round,
    account.monthly_save,
    DATE_ADD(account.open_date, INTERVAL (payment_round.pay_round - 1) MONTH),
    NOW(),
    'TEST-SOCIAL-VETERAN',
    'N'
FROM saving_account account
JOIN (
    SELECT 1 AS pay_round
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
    UNION ALL SELECT 5
    UNION ALL SELECT 6
    UNION ALL SELECT 7
    UNION ALL SELECT 8
    UNION ALL SELECT 9
    UNION ALL SELECT 10
    UNION ALL SELECT 11
    UNION ALL SELECT 12
    UNION ALL SELECT 13
    UNION ALL SELECT 14
    UNION ALL SELECT 15
    UNION ALL SELECT 16
    UNION ALL SELECT 17
    UNION ALL SELECT 18
) payment_round
  ON payment_round.pay_round <= account.monthly_count
WHERE account.created_nm = 'TEST-SOCIAL-VETERAN'
  AND account.del_yn = 'N'
  AND NOT EXISTS (
      SELECT 1
      FROM saving_history existing_history
      WHERE existing_history.account_id = account.account_id
        AND existing_history.pay_round = payment_round.pay_round
  );

-- ── 전역자 로드맵 관심도 100명 ─────────────────────────────────
-- 한 회원당 대표 관심 로드맵 한 건을 저장해 전체/군종/부대 탭에서
-- 각각 유의미한 비율이 나오도록 한다.

-- 여행 40명
INSERT INTO travel_goal
(`user_id`, `title`, `departure`, `destination`, `is_domestic`, `style`,
 `start_date`, `end_date`, `total_budget`, `status`,
 `created_date`, `created_nm`, `del_yn`)
SELECT
    member.id,
    '전역 후 여행 목표',
    '서울',
    CASE MOD(source.sequence_no, 4)
        WHEN 0 THEN '부산'
        WHEN 1 THEN '도쿄'
        WHEN 2 THEN '다낭'
        ELSE '오사카'
    END,
    CASE WHEN MOD(source.sequence_no, 4) = 0 THEN 1 ELSE 0 END,
    'common',
    '2027-10-01',
    '2027-10-04',
    1200000,
    'CONFIRMED',
    NOW(),
    'TEST-SOCIAL-VETERAN',
    'N'
FROM (
    SELECT
        source_member.id AS member_id,
        ROW_NUMBER() OVER (ORDER BY source_member.user_id) AS sequence_no
    FROM `user` source_member
    WHERE source_member.created_nm = 'TEST-SOCIAL-VETERAN'
) source
JOIN `user` member ON member.id = source.member_id
WHERE source.sequence_no BETWEEN 1 AND 40
  AND NOT EXISTS (
      SELECT 1
      FROM travel_goal existing_goal
      WHERE existing_goal.user_id = member.id
        AND existing_goal.created_nm = 'TEST-SOCIAL-VETERAN'
  );

-- 진로 30명
INSERT INTO job_goal
(`user_id`, `goal_type`, `expected_date`, `status`,
 `created_date`, `created_nm`, `del_yn`)
SELECT
    member.id,
    CASE MOD(source.sequence_no, 3)
        WHEN 0 THEN 'J01'
        WHEN 1 THEN 'J02'
        ELSE 'J03'
    END,
    '2027-12',
    'CONFIRMED',
    NOW(),
    'TEST-SOCIAL-VETERAN',
    'N'
FROM (
    SELECT
        source_member.id AS member_id,
        ROW_NUMBER() OVER (ORDER BY source_member.user_id) AS sequence_no
    FROM `user` source_member
    WHERE source_member.created_nm = 'TEST-SOCIAL-VETERAN'
) source
JOIN `user` member ON member.id = source.member_id
WHERE source.sequence_no BETWEEN 41 AND 70
  AND NOT EXISTS (
      SELECT 1
      FROM job_goal existing_goal
      WHERE existing_goal.user_id = member.id
        AND existing_goal.created_nm = 'TEST-SOCIAL-VETERAN'
  );

-- 자동차 20명
INSERT INTO car_goal
(`user_id`, `budget`, `is_new`, `experience_years`,
 `target_date`, `region`, `status`, `created_date`, `created_nm`, `del_yn`)
SELECT
    member.id,
    15000000 + MOD(source.sequence_no, 4) * 3000000,
    MOD(source.sequence_no, 2),
    1 + MOD(source.sequence_no, 3),
    '2027-11-01',
    '서울',
    'CONFIRMED',
    NOW(),
    'TEST-SOCIAL-VETERAN',
    'N'
FROM (
    SELECT
        source_member.id AS member_id,
        ROW_NUMBER() OVER (ORDER BY source_member.user_id) AS sequence_no
    FROM `user` source_member
    WHERE source_member.created_nm = 'TEST-SOCIAL-VETERAN'
) source
JOIN `user` member ON member.id = source.member_id
WHERE source.sequence_no BETWEEN 71 AND 90
  AND NOT EXISTS (
      SELECT 1
      FROM car_goal existing_goal
      WHERE existing_goal.user_id = member.id
        AND existing_goal.created_nm = 'TEST-SOCIAL-VETERAN'
  );

-- 자취 10명
INSERT INTO rent_goal
(`user_id`, `title`, `selection_mode`, `monthly_budget`,
 `residence_preset`, `residence_months`, `status`,
 `created_date`, `created_nm`, `del_yn`)
SELECT
    member.id,
    '전역 후 자취 목표',
    'REGION',
    500000 + MOD(source.sequence_no, 4) * 50000,
    'YEAR',
    12,
    'CONFIRMED',
    NOW(),
    'TEST-SOCIAL-VETERAN',
    'N'
FROM (
    SELECT
        source_member.id AS member_id,
        ROW_NUMBER() OVER (ORDER BY source_member.user_id) AS sequence_no
    FROM `user` source_member
    WHERE source_member.created_nm = 'TEST-SOCIAL-VETERAN'
) source
JOIN `user` member ON member.id = source.member_id
WHERE source.sequence_no BETWEEN 91 AND 100
  AND NOT EXISTS (
      SELECT 1
      FROM rent_goal existing_goal
      WHERE existing_goal.user_id = member.id
        AND existing_goal.created_nm = 'TEST-SOCIAL-VETERAN'
  );

DROP TEMPORARY TABLE IF EXISTS tmp_social_veteran_unit;
