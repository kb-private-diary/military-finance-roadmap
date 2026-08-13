package org.scoula.travel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.product.service.ProductService;
import org.scoula.regret.dto.RegretSpendingSummaryDTO;
import org.scoula.regret.service.RegretService;
import org.scoula.simulator.dto.SimulatorSavingDetailsResponseDTO;
import org.scoula.simulator.service.SimulatorService;
import org.scoula.travel.client.BookingApiClient;
import org.scoula.travel.client.FlightApiClient;
import org.scoula.travel.client.OdsayClient;
import org.scoula.travel.client.SerpApiClient;
import org.scoula.travel.client.YellowBalloonClient;
import org.scoula.travel.domain.CityCostVO;
import org.scoula.travel.domain.TravelCostVO;
import org.scoula.travel.domain.TravelGoalVO;
import org.scoula.travel.domain.TravelPackageVO;
import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.dto.TravelCostResponseDTO;
import org.scoula.travel.dto.TravelCostStyleUpdateRequestDTO;
import org.scoula.travel.dto.TravelBudgetPlanResponseDTO;
import org.scoula.travel.dto.TravelFinancialProductResponseDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;
import org.scoula.travel.dto.TravelGoalDetailResponseDTO;
import org.scoula.travel.dto.TravelGoalDraftResponseDTO;
import org.scoula.travel.dto.TravelPlaceResponseDTO;
import org.scoula.travel.dto.TravelPlaceSelectionDTO;
import org.scoula.travel.dto.TravelPlacesUpdateRequestDTO;
import org.scoula.travel.dto.TravelQuarterCostSearchDTO;
import org.scoula.travel.dto.TravelRegretInsightResponseDTO;
import org.scoula.travel.dto.TravelPackageResponseDTO;
import org.scoula.travel.dto.TravelPackageSearchDTO;
import org.scoula.travel.dto.TravelPackageUpdateRequestDTO;
import org.scoula.travel.dto.TravelProductRecommendationResponseDTO;
import org.scoula.travel.dto.TravelStyleCostResponseDTO;
import org.scoula.travel.dto.TravelUserFinanceDTO;
import org.scoula.travel.mapper.TravelMapper;
import org.scoula.travel.util.TravelDateCalculator;
import org.scoula.travel.util.TravelPriceCalculator;

@Log4j2
@Service
@RequiredArgsConstructor
public class TravelServiceImpl implements TravelService {

    private final TravelMapper mapper;
    private final OdsayClient odsayClient;
    private final FlightApiClient flightApiClient;
    private final BookingApiClient bookingApiClient;
    private final SerpApiClient serpApiClient;
    private final YellowBalloonClient yellowBalloonClient;
    private final ProductService productService;
    private final SimulatorService simulatorService;
    private final RegretService regretService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, LocalDateTime> packageQueryCache =
            new ConcurrentHashMap<>();

    // 목표 상태값 (DRAFT / CONFIRMED / ARCHIVED)
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String BUDGET_PLAN_IN_SERVICE = "IN_SERVICE";
    private static final String BUDGET_PLAN_AFTER_DISCHARGE =
            "AFTER_DISCHARGE";

    private static final String DOMESTIC_COUNTRY = "대한민국";
    private static final DateTimeFormatter PACKAGE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final int PACKAGE_CACHE_HOURS = 6;
    private static final int ROADMAP_CATEGORY_TRAVEL = 1;
    private static final int REGRET_LOOKBACK_MONTHS = 3;
    private static final int MAX_REGRET_SAVINGS_MONTHS = 12;
    private static final String TRAVEL_SAVING_CODE_PREFIX = "TRV-";

    // 여행 스타일. city_cost 의 saving_cost / common_cost / premium_cost 에 대응.
    private static final List<String> VALID_STYLES =
            Arrays.asList("saving", "common", "premium");

    private static final Set<String> FLIGHT_FALLBACK_CODES =
            Set.of("TRAVEL_017", "TRAVEL_018", "TRAVEL_019");

    private static final Set<String> HOTEL_FALLBACK_CODES =
            Set.of("TRAVEL_020", "TRAVEL_021", "TRAVEL_022");

    private static final BigDecimal TRANSPORT_SAVING_RATE =
            new BigDecimal("0.90");
    private static final BigDecimal TRANSPORT_PREMIUM_RATE =
            new BigDecimal("1.20");
    private static final BigDecimal HOTEL_SAVING_RATE =
            new BigDecimal("0.80");
    private static final BigDecimal HOTEL_PREMIUM_RATE =
            new BigDecimal("1.35");

