package org.scoula.rent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.exception.BusinessException;
import org.scoula.rent.domain.RentGoalVO;
import org.scoula.rent.domain.RentGoalRegionVO;
import org.scoula.rent.domain.RentListingVO;
import org.scoula.rent.domain.ResidencePreset;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.SchoolSearchResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.RentListingResponseDTO;
import org.scoula.rent.dto.RentListingDetailResponseDTO;
import org.scoula.rent.dto.RentCostResponseDTO;
import org.scoula.rent.dto.RentAffordabilityResponseDTO;
import org.scoula.rent.dto.ListingSummaryDTO;
import org.scoula.rent.dto.NearbyFacilityDTO;
import org.scoula.rent.dto.PrecisionSimulationDTO;
import org.scoula.rent.dto.UtilityEstimateResponseDTO;
import org.scoula.rent.client.KakaoLocalClient;
import org.scoula.rent.mapper.RentMapper;
import org.scoula.rent.mapper.RentListingMapper;
import org.scoula.dashboard.service.DashboardService;
import org.scoula.regret.service.RegretService;
import org.scoula.regret.dto.RegretSpendingSummaryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final RentMapper mapper;
    private final RentListingMapper listingMapper;
    private final UtilityService utilityService; // Step3 관리비 = 새 공과금 방식(region_fee_stat 대체)
    // 만기금(군적금 예상 만기 수령액) 조회용 - rent → dashboard 단방향 주입 (dashboard는 rent 미참조, 순환 없음)
    private final DashboardService dashboardService;
    private final RegretService regretService;   // Step5 소비 패턴 (후회소비 최근 3개월) - rent → regret 단방향
    private final KakaoLocalClient kakaoLocalClient; // Step5 주변 편의시설 (카카오 로컬)

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

        return RentGoalDetailResponseDTO.of(goal).toBuilder()
                .listing(ListingSummaryDTO.of(listing))
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
            // 학교 좌표 기준 반경 내 매물
            candidates = this.listingMapper.findListingsBySchool(
                    goal.getSchoolId(), goal.getCommuteRadiusKm(), goal.getMonthlyBudget());
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

        // 4) DTO 변환 (뱃지 + 관리비·보증금환산·실질월부담 값 담기)
        return affordable.stream()
                .map(c -> RentListingResponseDTO.of(
                        c.listing(),
                        avgRentByType.get(c.listing().getEstateType()),
                        c.maintenanceFee(), c.depositConverted(), c.effectiveMonthly()))
                .toList();
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
        return RentListingDetailResponseDTO.of(vo, maintenanceFee);
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
    public void confirmGoal(Long goalId, Long userId, Integer months, Long listingId) {
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
        // 4) 회원당 저장된 로드맵(CONFIRMED) 1건만 - 이미 있으면 기존 것 삭제 후 저장
        if (this.mapper.countGoalByUserIdAndStatus(userId, "CONFIRMED") > 0) {
            throw BusinessException.conflict("이미 저장된 로드맵이 있습니다. 기존 로드맵을 삭제 후 저장해주세요.", "RENT_010");
        }

        String modifier = "user:" + userId; // TODO: JWT 연동 후 로그인 사용자명으로 교체

        // 5) 상태 DRAFT → CONFIRMED 확정 (months=거주개월, listingId=Step4에서 고른 확정 매물)
        this.mapper.confirmGoal(goalId, months, listingId, modifier);

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
