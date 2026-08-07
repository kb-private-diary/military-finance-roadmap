-- =====================================================================
-- kb_seed.sql : 데이터 일괄 적재 로더 (지휘자)
-- ---------------------------------------------------------------------
-- 이 파일 하나만 실행하면 마스터 + 데모 데이터가 순서대로 들어간다.
-- 데이터는 없고, "무슨 파일을 어떤 순서로 불러올지"만 담는다.
--
-- [실행 방법]  반드시 이 sql 폴더 안에서 실행할 것!
--   cd backend/src/main/resources/sql
--   mysql -u scoula -p scoula_db < kb_schema.sql   -- 1) 테이블 먼저
--   mysql -u scoula -p scoula_db < kb_seed.sql      -- 2) 데이터 적재
--   ( SOURCE 경로는 mysql 실행 위치 기준이라, sql 폴더 밖에서 돌리면 파일을 못 찾음 )
--
-- [FK 처리]  각 데이터 파일 맨 위에 SET FOREIGN_KEY_CHECKS = 0; 을 둔다 (개별 실행 대비).
--   이 로더는 시작에 0, 끝에 1 로 감싸주므로, 파일들이 중간에 다시 켜지 않는 한
--   로드 순서를 신경 쓰지 않아도 된다. (개별 파일은 재활성화 SET ...=1 을 넣지 말 것)
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;   -- FK 체크 OFF (여기서부터)

-- ── 마스터 데이터 (각자 준비, 순서 무관) ──────────────────────────
-- 파일 준비되면 주석(--) 풀어서 추가. 파일명 규칙: 도메인_이니셜.sql
SOURCE master/region_sy.sql;      -- 법정동 (수연)
SOURCE master/school_sy.sql;      -- 학교 (수연)
SOURCE master/station_sy.sql;     -- 지하철역 (수연)
SOURCE master/utility_sy.sql;     -- 공과금 계수 (수연)
-- SOURCE master/rank_xx.sql;     -- 계급/급여
-- SOURCE master/car_hb.sql;      -- 자동차 (호빈)
-- SOURCE master/product_xx.sql;  -- 금융상품
-- ... 각자 파일 여기에 추가

-- ── 데모 트랜잭션 데이터 (마스터 뒤에 로드) ───────────────────────
SOURCE demo/kb_demo_reviews.sql;  -- 후회소비 데모 (호빈 start)

SET FOREIGN_KEY_CHECKS = 1;   -- FK 체크 ON (다시 켬)

SELECT 'kb_seed done' AS status;  -- 완료 확인용
