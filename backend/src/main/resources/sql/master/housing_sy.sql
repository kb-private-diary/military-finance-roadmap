-- --------------------------------------------------------------------
--  [수연] 마스터 / 주거 금융상품(housing_product) - 자취 로드맵 월세 추천용
--  테이블: housing_product
--  주의: 이 테이블의 감사컬럼은 다른 테이블과 달리 created_at / updated_at 이며
--        del_yn / created_nm 컬럼이 없다(스키마 기준). 공통 감사규칙 미적용.
--  분류:
--   - product_type : POLICY(정책) / LOCAL(지자체) / BANK(시중)
--   - support_type : GRANT(지원금) / LOAN(대출) / INTEREST / COST
--   - loan_type    : DEPOSIT(보증금) / MONTHLY(월세) / BOTH, GRANT는 NULL
--   - region_code  : 법정동 시도코드 2자리(NULL=전국). 부산=26
--   - is_kb='Y' + priority 낮음 → KB/정책 상단 노출
--   - plcy_no NULL → KB 자체등록(수동), API 배치가 건드리지 않음
--  (현재 DB 0건. 재실행 대비 전체 DELETE 후 INSERT)
-- --------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `housing_product` WHERE `product_id` BETWEEN 1 AND 7;

INSERT INTO `housing_product`
(`product_id`, `product_name`, `product_type`, `support_type`, `loan_type`, `provider`, `is_kb`,
 `region_code`, `rate_min`, `rate_max`, `rate_summary`, `support_amount`, `support_months`, `loan_limit`,
 `min_age`, `max_age`, `veteran_extend`, `veteran_note`,
 `deposit_limit`, `monthly_limit`, `area_limit`, `income_limit`, `join_condition`,
 `exclusive_group`, `base_year`, `apply_start`, `apply_end`, `apply_cycle`,
 `priority`, `detail`, `external_url`, `deeplink`, `plcy_no`, `zip_cd`, `api_synced_at`,
 `created_at`, `updated_at`)
VALUES
-- ── KB 상품 (BANK, is_kb='Y', 전국, 상단 노출) ─────────────────────────
(1, 'KB 청년 맞춤형 전세자금대출', 'BANK', 'LOAN', 'DEPOSIT', 'KB국민은행', 'Y',
 NULL, 3.20, 4.30, '연 3.2~4.3%', NULL, NULL, 200000000,
 19, 34, 6, '병역 이행 기간(최대 6년)만큼 만 나이 상한 연장',
 NULL, NULL, NULL, 50000000, '만 19~34세 무주택 청년, 부부합산 연소득 5천만원 이하',
 NULL, 2026, NULL, NULL, '상시',
 10, 'KB국민은행 청년 전용 전세자금대출. 보증금의 최대 90%까지 지원.',
 'https://obank.kbstar.com/quics?page=C016613', 'kbstarbanking://loan/jeonse-youth', NULL, NULL, NULL,
 NOW(), NOW()),

(2, 'KB 전세금안심대출', 'BANK', 'LOAN', 'DEPOSIT', 'KB국민은행', 'Y',
 NULL, 3.40, 4.50, '연 3.4~4.5%', NULL, NULL, 300000000,
 19, 34, 6, '병역 이행 기간(최대 6년)만큼 만 나이 상한 연장',
 NULL, NULL, NULL, NULL, '주택도시보증공사(HUG) 전세보증금 반환보증 연계',
 NULL, 2026, NULL, NULL, '상시',
 11, 'HUG 안심대출 연계 상품으로 전세보증금 반환 리스크를 낮춘 KB 전세대출.',
 'https://obank.kbstar.com/quics?page=C016613', 'kbstarbanking://loan/jeonse-safe', NULL, NULL, NULL,
 NOW(), NOW()),