    @Transactional(readOnly = true)
    @Override
    public List<CityCostResponseDTO> findCityCosts(String country) {
        log.info("findCityCosts: country = " + country);
        return this.mapper.findCityCostList(country).stream()
                .map(CityCostResponseDTO::of)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Long createGoal(
            final Long userId,
            final String userName,
            final TravelGoalCreateRequestDTO request) {
        log.info("createGoal: " + request.getTitle());

        this.validatePeriod(request.getStartDate(), request.getEndDate());
        this.validateStyle(request.getStyle());
        this.validateBudget(request.getTotalBudget());
        this.validateDraftLimit(userId);

        // 도착지가 city_cost 에 없으면 step2 경비 산출이 불가하므로 등록 단계에서 막는다.
        CityCostVO cityCost = this.findCityCostOrThrow(request.getDestination());

        TravelGoalVO goal = new TravelGoalVO();
        goal.setUserId(userId);
        goal.setTitle(request.getTitle());
        goal.setDeparture(request.getDeparture());
        goal.setDestination(request.getDestination());
        goal.setIsDomestic(DOMESTIC_COUNTRY.equals(cityCost.getCountry()));
        goal.setStyle(request.getStyle().toLowerCase());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setTotalBudget(request.getTotalBudget());
        goal.setStatus(STATUS_DRAFT);
        goal.setCreatedNm(userName);

        this.mapper.insertGoal(goal);

        return goal.getGoalId();
    }

    @Transactional(readOnly = true)
    @Override
    public TravelGoalDraftResponseDTO findCurrentDraft(final Long userId) {
        final TravelGoalVO goal =
                this.mapper.findDraftGoalByUserId(userId);
        return goal == null ? null : TravelGoalDraftResponseDTO.of(goal);
    }

    @Transactional(readOnly = true)
    @Override
    public TravelGoalDetailResponseDTO getGoalDetail(
            final Long userId,
            final Long goalId) {
        final TravelGoalVO goal =
                this.findOwnedGoalOrThrow(userId, goalId);
        final TravelCostVO cost = this.findCostOrThrow(goalId);
        final CityCostVO cityCost =
                this.findCityCostOrThrow(goal.getDestination());
        final TravelCostResponseDTO costResponse =
                TravelCostResponseDTO.of(
                        cost,
                        goal.getStyle(),
                        this.createStyleCostResponses(goal, cityCost, cost));
        final TravelPackageVO selectedPackage = goal.getPackageId() == null
                ? null
                : this.mapper.getPackageById(goal.getPackageId());
        final TravelPackageResponseDTO packageResponse =
                selectedPackage == null
                        ? null
                        : TravelPackageResponseDTO.of(
                                selectedPackage, goal.getPackageId());

        return TravelGoalDetailResponseDTO.of(
                goal,
                costResponse,
                this.readSelectedPlaces(goal),
                packageResponse,
                this.createProductRecommendations(),
                this.createBudgetPlan(userId, goal, cost),
                this.createRegretInsight(userId, goal, cost));
    }

    private TravelRegretInsightResponseDTO createRegretInsight(
            final Long userId,
            final TravelGoalVO goal,
            final TravelCostVO cost) {
        final long remainingAmount =
                this.nvl(cost.getTotalCost()) - this.nvl(goal.getTotalBudget());
        if (remainingAmount <= 0L) {
            return null;
        }

        final int savingsMonths =
                this.calculateRegretSavingsMonths(goal.getStartDate());
        if (savingsMonths <= 0) {
            return null;
        }

        try {
            final RegretSpendingSummaryDTO spending =
                    this.regretService.getSpendingSummary(
                            userId, REGRET_LOOKBACK_MONTHS);
            if (spending == null || spending.getAvgRegretSpending() <= 0L) {
                return null;
            }

            final long avgRegretSpending = spending.getAvgRegretSpending();
            return TravelRegretInsightResponseDTO.builder()
                    .avgRegretSpending(avgRegretSpending)
                    .regretLookbackMonths(REGRET_LOOKBACK_MONTHS)
                    .regretSavingsMonths(savingsMonths)
                    .regretSavingsAmount(
                            avgRegretSpending * savingsMonths)
                    .remainingAmount(remainingAmount)
                    .build();
        } catch (BusinessException exception) {
            log.info(
                    "여행 후회소비 인사이트 정보를 찾을 수 없습니다: userId={}, code={}",
                    userId,
                    exception.getCode());
            return null;
        }
    }

    private int calculateRegretSavingsMonths(final LocalDate startDate) {
        if (startDate == null) {
            return 0;
        }

        final long remainingDays =
                ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        if (remainingDays <= 0L) {
            return 0;
        }

        final long months = Math.max(1L, remainingDays / 30L);
        return (int) Math.min(months, MAX_REGRET_SAVINGS_MONTHS);
    }

    private TravelBudgetPlanResponseDTO createBudgetPlan(
            final Long userId,
            final TravelGoalVO goal,
            final TravelCostVO cost) {
        final long shortfall =
                this.nvl(cost.getTotalCost()) - this.nvl(goal.getTotalBudget());
        if (shortfall <= 0L) {
            return null;
        }

        final TravelUserFinanceDTO finance =
                this.mapper.findUserFinanceByUserId(userId);
        if (finance == null || finance.getDischargeDate() == null) {
            return null;
        }

        if (!goal.getStartDate().isAfter(finance.getDischargeDate())) {
            return this.createInServiceBudgetPlan(shortfall, finance);
        }
        return this.createAfterDischargeBudgetPlan(
                userId, shortfall, finance.getDischargeDate());
    }

    private TravelBudgetPlanResponseDTO createInServiceBudgetPlan(
            final long shortfall,
            final TravelUserFinanceDTO finance) {
        final long monthlySalary = this.nvl(finance.getMonthlySalary());
        final long monthlySaving = this.nvl(finance.getMonthlySaving());
        final long monthlyAvailableAmount =
                Math.max(0L, monthlySalary - monthlySaving);
        final Integer requiredMonths = monthlyAvailableAmount > 0L
                ? this.calculateRequiredMonths(
                        shortfall, monthlyAvailableAmount)
                : null;

        return TravelBudgetPlanResponseDTO.builder()
                .planType(BUDGET_PLAN_IN_SERVICE)
                .shortfall(shortfall)
                .dischargeDate(finance.getDischargeDate())
                .monthlySalary(monthlySalary)
                .monthlySaving(monthlySaving)
                .monthlyAvailableAmount(monthlyAvailableAmount)
                .requiredMonths(requiredMonths)
                .build();
    }

    private TravelBudgetPlanResponseDTO createAfterDischargeBudgetPlan(
            final Long userId,
            final long shortfall,
            final LocalDate dischargeDate) {
        final SimulatorSavingDetailsResponseDTO savingDetails =
                this.findSavingDetailsOrNull(userId);
        final Long expectedMaturityAmount = savingDetails == null
                ? null
                : this.nvl(savingDetails.getTotalReceiptAmount());
        final Long remainingAfterTravel = expectedMaturityAmount == null
                ? null
                : expectedMaturityAmount - shortfall;

        return TravelBudgetPlanResponseDTO.builder()
                .planType(BUDGET_PLAN_AFTER_DISCHARGE)
                .shortfall(shortfall)
                .dischargeDate(dischargeDate)
                .expectedMaturityAmount(expectedMaturityAmount)
                .remainingAfterTravel(remainingAfterTravel)
                .build();
    }

    private SimulatorSavingDetailsResponseDTO findSavingDetailsOrNull(
            final Long userId) {
        try {
            return this.simulatorService.findSavingDetails(userId);
        } catch (BusinessException exception) {
            if ("SIMUL_001".equals(exception.getCode())
                    || "SIMUL_002".equals(exception.getCode())) {
                log.info(
                        "여행 자금 안내용 군적금 정보를 찾을 수 없습니다: userId={}, code={}",
                        userId,
                        exception.getCode());
                return null;
            }
            throw exception;
        }
    }

    private int calculateRequiredMonths(
            final long shortfall,
            final long monthlyAvailableAmount) {
        final long months = shortfall / monthlyAvailableAmount
                + (shortfall % monthlyAvailableAmount == 0L ? 0L : 1L);
        return (int) Math.min(months, Integer.MAX_VALUE);
    }

    @Transactional
    @Override
    public void updateGoal(
            final Long userId,
            final Long goalId,
            final String userName,
            final TravelGoalCreateRequestDTO request) {
        this.validatePeriod(request.getStartDate(), request.getEndDate());
        this.validateStyle(request.getStyle());
        this.validateBudget(request.getTotalBudget());

        final TravelGoalVO savedGoal =
                this.findOwnedDraftGoalOrThrow(userId, goalId);

        final CityCostVO cityCost =
                this.findCityCostOrThrow(request.getDestination());
        final TravelGoalVO goal = new TravelGoalVO();
        goal.setGoalId(goalId);
        goal.setUserId(userId);
        goal.setTitle(request.getTitle());
        goal.setDeparture(request.getDeparture());
        goal.setDestination(request.getDestination());
        goal.setIsDomestic(
                DOMESTIC_COUNTRY.equals(cityCost.getCountry()));
        goal.setStyle(request.getStyle().toLowerCase());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setTotalBudget(request.getTotalBudget());
        goal.setModifiedNm(userName);

        final boolean costInputChanged =
                this.hasCostCalculationInputChanged(savedGoal, request);
        final boolean budgetChanged = !Objects.equals(
                savedGoal.getTotalBudget(), request.getTotalBudget());

        if (this.mapper.updateGoal(goal) == 0) {
            throw BusinessException.conflict(
                    "작성 중인 여행 목표만 수정할 수 있습니다.",
                    "TRAVEL_029");
        }

        if (!costInputChanged && budgetChanged) {
            this.updateRemainingBudget(goalId, userName, request.getTotalBudget());
        }
    }

    private boolean hasCostCalculationInputChanged(
            final TravelGoalVO savedGoal,
            final TravelGoalCreateRequestDTO request) {
        return !Objects.equals(
                        savedGoal.getDeparture(), request.getDeparture())
                || !Objects.equals(
                        savedGoal.getDestination(),
                        request.getDestination())
                || !Objects.equals(
                        savedGoal.getStyle(), request.getStyle())
                || !Objects.equals(
                        savedGoal.getStartDate(), request.getStartDate())
                || !Objects.equals(
                        savedGoal.getEndDate(), request.getEndDate());
    }

    private void updateRemainingBudget(
            final Long goalId,
            final String userName,
            final Long totalBudget) {
        final TravelCostVO cost = this.mapper.findCostByGoalId(goalId);
        if (cost == null) {
            return;
        }

        cost.setRemainingBudget(
                this.nvl(totalBudget) - this.nvl(cost.getTotalCost()));
        cost.setModifiedNm(userName);
        this.mapper.updateCost(cost);
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw BusinessException.badRequest("출발일은 필수입니다.", "TRAVEL_001");
        }
        if (endDate == null) {
            throw BusinessException.badRequest("도착일은 필수입니다.", "TRAVEL_004");
        }
        if (endDate.isBefore(startDate)) {
            throw BusinessException.badRequest("도착일이 출발일보다 빠를 수 없습니다.", "TRAVEL_002");
        }
    }

