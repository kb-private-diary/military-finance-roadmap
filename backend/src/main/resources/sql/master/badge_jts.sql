-- --------------------------------------------------------------------
--  [태석] 공통 / 뱃지(badge)
--  테이블: badge
-- --------------------------------------------------------------------
INSERT INTO badge (badge_id, badge_name, image_url, created_date, created_nm, del_yn)

VALUES
    -- 랭킹
    (1, '상위 1% 뱃지',   '/images/badge/rank_1.png',   NOW(), 'jotaeseok', 'N'),
    (2, '상위 5% 뱃지',   '/images/badge/rank_5.png',   NOW(), 'jotaeseok', 'N'),
    (3, '상위 10% 뱃지',  '/images/badge/rank_10.png',  NOW(), 'jotaeseok', 'N'),
    (4, '상위 30% 뱃지',  '/images/badge/rank_30.png',  NOW(), 'jotaeseok', 'N'),
    -- 업적
    (5, '진행률 50% 뱃지',  '/images/badge/prog_50.png',  NOW(), 'jotaeseok', 'N'),
    (6, '진행률 75% 뱃지',  '/images/badge/prog_75.png',  NOW(), 'jotaeseok', 'N'),
    (7, '진행률 100% 뱃지', '/images/badge/prog_100.png', NOW(), 'jotaeseok', 'N'),
    (8, '절약 달인 뱃지', '/images/badge/saving_master.png', NOW(), 'jotaeseok', 'N');