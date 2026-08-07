-- --------------------------------------------------------------------
--  [수연] 마스터 / 가맹점 카테고리 매핑(merchant_category)
--  테이블: merchant_category
--  용도: 오픈뱅킹 지출 내역(spending)의 가맹점명 → 카테고리 자동 분류 키워드
--  카테고리 코드: PX / DELIVERY / GAME / VACATION / CONVENIENCE / ETC
--  (kb-data.sql 12건 이식, 재실행 대비 전체 DELETE 후 INSERT)
-- --------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `merchant_category` WHERE `mapping_id` BETWEEN 1 AND 12;

INSERT INTO `merchant_category`
(`mapping_id`, `keyword`, `category`, `priority`, `created_date`, `created_nm`, `modified_date`, `modified_nm`, `del_yn`) VALUES
(1,  '국군복지단',  'PX',          100, NOW(), 'demo', NULL, NULL, 'N'),
(2,  'PX',          'PX',          85,  NOW(), 'demo', NULL, NULL, 'N'),
(3,  '배달의민족',  'DELIVERY',    100, NOW(), 'demo', NULL, NULL, 'N'),
(4,  '쿠팡이츠',    'DELIVERY',    100, NOW(), 'demo', NULL, NULL, 'N'),
(5,  '요기요',      'DELIVERY',    100, NOW(), 'demo', NULL, NULL, 'N'),
(6,  'GS25',        'CONVENIENCE', 90,  NOW(), 'demo', NULL, NULL, 'N'),
(7,  'CU',          'CONVENIENCE', 90,  NOW(), 'demo', NULL, NULL, 'N'),
(8,  '세븐일레븐',  'CONVENIENCE', 90,  NOW(), 'demo', NULL, NULL, 'N'),
(9,  '넥슨',        'GAME',        95,  NOW(), 'demo', NULL, NULL, 'N'),
(10, 'APPLE',       'GAME',        60,  NOW(), 'demo', NULL, NULL, 'N'),
(11, 'KTX',         'VACATION',    85,  NOW(), 'demo', NULL, NULL, 'N'),
(12, '스타벅스',    'ETC',         50,  NOW(), 'demo', NULL, NULL, 'N');