    // DRAFT 상태라면 추가적인 목표 등록을 차단.
    private void validateDraftLimit(final Long userId) {
        int draftCount = this.mapper.countGoalByStatus(userId, STATUS_DRAFT);
        if (draftCount > 0) {
            throw BusinessException.conflict("작성 중인 여행 목표가 이미 있습니다.", "TRAVEL_003");
        }
    }

    // 여행 스타일이 잘못되면 costByStyle 이 조용히 일반형으로 계산하므로 등록 시점에 막는다.
    private void validateStyle(String style) {
        if (style == null || !VALID_STYLES.contains(style.toLowerCase())) {
            throw BusinessException.badRequest("여행 스타일을 선택해주세요.", "TRAVEL_008");
        }
    }

    private void validateBudget(Long totalBudget) {
        if (totalBudget == null || totalBudget <= 0) {
            throw BusinessException.badRequest("여행 예산을 입력해주세요.", "TRAVEL_007");
        }
    }

    // 도착지의 물가 정보를 조회한다. 없으면 예외. 국내외 구분 도출에도 사용.
    private CityCostVO findCityCostOrThrow(String destination) {
        CityCostVO cityCost = this.mapper.findCityCostByCity(destination);
        if (cityCost == null) {
            throw BusinessException.badRequest(
                    "선택할 수 없는 도착지입니다: " + destination, "TRAVEL_009");
        }
        return cityCost;
    }

