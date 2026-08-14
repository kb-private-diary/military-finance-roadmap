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

-- ── 마스터 데이터 ──────────────────────────
-- 주석은 SOURCE 윗줄에 쓸 것 (같은 줄 주석은 경로 인식 오류남)
-- 법정동 (수연)
SOURCE master/region_sy.sql;
-- 학교 (수연)
SOURCE master/school_sy.sql;
-- 지하철역 (수연)
SOURCE master/station_sy.sql;
-- 공과금 계수 (수연)
SOURCE master/utility_sy.sql;
-- 공통(군종/부대/계급/은행/로드맵카테고리) (석윤) ※ user/saving_account FK·급여계산에 필수
SOURCE master/common_seok.sql;
-- 예적금/군적금/정책상품 (석윤) ※ military_saving_product 만기계산에 필수
SOURCE master/saving_seok.sql;
-- 가맹점 카테고리 매핑 (수연)
SOURCE master/merchant_sy.sql;
-- 주거 금융상품 (수연)
SOURCE master/housing_sy.sql;
-- 진로 마스터 데이터 (지원)
SOURCE master/job_jiwon.sql;
-- 여행 지역, 항공비, 숙박비, 상품 (조태석)
SOURCE master/travel_jts.sql;
-- 사용자 뱃지 정보 (조태석)
SOURCE master/badge_jts.sql;
-- 자동차 (호빈)
SOURCE master/car_hb.sql;
-- 약관 (호빈)
SOURCE master/terms_hb.sql;
-- ... 각자 파일 여기에 추가

-- ── 데모 트랜잭션 데이터 (마스터 뒤에 로드) ───────────────────────
-- 후회소비 데모: user(id=1,2)/vacation/chat (호빈 start) + 오픈뱅킹/적금/자취 (수연 병합)
SOURCE demo/kb_demo_reviews.sql;
-- 전역자 저축 비교 통계·로드맵 관심도용 회원/군적금/납입 이력 100명 (태석)
SOURCE demo/social_veteran_jts.sql;

SET FOREIGN_KEY_CHECKS = 1;   -- FK 체크 ON (다시 켬)

SELECT 'kb_seed done' AS status;  -- 완료 확인용