(3, 'KB 월세대출', 'BANK', 'LOAN', 'MONTHLY', 'KB국민은행', 'Y',
 NULL, 3.50, 4.80, '연 3.5~4.8%', NULL, NULL, 12000000,
 19, 34, 6, '병역 이행 기간(최대 6년)만큼 만 나이 상한 연장',
 NULL, NULL, NULL, 50000000, '만 19~34세 청년, 월세 부담 완화를 위한 소액 대출',
 NULL, 2026, NULL, NULL, '상시',
 12, '매월 월세 납입액을 대출로 지원하는 KB 청년 월세 전용 대출.',
 'https://obank.kbstar.com/quics?page=C016613', 'kbstarbanking://loan/wolse', NULL, NULL, NULL,
 NOW(), NOW()),

-- ── 전국 정책 상품 (POLICY, is_kb='N', 전국) ───────────────────────────
(4, '청년전용 버팀목 전세자금대출', 'POLICY', 'LOAN', 'DEPOSIT', '주택도시기금', 'N',
 NULL, 1.50, 2.70, '연 1.5~2.7%', NULL, NULL, 200000000,
 19, 34, 6, '병역 이행 기간(최대 6년)만큼 만 나이 상한 연장',
 300000000, NULL, NULL, 50000000, '만 19~34세 무주택 세대주, 부부합산 연소득 5천만원 이하',
 NULL, 2026, NULL, NULL, '상시',
 20, '주택도시기금 청년 전용 버팀목 전세대출. 낮은 정책금리가 강점.',
 'https://nhuf.molit.go.kr/', NULL, NULL, NULL, NULL,
 NOW(), NOW()),

(5, '중소기업취업청년 전월세보증금대출', 'POLICY', 'LOAN', 'DEPOSIT', '주택도시기금', 'N',
 NULL, 1.50, 1.50, '연 1.5% (고정)', NULL, NULL, 100000000,
 19, 34, 6, '병역 이행 기간(최대 6년)만큼 만 나이 상한 연장',
 200000000, NULL, NULL, 35000000, '중소·중견기업 재직 또는 청년창업 청년, 연소득 3,500만원 이하',
 NULL, 2026, NULL, NULL, '상시',
 21, '중소기업 취업 청년 대상 초저금리(연 1.5%) 전월세보증금 대출.',
 'https://nhuf.molit.go.kr/', NULL, NULL, NULL, NULL,
 NOW(), NOW()),

(6, '청년월세 특별지원', 'POLICY', 'GRANT', NULL, '국토교통부', 'N',
 NULL, NULL, NULL, '월 최대 20만원 (최대 12개월)', 200000, 12, NULL,
 19, 34, 6, '병역 이행 기간(최대 6년)만큼 만 나이 상한 연장',
 NULL, 600000, NULL, NULL, '만 19~34세 무주택 청년, 보증금 5천만원·월세 60만원 이하 거주',
 NULL, 2026, NULL, NULL, '연 1회 접수(공고 확인)',
 22, '국토교통부 청년월세 특별지원. 월 최대 20만원을 최대 12개월간 현금 지원.',
 'https://www.myhome.go.kr/', NULL, NULL, NULL, NULL,
 NOW(), NOW()),

-- ── 부산 지역 상품 (LOCAL, is_kb='N', region_code='26') ────────────────
(7, '부산 청년 월세 지원사업', 'LOCAL', 'GRANT', NULL, '부산광역시', 'N',
 '26', NULL, NULL, '월 최대 10만원 (최대 12개월)', 100000, 12, NULL,
 19, 34, 6, '병역 이행 기간(최대 6년)만큼 만 나이 상한 연장',
 NULL, 500000, NULL, NULL, '부산 거주 만 19~34세 무주택 청년, 월세 50만원 이하',
 NULL, 2026, NULL, NULL, '부산시 공고 시 접수',
 40, '부산광역시 청년 월세 지원. 부산 거주 청년에게 월 최대 10만원 지원.',
 'https://young.busan.go.kr/', NULL, NULL, NULL, NULL,
 NOW(), NOW());