    // 저장된 목표와 외부 교통·항공·숙박 API 결과로 예상 경비를 산출해 저장한다.
    @Transactional
    @Override
    public Long createCost(final Long goalId, final String userName) {
        TravelGoalVO goal = this.getGoalOrThrow(goalId);
        CityCostVO cityCost = this.findCityCostOrThrow(goal.getDestination());

        final long commonFlightCost =
                this.calculateTransportationCost(goal, cityCost);
        final long commonHotelCost = this.calculateHotelCost(goal, cityCost);
        final long flightCost = this.findTransportationCostByStyle(
                commonFlightCost, goal.getStyle());
        final long hotelCost = this.findHotelCostByStyle(
                commonHotelCost, goal.getStyle());
        final int days = TravelDateCalculator.calculateTravelDays(
                goal.getStartDate(), goal.getEndDate());
        long dailyCost = this.findDailyCost(cityCost, goal.getStyle());
        long livingCost = dailyCost * days;
        long totalCost = flightCost + hotelCost + livingCost;
        long remainingBudget = this.nvl(goal.getTotalBudget()) - totalCost;

        log.info(String.format(
                "경비 산출: goalId=%d %s(%s) %,d원 x %d일 = %,d원 / 총 %,d원 / 잔여 %,d원",
                goalId, goal.getDestination(), goal.getStyle(),
                dailyCost, days, livingCost, totalCost, remainingBudget));

        final TravelCostVO cost = new TravelCostVO();
        cost.setGoalId(goalId);
        // 기존 컬럼은 모든 스타일 계산의 기준이 되는 일반 가격을 저장한다.
        cost.setFlightCost(commonFlightCost);
        cost.setHotelCost(commonHotelCost);
        cost.setLivingCost(livingCost);
        cost.setTotalCost(totalCost);
        cost.setRemainingBudget(remainingBudget);
        cost.setCreatedNm(userName);
        cost.setModifiedNm(userName);

        // 목표에 저장된 비용이 있으면 현재 값을 갱신하고, 없을 때만 새로 생성한다.
        final TravelCostVO savedCost =
                this.mapper.findCostByGoalId(goalId);
        if (savedCost == null) {
            this.mapper.insertCost(cost);
        } else {
            cost.setCostId(savedCost.getCostId());
            final int updatedRows = this.mapper.updateCost(cost);
            if (updatedRows == 0) {
                cost.setCostId(null);
                this.mapper.insertCost(cost);
            }
        }

        return cost.getCostId();
    }

    private TravelGoalVO getGoalOrThrow(final Long goalId) {
        final TravelGoalVO goal = this.mapper.findGoal(goalId);
        if (goal == null) {
            throw BusinessException.notFound(
                    "여행 목표를 찾을 수 없습니다.", "TRAVEL_005");
        }
        return goal;
    }

