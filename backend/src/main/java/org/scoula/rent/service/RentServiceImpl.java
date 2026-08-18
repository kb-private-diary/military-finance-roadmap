package org.scoula.rent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.exception.BusinessException;
import org.scoula.rent.domain.RentGoalVO;
import org.scoula.rent.domain.RentGoalRegionVO;
import org.scoula.rent.domain.RentListingVO;
import org.scoula.rent.domain.RentAffordability;
import org.scoula.rent.domain.SchoolVO;
import org.scoula.rent.domain.ResidencePreset;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.SchoolSearchResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.RentListingResponseDTO;
import org.scoula.rent.dto.RentListingDetailResponseDTO;
import org.scoula.rent.dto.RentCostResponseDTO;
import org.scoula.rent.dto.RentAffordabilityResponseDTO;
import org.scoula.rent.dto.MarketComparisonResponseDTO;
import org.scoula.rent.dto.ListingSummaryDTO;
import org.scoula.rent.dto.NearbyFacilityDTO;
import org.scoula.rent.dto.PrecisionSimulationDTO;
import org.scoula.rent.dto.UtilityEstimateResponseDTO;
import org.scoula.rent.client.KakaoLocalClient;
import org.scoula.rent.client.KakaoGeocodingClient;
import org.scoula.rent.dto.NearestStationDTO;
import org.scoula.rent.mapper.RentMapper;
import org.scoula.rent.mapper.RentListingMapper;
import org.scoula.rent.mapper.StationMapper;
import org.scoula.dashboard.service.DashboardService;
import org.scoula.regret.service.RegretService;
import org.scoula.regret.dto.RegretSpendingSummaryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentServiceImpl implements RentService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String MODE_SCHOOL = "SCHOOL";
    private static final String MODE_REGION = "REGION";
    private static final int SPENDING_MONTHS = 3; // 소비 패턴 집계 기간 (최근 3개월)

    // 전월세전환율 (연 5.5%). 보증금환산액 = deposit × 0.055 ÷ 12 (원/월)
    //   반전세(보증금 크고 월세 낮음)를 순수 월세와 공정 비교하려고 보증금을 월부담으로 환산한다.
    //   근거: 주택임대차보호법 법정 전월세전환율 상한(기준금리+2%) 근처 + 한국부동산원 서울 오피스텔 실무값 ≈ 5.5%
    //   저장 위치: utility_constant 는 지역계수·요금표 중심 key-value 라 법정 도메인 상수는 코드에서 단일 관리
    private static final double JEONSE_CONVERSION_RATE = 0.055;

    // 보증금 감당 토글 (findAffordability) - 기본 INCLUDE
    private static final String DEPOSIT_MODE_EXCLUDE = "EXCLUDE"; // 보증금 대출 전제 → 필요목돈에서 제외

    // findListings 노출 매물 최대 개수 (실질월부담 필터 후 상위 N개, 선택 피로 방지)
    private static final int LISTING_LIMIT = 30;

    // --- 동네 시세 비교 (findMarketComparison) 상수 ---
    private static final int MARKET_MIN_SAMPLE = 5;                    // 최소 표본 (미만이면 enough=false)
    private static final int MARKET_TOTAL_ITEMS = 6;                  // 비교 항목 수 (고정)
    private static final BigDecimal MARKET_AREA_BAND = BigDecimal.valueOf(5); // 유사 전용면적 비교구간 ±5㎡

    // --- Step2 카드 모드별 뱃지 상수 ---
    private static final int AFFORD_MONTHS = 6;    // 재정진단 기준 거주개월 (뱃지는 6개월 고정)
    private static final int WALK_M_PER_MIN = 80;  // 도보 환산 (부동산 관례 1분 = 80m)
    private static final int BUS_M_PER_MIN = 200;  // 버스 환산 대략 (정차·대기 포함 시속 ≈ 12km = 200m/분)
    private static final int WALK_RADIUS_M = 800;  // 도보권 상한 (도보 10분) - 초과 시 버스권으로 표시
    private static final double EARTH_RADIUS_M = 6_371_000; // 하버사인 지구 반지름(m)

    private final RentMapper mapper;
    private final RentListingMapper listingMapper;
    private final StationMapper stationMapper; // Step2 지역 모드 대중교통 뱃지 (매물 800m 내 최근접 지하철역)
    private final UtilityService utilityService; // Step3 관리비 = 새 공과금 방식(region_fee_stat 대체)
    // 만기금(군적금 예상 만기 수령액) 조회용 - rent → dashboard 단방향 주입 (dashboard는 rent 미참조, 순환 없음)
    private final DashboardService dashboardService;
    private final RegretService regretService;   // Step5 소비 패턴 (후회소비 최근 3개월) - rent → regret 단방향
    private final KakaoLocalClient kakaoLocalClient; // Step5 주변 편의시설 (카카오 로컬)
    private final KakaoGeocodingClient geocodingClient; // SCHOOL 모드 학교 좌표→시군구 역지오코딩 (좌표 없는 매물 대응)

    @Override
    @Transactional(readOnly = true)
    public List<RegionResponseDTO> findRegions(String sido, String sigunguCode) {
        if (sido == null && sigunguCode == null) {
            // 아무것도 없음 → 시/도 목록
            return this.mapper.findSidoList().stream()
                    .map(vo -> new RegionResponseDTO(vo.getSidoName(), vo.getSidoName()))
                    .toList();
        } else if (sigunguCode == null) {
            // 시/도만 있음 → 시/군/구 목록
            return this.mapper.findSigunguListBySido(sido).stream()
                    .map(vo -> new RegionResponseDTO(vo.getSigunguCode(), vo.getSigunguName()))
                    .toList();
        } else {
            // 시/군/구코드 있음 → 읍/면/동 목록
            return this.mapper.findUmdListBySigunguCode(sigunguCode).stream()
                    .map(vo -> new RegionResponseDTO(vo.getRegionCode(), vo.getUmdName()))
                    .toList();
        }
    }

    @Override
    @Transactional
    public Long createGoal(RentGoalCreateRequestDTO request, Long userId) {
        // 1) 모드별 필수값 검증 (SCHOOL ↔ REGION XOR)
        validateSelectionMode(request);

        // 2) 거주기간 프리셋 → 개월수 변환
        int months = toResidenceMonths(request.getResidencePreset());

        String creator = "user:" + userId; // TODO: JWT 연동 후 로그인 사용자명으로 교체

        // 3) 재등록 방식: 기존 DRAFT 를 soft delete(del_yn='Y') 후 새로 INSERT
        //    자취는 "목표 수정"이 없음 - 등록값을 바꾸면 step1부터 다시 추천해야 해서 새 등록과 동일하기 때문
        //    그래서 conflict(막기)가 아니라 기존 DRAFT 대체로 처리 (결과적으로 회원당 DRAFT 1건 유지)
        this.mapper.deleteDraftGoalByUserId(userId, creator);

        // 4) VO 조립 - 서버가 정하는 값(status·개월수·생성자)을 여기서 채움
        RentGoalVO goal = new RentGoalVO();
        goal.setUserId(userId);
        goal.setSelectionMode(request.getSelectionMode());
        goal.setSchoolId(request.getSchoolId());
        goal.setCommuteRadiusKm(request.getCommuteRadiusKm());
        goal.setMonthlyBudget(request.getMonthlyBudget());
        goal.setResidencePreset(request.getResidencePreset());
        goal.setResidenceMonths(months);
        goal.setStatus(STATUS_DRAFT);
        goal.setCreatedNm(creator);

        // 5) rent_goal INSERT - 실행 후 goal.goalId 에 생성된 번호가 채워짐 (useGeneratedKeys)
        this.mapper.insertGoal(goal);

        // 6) REGION 모드면 희망 지역들을 rent_goal_region 에 저장 (1:N)
        if (MODE_REGION.equals(request.getSelectionMode())) {
            List<RentGoalRegionVO> regions = request.getRegionCodes().stream()
                    .map(code -> {
                        RentGoalRegionVO region = new RentGoalRegionVO();
                        region.setGoalId(goal.getGoalId());
                        region.setRegionCode(code);
                        region.setCreatedNm(creator);
                        return region;
                    })
                    .toList();
            this.mapper.insertGoalRegions(regions);
        }

        // 7) 생성된 goalId 반환
        return goal.getGoalId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolSearchResponseDTO> findSchools(String keyword) {
        return this.mapper.findSchoolsByKeyword(keyword).stream()
                .map(SchoolSearchResponseDTO::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RentGoalDetailResponseDTO findGoal(Long goalId) {
        RentGoalVO goal = this.mapper.findGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다.", "RENT_005");
        }
        // 확정 매물 없으면(DRAFT 등) 목표 기본 정보만 (Step1~4 단계)
        if (goal.getConfirmedListingId() == null) {
            return RentGoalDetailResponseDTO.of(goal);
        }
        RentListingVO listing = this.listingMapper.findById(goal.getConfirmedListingId());
        if (listing == null) {
            return RentGoalDetailResponseDTO.of(goal); // 확정 매물이 유실됐으면 기본 정보만
        }

        // Step5 저장 후 상세: 주변 편의시설 + 진짜 정밀 시뮬레이션
        List<NearbyFacilityDTO> facilities = this.kakaoLocalClient.searchNearby(
                listing.getLatitude(), listing.getLongitude());
        PrecisionSimulationDTO simulation = buildPrecisionSimulation(goal, listing);

        // step5 비용계산 관리비 표시용 - 면적앵커+시도계수로 월 관리비 계산 (regionCode/면적 없으면 0)
        long mgmtFee = 0L;
        if (listing.getRegionCode() != null && listing.getAreaSqm() != null) {
            mgmtFee = this.utilityService.calcManagementFee(
                    listing.getRegionCode(), listing.getAreaSqm().doubleValue());
        }
        ListingSummaryDTO listingSummary = ListingSummaryDTO.of(listing);
        listingSummary.setMaintenanceFee(mgmtFee);

        return RentGoalDetailResponseDTO.of(goal).toBuilder()
                .listing(listingSummary)
                .nearbyFacilities(facilities)
                .precisionSimulation(simulation)
                .build();
    }

    /**
     * 진짜 정밀 시뮬레이션 (Step5 킬러)
     * 매달 주거비(월세 + 관리비 + 공과금) + 소비 패턴(후회소비) → 만기금으로 자취 가능 개월수
     */
    private PrecisionSimulationDTO buildPrecisionSimulation(RentGoalVO goal, RentListingVO listing) {
        int months = goal.getResidenceMonths() != null ? goal.getResidenceMonths() : 6;
        double area = listing.getAreaSqm() != null ? listing.getAreaSqm().doubleValue() : 0;
        long monthlyRent = listing.getMonthlyRent() != null ? listing.getMonthlyRent() : 0L;
        String regionCode = listing.getRegionCode();

        // 월 관리비 = K-apt 면적앵커 보간 단가 × 전용면적 × 시도계수 (공용관리비)
        long mgmtMonthly = this.utilityService.calcManagementFee(regionCode, area);

        // 월평균 공과금 = 전체 월평균(전기+난방+수도+관리비) - 관리비
        //   입주월 정보가 없어 현재 연월 기준 N개월로 추정 (계절 편차는 월별 누적 평균으로 흡수)
        long utilityMonthly = 0L;
        try {
            LocalDate now = LocalDate.now();
            UtilityEstimateResponseDTO est = this.utilityService.estimate(
                    regionCode, area, now.getYear(), now.getMonthValue(), months);
            utilityMonthly = Math.max(0L, est.getMonthlyAvgFee() - mgmtMonthly);
        } catch (Exception e) {
            // 공과금 계수 미비 지역 등은 공과금 0으로 표시 (주거비는 월세+관리비까지만)
            log.warn("공과금 추정 실패 (regionCode={}): {}", regionCode, e.getMessage());
        }

        long rentAndFee = monthlyRent + mgmtMonthly;      // 월세 + 관리비
        long housingTotal = rentAndFee + utilityMonthly;  // 매달 주거비

        // 소비 패턴 (후회소비 최근 3개월 월평균)
        RegretSpendingSummaryDTO spending = this.regretService.getSpendingSummary(goal.getUserId(), SPENDING_MONTHS);
        long avgSpending = spending != null ? spending.getAvgMonthlySpending() : 0L;
        long avgRegret = spending != null ? spending.getAvgRegretSpending() : 0L;

        // 만기금 (군적금 예상 만기 수령액)
        long maturity = this.dashboardService.findSavingsStatus(goal.getUserId()).getExpectedMaturityTotal();

        // 진짜 필요한 월 자금 = 주거비 + 월평균 지출 → 만기금으로 자취 가능 개월수
        long totalMonthlyNeed = housingTotal + avgSpending;
        double possibleMonths = monthsAffordable(maturity, totalMonthlyNeed);
        long reducedMonthlyNeed = Math.max(0L, totalMonthlyNeed - avgRegret); // 후회소비 절감 시
        double reducedPossibleMonths = monthsAffordable(maturity, reducedMonthlyNeed);

        return PrecisionSimulationDTO.builder()
                .monthlyHousingCost(new PrecisionSimulationDTO.MonthlyHousingCost(
                        rentAndFee, utilityMonthly, housingTotal))
                .userSpending(new PrecisionSimulationDTO.UserSpending(avgSpending, avgRegret))
                .totalMonthlyNeed(totalMonthlyNeed)
                .possibleMonths(possibleMonths)
                .reducedMonthlyNeed(reducedMonthlyNeed)
                .reducedPossibleMonths(reducedPossibleMonths)
                .build();
    }

    /** 만기금 / 월 필요자금 → 자취 가능 개월수 (소수 1자리, 0 나눗셈 방지) */
    private double monthsAffordable(long maturity, long monthlyNeed) {
        if (monthlyNeed <= 0) {
            return 0;
        }
        return Math.round((double) maturity / monthlyNeed * 10) / 10.0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentListingResponseDTO> findListings(Long goalId) {
        RentGoalVO goal = this.mapper.findGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다.", "RENT_005");
        }

        // 1) 후보 매물 조회 (SQL 은 지역/학교반경 + 월세 느슨한 상한까지만, LIMIT 없음)
        List<RentListingVO> candidates;
        if (MODE_SCHOOL.equals(goal.getSelectionMode())) {
            // 학교 좌표 기준 반경 내 매물 (좌표 있는 매물)
            candidates = this.listingMapper.findListingsBySchool(
                    goal.getSchoolId(), goal.getCommuteRadiusKm(), goal.getMonthlyBudget());
            // 국토부 매물은 대부분 좌표 미적재라 반경검색이 자주 빈다 → 학교가 속한 시군구 매물로 폴백
            if (candidates.isEmpty()) {
                candidates = findListingsBySchoolRegionFallback(goal);
            }
        } else {
            // 희망 지역(법정동코드)에 속한 매물
            List<String> regionCodes = this.mapper.findRegionCodesByGoalId(goalId);
            candidates = regionCodes.isEmpty()
                    ? List.of()
                    : this.listingMapper.findListingsByRegions(regionCodes, goal.getMonthlyBudget());
        }

        // 2) 실질월부담(월세+관리비+보증금환산) 계산 → 월예산 이하만 남기고 오름차순 상위 30개
        long budget = goal.getMonthlyBudget() != null ? goal.getMonthlyBudget() : 0L;
        List<ListingCost> affordable = filterByEffectiveMonthly(candidates, budget);

        // 3) 시세 상대평가 뱃지용: 최종 노출 매물의 종류별 평균 월세 (같은 조건 매물끼리 비교)
        Map<String, Double> avgRentByType = affordable.stream()
                .map(ListingCost::listing)
                .filter(l -> l.getMonthlyRent() != null && l.getEstateType() != null)
                .collect(Collectors.groupingBy(
                        RentListingVO::getEstateType,
                        Collectors.averagingLong(RentListingVO::getMonthlyRent)));

        // 4) 모드별 뱃지용 공통 데이터 (매물 루프 밖에서 1회 준비)
        String mode = goal.getSelectionMode();
        // 재정진단 만기금(군적금 예상 만기 수령액) - 매물마다 동일하므로 1회 조회
        //   온보딩에서 군적금 가입을 강제하므로 미가입(DASH_002)은 정상 흐름에 없음 - 예외는 그대로 전파
        long maturity = this.dashboardService.findSavingsStatus(goal.getUserId()).getExpectedMaturityTotal();
        // 학교 모드면 통학시간 계산용 학교 좌표를 1회 조회 (지역 모드면 불필요 → null)
        SchoolVO school = MODE_SCHOOL.equals(mode) ? this.mapper.findSchoolById(goal.getSchoolId()) : null;

        // 5) DTO 변환 (기존 뱃지·실질월부담 + 모드별 뱃지 3종: 통학/대중교통·재정진단)
        List<RentListingResponseDTO> result = new ArrayList<>(affordable.size());
        for (ListingCost c : affordable) {
            RentListingVO l = c.listing();
            RentListingResponseDTO dto = RentListingResponseDTO.of(
                    l, avgRentByType.get(l.getEstateType()),
                    c.maintenanceFee(), c.depositConverted(), c.effectiveMonthly());

            dto.setSelectionMode(mode); // 프론트 뱃지 분기용

            // 통학/대중교통 뱃지 - 모드에 해당하는 것 하나만 채움 (다른 하나는 null 유지)
            if (MODE_SCHOOL.equals(mode)) {
                dto.setCommuteText(buildCommuteText(school, l));   // 학교↔매물 도보/버스
            } else {
                dto.setTransitText(buildTransitText(l));           // 매물 800m 내 최근접 지하철역
            }

            // 재정진단 뱃지 (공통, 6개월 거주 기준) - 만기금 vs 6개월 총필요자금
            long totalNeed = calcSixMonthNeed(l, c.maintenanceFee()); // 보증금 + (월세 + 관리비) × 6
            dto.setTotalCost6M(totalNeed); // 프론트 "6개월 예상 N원" 표시용 - 재정진단 판정과 같은 6개월 총필요자금을 그대로 노출
            RentAffordability level = RentAffordability.judge(totalNeed, maturity);
            dto.setAffordLevel(toAffordLevel(level));
            dto.setAffordText(level.getLabel());

            result.add(dto);
        }
        return result;
    }

    /**
     * SCHOOL 모드 폴백: 매물 좌표가 없어 반경검색이 비었을 때, 학교가 속한 시군구의 매물을 반환.
     * 학교 시군구코드가 없으면 학교 좌표를 카카오 역지오코딩해 구하고 school 에 캐시(다음부턴 카카오 호출 없음).
     * 이렇게 하면 전국 어느 학교든 매물 좌표 유무와 무관하게 매물이 노출된다.
     */
    private List<RentListingVO> findListingsBySchoolRegionFallback(RentGoalVO goal) {
        SchoolVO school = this.mapper.findSchoolById(goal.getSchoolId());
        if (school == null) {
            return List.of();
        }
        String sigunguCode = school.getSigunguCode();
        if (sigunguCode == null || sigunguCode.isBlank()) {
            // 학교 좌표 → 시군구코드 (카카오 역지오코딩) 후 school 에 캐시
            sigunguCode = this.geocodingClient.coord2sigungu(school.getLatitude(), school.getLongitude());
            if (sigunguCode != null) {
                // readOnly 트랜잭션이라 캐시 저장 실패해도 조회는 진행 (다음 조회에서 재시도)
                try {
                    this.mapper.updateSchoolSigungu(school.getSchoolId(), sigunguCode);
                } catch (Exception e) {
                    log.warn("학교 시군구 캐시 저장 실패 (schoolId={}): {}", school.getSchoolId(), e.getMessage());
                }
            }
        }
        if (sigunguCode == null || sigunguCode.isBlank()) {
            return List.of(); // 시군구 판정 불가 (좌표·카카오 모두 실패)
        }
        return this.listingMapper.findListingsBySchoolRegion(sigunguCode, goal.getMonthlyBudget());
    }

    /**
     * 6개월 거주 총필요자금 = 보증금 + (월세 + 관리비) × 6 (재정진단 뱃지 기준).
     * findAffordability(총 필요자금 = 보증금 + 월주거비)와 동일한 개념을 6개월 고정으로 적용한다.
     */
    private long calcSixMonthNeed(RentListingVO l, long maintenanceFee) {
        long deposit = l.getDeposit() != null ? l.getDeposit() : 0L;
        long monthlyRent = l.getMonthlyRent() != null ? l.getMonthlyRent() : 0L;
        return deposit + (monthlyRent + maintenanceFee) * AFFORD_MONTHS;
    }

    /**
     * 감당도 판정(RentAffordability) → 프론트 계약 코드 매핑.
     * 판정 로직은 findAffordability 와 동일하게 RentAffordability.judge 재사용,
     * 코드만 SUFFICIENT → ENOUGH 로 바꿔 프론트 계약(ENOUGH/TIGHT/OVER)에 맞춘다. (라벨은 enum 그대로)
     */
    private String toAffordLevel(RentAffordability level) {
        return level == RentAffordability.SUFFICIENT ? "ENOUGH" : level.name();
    }

    /**
     * [학교 모드] 통학시간 뱃지 텍스트.
     * 학교↔매물 직선거리(하버사인)를 구해 800m(도보 10분) 이하면 "도보 N분", 초과면 "버스 N분".
     *   도보 N = 거리 ÷ 80 (부동산 관례 1분=80m), 버스 N = 거리 ÷ 200 (정차·대기 포함 대략)
     * 학교/매물 좌표가 없으면 계산 불가 → null (프론트에서 뱃지 미표시)
     */
    private String buildCommuteText(SchoolVO school, RentListingVO listing) {
        if (school == null) {
            return null;
        }
        // (1) 학교·매물 좌표가 둘 다 있으면 하버사인 직선거리로 정확한 통학시간 (도보/버스)
        if (school.getLatitude() != null && school.getLongitude() != null
                && listing.getLatitude() != null && listing.getLongitude() != null) {
            double distanceM = haversineMeters(
                    school.getLatitude().doubleValue(), school.getLongitude().doubleValue(),
                    listing.getLatitude().doubleValue(), listing.getLongitude().doubleValue());
            if (distanceM <= WALK_RADIUS_M) {
                int walkMin = Math.max(1, (int) Math.round(distanceM / WALK_M_PER_MIN));
                return "도보 " + walkMin + "분";
            }
            int busMin = Math.max(1, (int) Math.round(distanceM / BUS_M_PER_MIN));
            return "버스 " + busMin + "분";
        }
        // (2) 좌표 없는 폴백 매물(국토부 매물 대부분 좌표 미적재): 학교와 같은 시군구면 통학권으로 표시
        //     거리는 계산 불가하지만 "학교와 같은 구"임을 알려 통학 뱃지가 비지 않게 함
        if (school.getSigunguCode() != null
                && school.getSigunguCode().equals(listing.getSigunguCode())) {
            return "학교와 같은 구";
        }
        // (3) 좌표도 없고 시군구도 다르면 통학 뱃지 미표시
        return null;
    }

    /**
     * [지역 모드] 대중교통 뱃지 텍스트.
     * 매물 800m 내 최근접 지하철역이 있으면 "OO역 도보 N분"(N = 거리 ÷ 80), 없으면 "버스 이용 지역".
     * (역 조회는 매물마다 1회 - 노출 매물 최대 30개라 부담이 크지 않음. 필요 시 좌표 반올림 캐시로 고도화)
     * 원본 역명이 이미 '역'으로 끝나면(예: 양촌역) '역'을 덧붙이지 않아 "양촌역역" 중복을 막는다.
     */
    private String buildTransitText(RentListingVO listing) {
        if (listing.getLatitude() == null || listing.getLongitude() == null) {
            return "버스 이용 지역"; // 좌표 없으면 역 검색 불가 → 버스권으로 표시
        }
        NearestStationDTO station = this.stationMapper.findNearestStation(
                listing.getLatitude(), listing.getLongitude());
        if (station == null || station.getDistanceM() == null) {
            return "버스 이용 지역";
        }
        int walkMin = Math.max(1, (int) Math.round(station.getDistanceM() / WALK_M_PER_MIN));
        String name = station.getStationName();
        String label = name.endsWith("역") ? name : name + "역";
        return label + " 도보 " + walkMin + "분";
    }

    /**
     * 두 위경도 사이 직선거리(m) - 하버사인 공식.
     * SQL 의 ST_Distance_Sphere 와 동일 개념이며, 학교 거리는 매물마다 SQL 재조회 대신
     * 학교 좌표 1회 조회 후 인메모리로 계산한다(N+1 회피).
     */
    private double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * 후보 매물 각각의 실질 월부담을 계산해, 월예산 이하인 것만 실질월부담 오름차순 상위 30개로 추린다.
     * - 관리비: utilityService.calcManagementFee(regionCode, areaSqm) (regionCode/areaSqm 없으면 0)
     * - 보증금환산: deposit × 전월세전환율 ÷ 12 (원/월)
     * - 실질월부담: 월세 + 관리비 + 보증금환산 (반전세 공정 비교 기준)
     *
     * 성능: calcManagementFee 는 매 건 DB 조회(시도계수·관리비 앵커)라 N+1 소지가 있어,
     *       동일 (regionCode, areaSqm) 은 요청 단위 Map 으로 메모이즈해 중복 조회를 줄인다.
     *       (같은 건물·같은 면적 매물이 실거래에 반복 등장하므로 중복 제거 효과가 있다)
     *       TODO(확장): 관리비 앵커(findAreaMgmtAnchors)는 요청당 1회, 시도계수는 시도별 1회만
     *                  조회하면 서로 다른 면적까지 완전 제거 가능 - UtilityService 에 배치 API 추가 시 고도화
     */
    private List<ListingCost> filterByEffectiveMonthly(List<RentListingVO> candidates, long budget) {
        Map<String, Long> mgmtCache = new HashMap<>(); // key: regionCode|areaSqm → 관리비 (요청 단위 메모이즈)
        List<ListingCost> result = new ArrayList<>();

        for (RentListingVO l : candidates) {
            long monthlyRent = l.getMonthlyRent() != null ? l.getMonthlyRent() : 0L;
            long deposit = l.getDeposit() != null ? l.getDeposit() : 0L;

            long maintenanceFee = calcMaintenanceFeeCached(l, mgmtCache); // 관리비 (없으면 0)
            long depositConverted = Math.round(deposit * JEONSE_CONVERSION_RATE / 12); // 보증금환산 (원/월)
            long effectiveMonthly = monthlyRent + maintenanceFee + depositConverted;   // 실질 월부담

            // 실질월부담 ≤ 월예산 인 매물만 통과
            if (effectiveMonthly <= budget) {
                result.add(new ListingCost(l, maintenanceFee, depositConverted, effectiveMonthly));
            }
        }

        // 실질월부담 오름차순 정렬 후 상위 30개 (선택 피로 방지)
        result.sort((a, b) -> Long.compare(a.effectiveMonthly(), b.effectiveMonthly()));
        return result.size() > LISTING_LIMIT ? result.subList(0, LISTING_LIMIT) : result;
    }

    /**
     * 관리비 계산 (요청 단위 메모이즈).
     * regionCode/areaSqm 이 없으면 계산 불가 → 0.
     */
    private long calcMaintenanceFeeCached(RentListingVO l, Map<String, Long> cache) {
        String regionCode = l.getRegionCode();
        if (regionCode == null || l.getAreaSqm() == null) {
            return 0L;
        }
        double areaSqm = l.getAreaSqm().doubleValue();
        String key = regionCode + "|" + areaSqm;
        Long cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        long fee = this.utilityService.calcManagementFee(regionCode, areaSqm);
        cache.put(key, fee);
        return fee;
    }

    /** findListings 내부용 홀더 - 매물 VO + 계산된 관리비·보증금환산·실질월부담을 함께 들고 다닌다 */
    private record ListingCost(RentListingVO listing, long maintenanceFee,
                               long depositConverted, long effectiveMonthly) {
    }

    @Override
    @Transactional(readOnly = true)
    public RentListingDetailResponseDTO findListingDetail(Long listingId) {
        RentListingVO vo = this.listingMapper.findById(listingId);
        if (vo == null) {
            throw BusinessException.notFound("매물을 찾을 수 없습니다.", "RENT_006");
        }
        // 예상 관리비 = K-apt 면적앵커 보간 단가 × 전용면적 × 시도계수 (공용관리비, UtilityService로 통일)
        //   regionCode 없거나 areaSqm null 이면 계산 불가 → 0
        long maintenanceFee = 0L;
        if (vo.getRegionCode() != null && vo.getAreaSqm() != null) {
            maintenanceFee = utilityService.calcManagementFee(
                    vo.getRegionCode(), vo.getAreaSqm().doubleValue());
        }

        // 시세 등급(priceLevel): 같은 동네(법정동, 없으면 읍면동)+같은 종류 평균 월세 대비 판정 (표본 없으면 null)
        //   면적 필터 없이 넓게 동네 평균으로 비교 → 상세 시세뱃지가 항상 뜨도록 백엔드에서 직접 계산해 내려준다.
        Double avgRent = this.listingMapper.selectAvgRentForPriceLevel(
                listingId, vo.getEstateType(), vo.getRegionCode(), vo.getUmdName());
        // 법정동에 비교 표본이 없으면 시군구(구 단위)로 넓혀 재판정 → 시세뱃지가 '있다 없다' 하지 않게 안정화
        if (avgRent == null && vo.getSigunguCode() != null) {
            avgRent = this.listingMapper.selectAvgRentBySigungu(
                    listingId, vo.getEstateType(), vo.getSigunguCode());
        }
        String priceLevel = judgePriceLevel(vo.getMonthlyRent(), avgRent);

        return RentListingDetailResponseDTO.of(vo, maintenanceFee, priceLevel);
    }

    /**
     * 시세 상대평가 (RentListingResponseDTO.priceLevel 과 동일 규칙 - 리스트/상세 뱃지 일관성 유지)
     *   avgRent 가 null/0 → null. ratio = 월세/평균; ratio &lt;= 0.9 CHEAP / &gt; 1.1 EXPENSIVE / 그 외 AVERAGE.
     */
    private String judgePriceLevel(Long monthlyRent, Double avgRent) {
        if (monthlyRent == null || avgRent == null || avgRent <= 0) {
            return null;
        }
        double ratio = monthlyRent / avgRent;
        if (ratio <= 0.9) return "CHEAP";
        if (ratio > 1.1) return "EXPENSIVE";
        return "AVERAGE";
    }

    @Override
    @Transactional(readOnly = true)
    public MarketComparisonResponseDTO findMarketComparison(Long listingId) {
        RentListingVO vo = this.listingMapper.findById(listingId);
        if (vo == null) {
            throw BusinessException.notFound("매물을 찾을 수 없습니다.", "RENT_006");
        }

        // 유사 전용면적 비교구간 ±5㎡ (면적 없으면 구간 필터 생략 → 매퍼에서 area 조건 skip)
        BigDecimal areaMin = null;
        BigDecimal areaMax = null;
        if (vo.getAreaSqm() != null) {
            areaMin = vo.getAreaSqm().subtract(MARKET_AREA_BAND);
            areaMax = vo.getAreaSqm().add(MARKET_AREA_BAND);
        }

        long mineRent = vo.getMonthlyRent() != null ? vo.getMonthlyRent() : 0L;

        // 모집단 집계 (같은 법정동+종류+유사면적, 자기 자신 제외)
        Map<String, Object> agg = this.listingMapper.aggregateMarketComparison(
                listingId, vo.getEstateType(), vo.getRegionCode(), vo.getUmdName(),
                areaMin, areaMax, mineRent);

        int sampleCount = (int) toLong(agg.get("sampleCount"));
        boolean enough = sampleCount >= MARKET_MIN_SAMPLE;

        // 평균값: 금액·연도는 반올림 정수, 면적·층은 소수 1자리
        long avgRent = Math.round(toDouble(agg.get("avgMonthlyRent")));
        long avgDeposit = Math.round(toDouble(agg.get("avgDeposit")));
        double avgArea = round1(toDouble(agg.get("avgAreaSqm")));
        int avgBuiltYear = (int) Math.round(toDouble(agg.get("avgBuiltYear")));
        double avgFloor = round1(toDouble(agg.get("avgFloor")));
        long avgRentPerSqm = Math.round(toDouble(agg.get("avgRentPerSqm")));
        long rentMin = toLong(agg.get("rentMin"));
        long rentMax = toLong(agg.get("rentMax"));
        long lowerCount = toLong(agg.get("lowerCount")); // 이 매물 월세보다 싼 표본 수

        // 이 매물(mine) 값
        long mineDeposit = vo.getDeposit() != null ? vo.getDeposit() : 0L;
        double mineArea = vo.getAreaSqm() != null ? round1(vo.getAreaSqm().doubleValue()) : 0.0;
        Integer mineBuiltYear = vo.getBuiltYear();
        Integer mineFloor = vo.getFloor();
        long mineRentPerSqm = (vo.getAreaSqm() != null && vo.getAreaSqm().doubleValue() > 0)
                ? Math.round(mineRent / vo.getAreaSqm().doubleValue()) : 0L;

        // 6개 항목 rows (순서 고정)
        //   쌀수록 좋음(mine<avg): 월세·보증금·㎡당월세 / 클수록·최신일수록 좋음(mine>avg): 면적·건축년도·층수
        List<MarketComparisonResponseDTO.Row> rows = new ArrayList<>(MARKET_TOTAL_ITEMS);
        rows.add(marketRow("monthlyRent", "월세", mineRent, avgRent, mineRent < avgRent));
        rows.add(marketRow("deposit", "보증금", mineDeposit, avgDeposit, mineDeposit < avgDeposit));
        rows.add(marketRow("areaSqm", "전용면적", mineArea, avgArea, mineArea > avgArea));
        rows.add(marketRow("builtYear", "건축년도", mineBuiltYear, avgBuiltYear,
                mineBuiltYear != null && mineBuiltYear > avgBuiltYear));
        rows.add(marketRow("floor", "층수", mineFloor, avgFloor,
                mineFloor != null && mineFloor > avgFloor));
        rows.add(marketRow("rentPerSqm", "㎡당 월세", mineRentPerSqm, avgRentPerSqm, mineRentPerSqm < avgRentPerSqm));

        int betterCount = (int) rows.stream().filter(MarketComparisonResponseDTO.Row::isBetter).count();

        // 월세 백분위 = 이 매물보다 싼 표본 비율(%) (낮을수록 저렴)
        int rentPercentile = sampleCount > 0 ? (int) Math.round(lowerCount * 100.0 / sampleCount) : 0;

        // 가성비 판정 (우위 개수 기준)
        String verdict;
        String verdictTitle;
        if (betterCount >= 4) {
            verdict = "GOOD";
            verdictTitle = "가성비 좋은 매물이에요";
        } else if (betterCount >= 2) {
            verdict = "NORMAL";
            verdictTitle = "무난한 매물이에요";
        } else {
            verdict = "BAD";
            verdictTitle = "아쉬운 조건이 많아요";
        }
        String verdictText = buildMarketVerdictText(
                mineRentPerSqm, avgRentPerSqm, mineDeposit, avgDeposit, mineRent, avgRent);

        return MarketComparisonResponseDTO.builder()
                .umdName(vo.getUmdName())
                .estateTypeLabel(estateTypeLabel(vo.getEstateType()))
                .sampleCount(sampleCount)
                .enough(enough)
                .betterCount(betterCount)
                .totalItems(MARKET_TOTAL_ITEMS)
                .rows(rows)
                .rentMin(rentMin)
                .rentMax(rentMax)
                .rentAvg(avgRent)
                .rentPercentile(rentPercentile)
                .verdict(verdict)
                .verdictTitle(verdictTitle)
                .verdictText(verdictText)
                .build();
    }

    /** 시세 비교 한 행 조립 (mine/avg 는 항목별 타입 그대로 Object 로 담아 JSON 직렬화) */
    private MarketComparisonResponseDTO.Row marketRow(String key, String label,
                                                      Object mine, Object avg, boolean better) {
        return MarketComparisonResponseDTO.Row.builder()
                .key(key).label(label).mine(mine).avg(avg).better(better).build();
    }

    /**
     * 가성비 설명 문장 생성 (목업 톤).
     * ㎡당 월세 차이%(핵심 지표) + 보증금 차액·회수개월(약점/강점)을 자연스러운 한 문장으로.
     *   회수개월 = 보증금이 평균보다 높을 때, (보증금차액 ÷ 월세절감액) 올림 근사.
     */
    private String buildMarketVerdictText(long mineRentPerSqm, long avgRentPerSqm,
                                          long mineDeposit, long avgDeposit,
                                          long mineRent, long avgRent) {
        StringBuilder sb = new StringBuilder();

        // 1) ㎡당 월세 비교 (가성비 핵심)
        if (avgRentPerSqm <= 0 || mineRentPerSqm <= 0) {
            return "비교할 유사 매물 데이터가 충분하지 않아요.";
        }
        long perSqmDiff = avgRentPerSqm - mineRentPerSqm; // 양수면 이 매물이 평균보다 쌈
        int perSqmPct = (int) Math.round(Math.abs(perSqmDiff) * 100.0 / avgRentPerSqm);
        if (perSqmDiff > 0) {
            sb.append("㎡당 월세가 평균보다 ").append(perSqmPct).append("% 낮아요.");
        } else if (perSqmDiff < 0) {
            sb.append("㎡당 월세가 평균보다 ").append(perSqmPct).append("% 높아요.");
        } else {
            sb.append("㎡당 월세가 평균과 비슷해요.");
        }

        // 2) 보증금 차액 + 회수개월
        long depositDiff = mineDeposit - avgDeposit;                 // 양수면 보증금이 평균보다 높음(약점)
        long manDiff = Math.round(Math.abs(depositDiff) / 10000.0);  // 만원 단위
        long monthlyRentSaving = avgRent - mineRent;                 // 월세 절감액(양수면 이 매물이 쌈)
        if (depositDiff > 0) {
            sb.append(" 보증금이 ").append(manDiff).append("만 높은 점은 부담이지만, ");
            if (monthlyRentSaving > 0) {
                long recoverMonths = (long) Math.ceil((double) depositDiff / monthlyRentSaving);
                sb.append(recoverMonths).append("개월 이상 살면 월세 차액으로 회수됩니다.");
            } else {
                sb.append("월세 차액이 크지 않아 회수까지는 시간이 걸립니다.");
            }
        } else if (depositDiff < 0) {
            sb.append(" 보증금도 평균보다 ").append(manDiff).append("만 낮아 초기 부담이 적어요.");
        } else {
            sb.append(" 보증금은 평균과 비슷한 수준이에요.");
        }
        return sb.toString();
    }

    /** 매물종류 코드 → 한글 라벨 (APARTMENT/OFFICETEL/VILLA) */
    private String estateTypeLabel(String type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "OFFICETEL": return "오피스텔";
            case "APARTMENT": return "아파트";
            case "VILLA":     return "빌라";
            default:          return type;
        }
    }

    /** 집계 map 값 → double (AVG=BigDecimal, COUNT/MIN/MAX=Long 등 혼재 → Number 로 통일) */
    private double toDouble(Object o) {
        return o == null ? 0.0 : ((Number) o).doubleValue();
    }

    /** 집계 map 값 → long */
    private long toLong(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    /** 소수 1자리 반올림 (면적·층 표시용) */
    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    @Override
    @Transactional(readOnly = true)
    public RentCostResponseDTO calculateCost(Long listingId, int months) {
        RentListingVO vo = this.listingMapper.findById(listingId);
        if (vo == null) {
            throw BusinessException.notFound("매물을 찾을 수 없습니다.", "RENT_006");
        }
        // 거주개월 검증 (1개월 이상)
        if (months <= 0) {
            throw BusinessException.badRequest("거주 개월수가 올바르지 않습니다.", "RENT_007");
        }
        long deposit = vo.getDeposit() == null ? 0L : vo.getDeposit();
        long monthlyRent = vo.getMonthlyRent() == null ? 0L : vo.getMonthlyRent();

        // 예상 관리비 = K-apt 면적앵커 보간 단가 × 전용면적 × 시도계수 (공용관리비, UtilityService로 통일)
        long monthlyFee = utilityService.calcManagementFee(
                vo.getRegionCode(), vo.getAreaSqm() != null ? vo.getAreaSqm().doubleValue() : 0);

        long livingCost = (monthlyRent + monthlyFee) * months; // 월주거비 = (월세 + 관리비) × 개월
        long totalRequired = deposit + livingCost;             // 보증금(반환) + 월주거비
        return RentCostResponseDTO.of(vo, months, monthlyFee, livingCost, totalRequired);
    }

    @Override
    @Transactional(readOnly = true)
    public RentGoalDetailResponseDTO findCurrentGoal(Long userId) {
        RentGoalVO goal = this.mapper.findCurrentGoalByUserId(userId);
        return goal == null ? null : RentGoalDetailResponseDTO.of(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public RentAffordabilityResponseDTO findAffordability(Long listingId, Long userId, int months, String depositMode) {
        // 1) 총 필요자금 계산 재사용 (매물 존재·개월수 검증 포함)
        //    cost.totalRequired = 보증금 + 월주거비((월세+관리비)×개월), cost.livingCost = 월주거비만
        RentCostResponseDTO cost = this.calculateCost(listingId, months);

        // 2) 보증금 감당 토글 → 필요목돈 결정
        //    INCLUDE(기본): 보증금 + 월주거비 (보증금까지 직접 마련)
        //    EXCLUDE       : 월주거비만 (보증금은 전세대출 전제로 필요목돈에서 제외)
        long required = DEPOSIT_MODE_EXCLUDE.equalsIgnoreCase(depositMode)
                ? cost.getLivingCost()
                : cost.getTotalRequired();

        // 3) 만기금(군적금 예상 만기 수령액) 조회
        //    온보딩에서 군적금 가입을 강제하므로 미가입(DASH_002)은 정상 흐름에 없음 - 예외는 그대로 전파(공통 advice가 처리)
        long maturity = this.dashboardService.findSavingsStatus(userId).getExpectedMaturityTotal();

        // 4) 부족분·감당도 판정 후 응답 조립 (판정 로직은 기존 재사용)
        return RentAffordabilityResponseDTO.of(listingId, months, required, maturity);
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId) {
        RentGoalVO goal = this.mapper.findGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다.", "RENT_005");
        }
        // TODO: JWT 연동 후 로그인 사용자명으로 교체
        this.mapper.deleteGoalById(goalId, "user:" + goal.getUserId());
    }

    @Override
    @Transactional
    public void confirmGoal(Long goalId, Long userId, Integer months, Long listingId, List<Long> selectedProductIds) {
        // 0) 저장할 확정 매물 필수 (Step4에서 고른 매물 = Step5 정밀 시뮬레이션 기준)
        if (listingId == null) {
            throw BusinessException.badRequest("저장할 매물을 선택해주세요.", "RENT_011");
        }
        // 1) 목표 조회 (없으면 404)
        RentGoalVO goal = this.mapper.findGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다.", "RENT_005");
        }
        // 2) 본인 목표만 저장 가능
        if (!goal.getUserId().equals(userId)) {
            throw BusinessException.forbidden("본인의 목표만 저장할 수 있습니다.", "RENT_008");
        }
        // 3) DRAFT 상태만 확정 가능 (이미 CONFIRMED면 충돌)
        if (!STATUS_DRAFT.equals(goal.getStatus())) {
            throw BusinessException.conflict("이미 저장된 목표입니다.", "RENT_009");
        }
        String modifier = "user:" + userId; // TODO: JWT 연동 후 로그인 사용자명으로 교체

        // 4) 회원당 저장된 로드맵(CONFIRMED) 1건만 유지 - 이미 있으면 기존 것 soft delete 후 새로 저장
        //    (사용자가 새 로드맵을 저장하면 기존 확정 로드맵을 대체한다)
        this.mapper.deleteConfirmedGoalByUserId(userId, modifier);

        // 5) 상태 DRAFT → CONFIRMED 확정 (months=거주개월, listingId=Step4에서 고른 확정 매물)
        this.mapper.confirmGoal(goalId, months, listingId, modifier);

        // 6) 선택한 금융상품 저장 (goal_product) - 재저장 대비 기존 것 soft delete 후 신규 삽입
        this.mapper.deleteGoalProductsByGoalId(goalId, modifier);
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            this.mapper.insertGoalProducts(goalId, selectedProductIds, modifier);
        }

        // 참고: Step5 정밀 시뮬레이션은 findGoal 조회 시 확정 매물 기준으로 실시간 계산한다
        //   (공과금 스냅샷 고정 저장(UtilityService.saveSnapshot)은 이력 보존용으로 추후 연결)
    }

    /** SCHOOL / REGION 모드별 필수값 검증 (모드에 따라 달라지는 조건이라 @Valid 대신 여기서) */
    private void validateSelectionMode(RentGoalCreateRequestDTO request) {
        String mode = request.getSelectionMode();
        if (MODE_SCHOOL.equals(mode)) {
            if (request.getSchoolId() == null) {
                throw BusinessException.badRequest("학교를 선택해주세요.", "RENT_002");
            }
        } else if (MODE_REGION.equals(mode)) {
            if (request.getRegionCodes() == null || request.getRegionCodes().isEmpty()) {
                throw BusinessException.badRequest("희망 지역을 선택해주세요.", "RENT_003");
            }
        } else {
            throw BusinessException.badRequest("위치 모드가 올바르지 않습니다.", "RENT_001");
        }
    }

    /** 거주기간 프리셋 → 개월수 (ResidencePreset enum 사용, 잘못된 값이면 RENT_004) */
    private int toResidenceMonths(String preset) {
        try {
            return ResidencePreset.valueOf(preset).getMonths();
        } catch (IllegalArgumentException | NullPointerException e) {
            throw BusinessException.badRequest("거주기간 선택이 올바르지 않습니다.", "RENT_004");
        }
    }
}