    private TravelGoalVO findOwnedGoalOrThrow(
            final Long userId, final Long goalId) {
        final TravelGoalVO goal = this.getGoalOrThrow(goalId);
        if (!goal.getUserId().equals(userId)) {
            throw BusinessException.forbidden(
                    "본인의 여행 목표만 조회할 수 있습니다.",
                    "AUTH_004");
        }
        return goal;
    }

    private TravelGoalVO findOwnedDraftGoalOrThrow(
            final Long userId, final Long goalId) {
        final TravelGoalVO goal = this.findOwnedGoalOrThrow(userId, goalId);
        if (!STATUS_DRAFT.equals(goal.getStatus())) {
            throw BusinessException.conflict(
                    "작성 중인 여행 목표만 수정할 수 있습니다.",
                    "TRAVEL_029");
        }
        return goal;
    }

    private long calculateTransportationCost(
            final TravelGoalVO goal,
            final CityCostVO cityCost) {
        if (Boolean.TRUE.equals(goal.getIsDomestic())) {
            return this.odsayClient.estimateRoundTripCost(
                    goal.getDeparture(), goal.getDestination());
        }

        try {
            return this.flightApiClient.estimateRoundTripCost(
                    cityCost.getCountry(),
                    goal.getStartDate(),
                    goal.getEndDate());
        } catch (final BusinessException exception) {
            if (!FLIGHT_FALLBACK_CODES.contains(exception.getCode())) {
                throw exception;
            }
            return this.calculateFallbackFlightCost(
                    goal, cityCost, exception);
        }
    }

    private long calculateFallbackFlightCost(
            final TravelGoalVO goal,
            final CityCostVO cityCost,
            final BusinessException originalException) {
        final int quarter =
                TravelDateCalculator.findQuarter(goal.getStartDate());
        final TravelQuarterCostSearchDTO search =
                new TravelQuarterCostSearchDTO(
                        cityCost.getCityCostId(), quarter);
        final Long fallbackCost =
                this.mapper.findFlightCostByQuarter(search);

        if (fallbackCost == null || fallbackCost <= 0) {
            log.warn(
                    "항공비 DB 대체값 없음: cityCostId={}, quarter={}",
                    cityCost.getCityCostId(),
                    quarter);
            throw originalException;
        }

        log.warn(
                "Flight API 대신 DB 항공비 사용: "
                        + "cityCostId={}, quarter={}, cost={}",
                cityCost.getCityCostId(),
                quarter,
                fallbackCost);
        return fallbackCost;
    }

    private long calculateHotelCost(
            final TravelGoalVO goal,
            final CityCostVO cityCost) {
        final int nights = TravelDateCalculator.calculateNights(
                goal.getStartDate(), goal.getEndDate());
        if (nights == 0) {
            return 0L;
        }

        try {
            return this.bookingApiClient.estimateHotelCost(
                    cityCost.getCountry(),
                    goal.getDestination(),
                    goal.getStartDate(),
                    goal.getEndDate());
        } catch (final BusinessException exception) {
            if (!HOTEL_FALLBACK_CODES.contains(exception.getCode())) {
                throw exception;
            }
            return this.calculateFallbackHotelCost(
                    goal, cityCost, nights, exception);
        }
    }

    private long calculateFallbackHotelCost(
            final TravelGoalVO goal,
            final CityCostVO cityCost,
            final int nights,
            final BusinessException originalException) {
        final int quarter =
                TravelDateCalculator.findQuarter(goal.getStartDate());
        final TravelQuarterCostSearchDTO search =
                new TravelQuarterCostSearchDTO(
                        cityCost.getCityCostId(), quarter);
        final Long nightlyCost =
                this.mapper.findHotelCostByQuarter(search);

        if (nightlyCost == null || nightlyCost <= 0) {
            log.warn(
                    "숙박비 DB 대체값 없음: cityCostId={}, quarter={}",
                    cityCost.getCityCostId(),
                    quarter);
            throw originalException;
        }

        try {
            final long fallbackCost = Math.multiplyExact(nightlyCost, nights);
            log.warn(
                    "Booking API 대신 DB 숙박비 사용: "
                            + "cityCostId={}, quarter={}, nightlyCost={}, "
                            + "nights={}, cost={}",
                    cityCost.getCityCostId(),
                    quarter,
                    nightlyCost,
                    nights,
                    fallbackCost);
            return fallbackCost;
        } catch (final ArithmeticException exception) {
            log.warn(
                    "숙박비 DB 대체값 계산 범위 초과: "
                            + "cityCostId={}, nights={}",
                    cityCost.getCityCostId(),
                    nights);
            throw originalException;
        }
    }

    private long findTransportationCostByStyle(
            final long commonCost,
            final String style) {
        return this.applyStyleRate(
                commonCost,
                style,
                TRANSPORT_SAVING_RATE,
                TRANSPORT_PREMIUM_RATE);
    }

    private long findHotelCostByStyle(
            final long commonCost,
            final String style) {
        return this.applyStyleRate(
                commonCost,
                style,
                HOTEL_SAVING_RATE,
                HOTEL_PREMIUM_RATE);
    }

    private long applyStyleRate(
            final long commonCost,
            final String style,
            final BigDecimal savingRate,
            final BigDecimal premiumRate) {
        final BigDecimal rate;
        if ("saving".equals(style)) {
            rate = savingRate;
        } else if ("common".equals(style)) {
            rate = BigDecimal.ONE;
        } else if ("premium".equals(style)) {
            rate = premiumRate;
        } else {
            throw BusinessException.badRequest(
                    "여행 스타일을 선택해주세요.",
                    "TRAVEL_008");
        }

        return TravelPriceCalculator.applyRate(commonCost, rate);
    }

    private long findDailyCost(CityCostVO cityCost, String style) {
        Long dailyCost = cityCost.costByStyle(style);
        if (dailyCost == null) {
            throw BusinessException.badRequest(
                    "물가 정보를 찾을 수 없습니다: " + cityCost.getCity(),
                    "TRAVEL_010");
        }
        return dailyCost;
    }

    private long nvl(Long value) {
        if (value == null) {
            return 0L;
        }
        return value;
    }

    @Transactional(readOnly = true)
    @Override
    public TravelCostResponseDTO findCost(final Long goalId) {
        final TravelGoalVO goal = this.getGoalOrThrow(goalId);
        final TravelCostVO cost = this.findCostOrThrow(goalId);
        final CityCostVO cityCost =
                this.findCityCostOrThrow(goal.getDestination());
        return TravelCostResponseDTO.of(
                cost,
                goal.getStyle(),
                this.createStyleCostResponses(goal, cityCost, cost));
    }

    @Transactional
    @Override
    public void updateCostStyle(
            final Long userId,
            final Long goalId,
            final String userName,
            final TravelCostStyleUpdateRequestDTO request) {
        final String requestedStyle =
                request == null ? null : request.getStyle();
        this.validateStyle(requestedStyle);

        final TravelGoalVO goal =
                this.findOwnedDraftGoalOrThrow(userId, goalId);
        final TravelCostVO cost = this.findCostOrThrow(goalId);
        final CityCostVO cityCost =
                this.findCityCostOrThrow(goal.getDestination());
        final String style = requestedStyle.toLowerCase();
        final long flightCost = this.findTransportationCostByStyle(
                this.nvl(cost.getFlightCost()), style);
        final long hotelCost = this.findHotelCostByStyle(
                this.nvl(cost.getHotelCost()), style);
        final int days = TravelDateCalculator.calculateTravelDays(
                goal.getStartDate(), goal.getEndDate());
        final long livingCost = this.findDailyCost(cityCost, style) * days;
        final long totalCost = flightCost + hotelCost + livingCost;

        if (this.mapper.updateGoalStyle(
                goalId, userId, style, userName) == 0) {
            throw BusinessException.conflict(
                    "작성 중인 여행 목표만 수정할 수 있습니다.",
                    "TRAVEL_029");
        }

        cost.setLivingCost(livingCost);
        cost.setTotalCost(totalCost);
        cost.setRemainingBudget(
                this.nvl(goal.getTotalBudget()) - totalCost);
        cost.setModifiedNm(userName);
        if (this.mapper.updateCost(cost) == 0) {
            throw BusinessException.conflict(
                    "예상 경비를 갱신하지 못했습니다.",
                    "TRAVEL_029");
        }
    }

    private List<TravelStyleCostResponseDTO> createStyleCostResponses(
            final TravelGoalVO goal,
            final CityCostVO cityCost,
            final TravelCostVO cost) {
        final int days = TravelDateCalculator.calculateTravelDays(
                goal.getStartDate(), goal.getEndDate());
        final long commonFlightCost = this.nvl(cost.getFlightCost());
        final long commonHotelCost = this.nvl(cost.getHotelCost());

        return VALID_STYLES.stream()
                .map(style -> {
                    final long flightCost =
                            this.findTransportationCostByStyle(
                                    commonFlightCost, style);
                    final long hotelCost = this.findHotelCostByStyle(
                            commonHotelCost, style);
                    final long livingCost =
                            this.findDailyCost(cityCost, style) * days;
                    final long totalCost =
                            flightCost + hotelCost + livingCost;
                    return TravelStyleCostResponseDTO.of(
                            style,
                            flightCost,
                            hotelCost,
                            livingCost,
                            totalCost,
                            this.nvl(goal.getTotalBudget()) - totalCost);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<TravelPlaceResponseDTO> searchPlaces(
            final Long goalId,
            final String category) {
        final String normalizedCategory = category == null
                ? ""
                : category.trim().toLowerCase();
        if (!"attraction".equals(normalizedCategory)
                && !"restaurant".equals(normalizedCategory)) {
            throw BusinessException.badRequest(
                    "검색 유형은 attraction 또는 restaurant이어야 합니다.",
                    "TRAVEL_025");
        }

        final TravelGoalVO goal = this.getGoalOrThrow(goalId);
        final CityCostVO cityCost =
                this.findCityCostOrThrow(goal.getDestination());
        return this.serpApiClient.searchPlaces(
                cityCost.getCountry(),
                goal.getDestination(),
                normalizedCategory);
    }

    @Transactional
    @Override
    public void updatePlaces(
            final Long goalId,
            final String userName,
            final TravelPlacesUpdateRequestDTO request) {
        this.getGoalOrThrow(goalId);
        if (request == null || request.getPlaces() == null) {
            throw BusinessException.badRequest(
                    "관심 여행지 목록을 입력해주세요.",
                    "TRAVEL_026");
        }

        try {
            final String places =
                    this.objectMapper.writeValueAsString(request.getPlaces());
            this.mapper.updateGoalPlaces(goalId, places, userName);
        } catch (final JsonProcessingException e) {
            log.warn("관심 여행지 JSON 변환 오류: goalId={}", goalId, e);
            throw BusinessException.badRequest(
                    "관심 여행지 정보를 저장할 수 없습니다.",
                    "TRAVEL_027");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<TravelPlaceSelectionDTO> getSelectedPlaces(
            final Long goalId) {
        final TravelGoalVO goal = this.getGoalOrThrow(goalId);
        return this.readSelectedPlaces(goal);
    }

    private List<TravelPlaceSelectionDTO> readSelectedPlaces(
            final TravelGoalVO goal) {
        if (goal.getPlaces() == null || goal.getPlaces().trim().isEmpty()) {
            return List.of();
        }

        try {
            return this.objectMapper.readValue(
                    goal.getPlaces(),
                    new TypeReference<List<TravelPlaceSelectionDTO>>() {
                    });
        } catch (final JsonProcessingException e) {
            log.warn(
                    "관심 여행지 JSON 파싱 오류: goalId={}",
                    goal.getGoalId(),
                    e);
            throw new BusinessException(
                    "저장된 관심 여행지 정보를 불러올 수 없습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRAVEL_028");
        }
    }

    @Transactional
    @Override
    public List<TravelPackageResponseDTO> findPackages(
            final Long goalId,
            final String userName) {
        final TravelGoalVO goal = this.getGoalOrThrow(goalId);
        final CityCostVO cityCost =
                this.findCityCostOrThrow(goal.getDestination());
        final TravelCostVO travelCost = this.findCostOrThrow(goalId);
        final TravelPackageSearchDTO packageSearch =
                this.createPackageSearch(
                        goal, cityCost, travelCost.getTotalCost());

        final String cacheKey = goal.getDestination()
                + ":"
                + goal.getStartDate();
        if (!this.hasFreshPackageQuery(cacheKey)) {
            try {
                final List<TravelPackageVO> crawledPackages =
                        this.yellowBalloonClient.searchPackages(
                                cityCost.getCountry(),
                                goal.getDestination(),
                                goal.getStartDate());
                crawledPackages.stream()
                        .filter(travelPackage -> this.isAvailablePackage(
                                travelPackage,
                                goal.getStartDate()))
                        .forEach(travelPackage ->
                                this.savePackage(travelPackage, userName));
                this.packageQueryCache.put(
                        cacheKey, LocalDateTime.now());
            } catch (final BusinessException exception) {
                log.warn(
                        "노랑풍선 조회 대신 DB 패키지 사용: "
                                + "goalId={}, destination={}, code={}",
                        goalId,
                        goal.getDestination(),
                        exception.getCode());
            }
        }

        final Set<String> selectedGoodsCodes = new HashSet<>();
        final List<TravelPackageVO> packages =
                this.mapper.findPackagesByDestination(packageSearch)
                        .stream()
                        .filter(travelPackage -> this.isAvailablePackage(
                                travelPackage,
                                goal.getStartDate()))
                        .filter(travelPackage -> selectedGoodsCodes.add(
                                this.findOriginalGoodsCode(
                                        travelPackage.getGoodsCode())))
                        .limit(10)
                        .collect(Collectors.toList());
        if (packages.isEmpty()) {
            return List.of();
        }

        return packages.stream()
                .map(travelPackage -> TravelPackageResponseDTO.of(
                        travelPackage, goal.getPackageId()))
                .collect(Collectors.toList());
    }

    private String findOriginalGoodsCode(final String eventCode) {
        if (eventCode == null) {
            return "";
        }
        final int separatorIndex = eventCode.indexOf('-');
        return separatorIndex < 0
                ? eventCode
                : eventCode.substring(0, separatorIndex);
    }

    private boolean hasFreshPackageQuery(final String cacheKey) {
        final LocalDateTime cacheBoundary =
                LocalDateTime.now().minusHours(PACKAGE_CACHE_HOURS);
        final LocalDateTime cachedAt =
                this.packageQueryCache.get(cacheKey);
        return cachedAt != null && cachedAt.isAfter(cacheBoundary);
    }

    private void savePackage(
            final TravelPackageVO travelPackage, final String userName) {
        final TravelPackageVO savedPackage =
                this.mapper.findPackageByGoodsCode(
                        travelPackage.getGoodsCode());
        travelPackage.setCreatedNm(userName);
        travelPackage.setModifiedNm(userName);

        if (savedPackage == null) {
            this.mapper.insertPackage(travelPackage);
            return;
        }

        travelPackage.setPackageId(savedPackage.getPackageId());
        this.mapper.updatePackage(travelPackage);
    }

    private TravelPackageSearchDTO createPackageSearch(
            final TravelGoalVO goal,
            final CityCostVO cityCost,
            final Long maxPrice) {
        return TravelPackageSearchDTO.builder()
                .country(cityCost.getCountry())
                .destination(goal.getDestination())
                .departureDate(goal.getStartDate())
                .maxPrice(maxPrice)
                .build();
    }

    private boolean isAvailablePackage(
            final TravelPackageVO travelPackage,
            final LocalDate startDate) {
        if (startDate == null
                || travelPackage.getDeparturePeriod() == null) {
            return true;
        }

        final String[] period =
                travelPackage.getDeparturePeriod().split("\\s*~\\s*");
        if (period.length != 2) {
            return true;
        }

        try {
            final LocalDate packageDepartureDate =
                    LocalDate.parse(period[0], PACKAGE_DATE_FORMAT);
            return startDate.equals(packageDepartureDate);
        } catch (final DateTimeParseException exception) {
            log.warn("패키지 출발기간 파싱 실패: goodsCode={}, period={}",
                    travelPackage.getGoodsCode(),
                    travelPackage.getDeparturePeriod());
            return true;
        }
    }

    @Transactional
    @Override
    public void updatePackage(
            final Long userId,
            final Long goalId,
            final String userName,
            final TravelPackageUpdateRequestDTO request) {
        final TravelGoalVO goal =
                this.findOwnedDraftGoalOrThrow(userId, goalId);
        if (request == null) {
            throw BusinessException.badRequest(
                    "여행 패키지 선택 정보를 입력해주세요.",
                    "TRAVEL_032");
        }

        if (request.getPackageId() == null) {
            if (this.mapper.updateGoalPackage(
                    goalId, null, userName) == 0) {
                throw BusinessException.conflict(
                        "작성 중인 여행 목표만 수정할 수 있습니다.",
                        "TRAVEL_029");
            }
            return;
        }

        final TravelPackageVO travelPackage = this.mapper
                .findPackagesByDestination(
                        this.createPackageSearch(
                                goal,
                                this.findCityCostOrThrow(
                                        goal.getDestination()),
                                this.findCostOrThrow(goalId)
                                        .getTotalCost()))
                .stream()
                .filter(item -> request.getPackageId()
                        .equals(item.getPackageId()))
                .findFirst()
                .orElseThrow(() -> BusinessException.badRequest(
                        "선택할 수 없는 여행 패키지 상품입니다.",
                        "TRAVEL_033"));

        if (this.mapper.updateGoalPackage(
                goalId,
                travelPackage.getPackageId(),
                userName) == 0) {
            throw BusinessException.conflict(
                    "작성 중인 여행 목표만 수정할 수 있습니다.",
                    "TRAVEL_029");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public TravelProductRecommendationResponseDTO findProducts(
            final Long userId,
            final Long goalId) {
        this.findOwnedGoalOrThrow(userId, goalId);
        return this.createProductRecommendations();
    }

    private TravelProductRecommendationResponseDTO
            createProductRecommendations() {
        final List<TravelFinancialProductResponseDTO> cards =
                this.productService
                        .findCardProductListByCategory(
                                ROADMAP_CATEGORY_TRAVEL)
                        .stream()
                        .map(TravelFinancialProductResponseDTO::ofCard)
                        .toList();

        final List<TravelFinancialProductResponseDTO> savings =
                this.mapper
                        .findTravelSavingProductList(
                                TRAVEL_SAVING_CODE_PREFIX)
                        .stream()
                        .map(TravelFinancialProductResponseDTO::ofSaving)
                        .toList();

        final List<TravelFinancialProductResponseDTO> insurances =
                this.mapper.findTravelInsuranceList()
                        .stream()
                        .map(TravelFinancialProductResponseDTO::ofInsurance)
                        .toList();

        return TravelProductRecommendationResponseDTO.of(
                cards,
                savings,
                insurances);
    }

    @Transactional
    @Override
    public void confirmGoal(
            final Long userId,
            final Long goalId,
            final String userName) {
        final TravelGoalVO goal =
                this.findOwnedDraftGoalOrThrow(userId, goalId);
        goal.setModifiedNm(userName);
        if (this.mapper.confirmGoal(goal) == 0) {
            throw BusinessException.conflict(
                    "작성 중인 여행 목표만 저장할 수 있습니다.",
                    "TRAVEL_029");
        }
    }

    private TravelCostVO findCostOrThrow(final Long goalId) {
        final TravelCostVO travelCost =
                this.mapper.findCostByGoalId(goalId);
        if (travelCost == null || travelCost.getTotalCost() == null) {
            throw BusinessException.notFound(
                    "산출된 예상 경비가 없습니다.",
                    "TRAVEL_006");
        }
        return travelCost;
    }

}
