package org.scoula.travel.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import org.scoula.travel.dto.TravelFinancialProductResponseDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;
import org.scoula.travel.dto.TravelGoalDraftResponseDTO;
import org.scoula.travel.dto.TravelPlaceResponseDTO;
import org.scoula.travel.dto.TravelPlaceSelectionDTO;
import org.scoula.travel.dto.TravelPlacesUpdateRequestDTO;
import org.scoula.travel.dto.TravelQuarterCostSearchDTO;
import org.scoula.travel.dto.TravelPackageResponseDTO;
import org.scoula.travel.dto.TravelPackageSearchDTO;
import org.scoula.travel.dto.TravelPackageUpdateRequestDTO;
import org.scoula.travel.dto.TravelProductRecommendationResponseDTO;
import org.scoula.travel.dto.TravelProductSelectionDTO;
import org.scoula.travel.dto.TravelProductsUpdateRequestDTO;
import org.scoula.travel.mapper.TravelMapper;
import org.scoula.travel.util.TravelDateCalculator;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, LocalDateTime> packageQueryCache =
            new ConcurrentHashMap<>();

    // 로그인 사용자 임시 고정값
    // 인증 모듈 완성 후 컨트롤러에서 CustomUser를 받아 넘기도록 교체.
    private static final Long LOGIN_USER_ID = 1L;
    private static final String LOGIN_USER_NAME = "hobin@kbthink.com";

    // 목표 상태값 (DRAFT / CONFIRMED / ARCHIVED)
    private static final String STATUS_DRAFT = "DRAFT";

    private static final String DOMESTIC_COUNTRY = "대한민국";
    private static final DateTimeFormatter PACKAGE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final int PACKAGE_CACHE_HOURS = 6;
    private static final int ROADMAP_CATEGORY_TRAVEL = 1;
    private static final String TRAVEL_SAVING_CODE_PREFIX = "TRV-";

    // 여행 스타일. city_cost 의 saving_cost / common_cost / premium_cost 에 대응.
    private static final List<String> VALID_STYLES =
            Arrays.asList("saving", "common", "premium");

    private static final Set<String> FLIGHT_FALLBACK_CODES =
            Set.of("TRAVEL_017", "TRAVEL_018", "TRAVEL_019");

    private static final Set<String> HOTEL_FALLBACK_CODES =
            Set.of("TRAVEL_020", "TRAVEL_021", "TRAVEL_022");

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
    public Long createGoal(TravelGoalCreateRequestDTO request) {
        log.info("createGoal: " + request.getTitle());

        this.validatePeriod(request.getStartDate(), request.getEndDate());
        this.validateStyle(request.getStyle());
        this.validateBudget(request.getTotalBudget());
        this.validateDraftLimit();

        // 도착지가 city_cost 에 없으면 step2 경비 산출이 불가하므로 등록 단계에서 막는다.
        CityCostVO cityCost = this.findCityCostOrThrow(request.getDestination());

        TravelGoalVO goal = new TravelGoalVO();
        goal.setUserId(LOGIN_USER_ID);
        goal.setTitle(request.getTitle());
        goal.setDeparture(request.getDeparture());
        goal.setDestination(request.getDestination());
        goal.setIsDomestic(DOMESTIC_COUNTRY.equals(cityCost.getCountry()));
        goal.setStyle(request.getStyle().toLowerCase());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setTotalBudget(request.getTotalBudget());
        goal.setStatus(STATUS_DRAFT);
        goal.setCreatedNm(LOGIN_USER_NAME);

        this.mapper.insertGoal(goal);

        return goal.getGoalId();
    }

    @Transactional(readOnly = true)
    @Override
    public TravelGoalDraftResponseDTO findCurrentDraft() {
        final TravelGoalVO goal =
                this.mapper.findDraftGoalByUserId(LOGIN_USER_ID);
        return goal == null ? null : TravelGoalDraftResponseDTO.of(goal);
    }

    @Transactional
    @Override
    public void updateGoal(
            final Long goalId,
            final TravelGoalCreateRequestDTO request) {
        this.validatePeriod(request.getStartDate(), request.getEndDate());
        this.validateStyle(request.getStyle());
        this.validateBudget(request.getTotalBudget());

        final TravelGoalVO savedGoal =
                this.findOwnedDraftGoalOrThrow(goalId);

        final CityCostVO cityCost =
                this.findCityCostOrThrow(request.getDestination());
        final TravelGoalVO goal = new TravelGoalVO();
        goal.setGoalId(goalId);
        goal.setUserId(LOGIN_USER_ID);
        goal.setTitle(request.getTitle());
        goal.setDeparture(request.getDeparture());
        goal.setDestination(request.getDestination());
        goal.setIsDomestic(
                DOMESTIC_COUNTRY.equals(cityCost.getCountry()));
        goal.setStyle(request.getStyle().toLowerCase());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setTotalBudget(request.getTotalBudget());
        goal.setModifiedNm(LOGIN_USER_NAME);

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
            this.updateRemainingBudget(goalId, request.getTotalBudget());
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
            final Long totalBudget) {
        final TravelCostVO cost = this.mapper.findCostByGoalId(goalId);
        if (cost == null) {
            return;
        }

        cost.setRemainingBudget(
                this.nvl(totalBudget) - this.nvl(cost.getTotalCost()));
        cost.setModifiedNm(LOGIN_USER_NAME);
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
    private void validateDraftLimit() {
        int draftCount = this.mapper.countGoalByStatus(LOGIN_USER_ID, STATUS_DRAFT);
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
    public Long createCost(final Long goalId) {
        TravelGoalVO goal = this.getGoalOrThrow(goalId);
        CityCostVO cityCost = this.findCityCostOrThrow(goal.getDestination());

        final long flightCost =
                this.calculateTransportationCost(goal, cityCost);
        final long hotelCost = this.calculateHotelCost(goal, cityCost);
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
        cost.setFlightCost(flightCost);
        cost.setHotelCost(hotelCost);
        cost.setLivingCost(livingCost);
        cost.setTotalCost(totalCost);
        cost.setRemainingBudget(remainingBudget);
        cost.setCreatedNm(LOGIN_USER_NAME);
        cost.setModifiedNm(LOGIN_USER_NAME);

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

    private TravelGoalVO findOwnedGoalOrThrow(final Long goalId) {
        final TravelGoalVO goal = this.getGoalOrThrow(goalId);
        if (!LOGIN_USER_ID.equals(goal.getUserId())) {
            throw BusinessException.forbidden(
                    "본인의 여행 목표만 조회할 수 있습니다.",
                    "AUTH_004");
        }
        return goal;
    }

    private TravelGoalVO findOwnedDraftGoalOrThrow(final Long goalId) {
        final TravelGoalVO goal = this.findOwnedGoalOrThrow(goalId);
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
        this.getGoalOrThrow(goalId);
        return TravelCostResponseDTO.of(this.findCostOrThrow(goalId));
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
            this.mapper.updateGoalPlaces(goalId, places, LOGIN_USER_NAME);
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
        if (goal.getPlaces() == null || goal.getPlaces().trim().isEmpty()) {
            return List.of();
        }

        try {
            return this.objectMapper.readValue(
                    goal.getPlaces(),
                    new TypeReference<List<TravelPlaceSelectionDTO>>() {
                    });
        } catch (final JsonProcessingException e) {
            log.warn("관심 여행지 JSON 파싱 오류: goalId={}", goalId, e);
            throw new BusinessException(
                    "저장된 관심 여행지 정보를 불러올 수 없습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRAVEL_028");
        }
    }

    @Transactional
    @Override
    public List<TravelPackageResponseDTO> findPackages(
            final Long goalId) {
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
                        .forEach(this::savePackage);
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

    private void savePackage(final TravelPackageVO travelPackage) {
        final TravelPackageVO savedPackage =
                this.mapper.findPackageByGoodsCode(
                        travelPackage.getGoodsCode());
        travelPackage.setCreatedNm(LOGIN_USER_NAME);
        travelPackage.setModifiedNm(LOGIN_USER_NAME);

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
            final Long goalId,
            final TravelPackageUpdateRequestDTO request) {
        final TravelGoalVO goal =
                this.findOwnedDraftGoalOrThrow(goalId);
        if (request == null) {
            throw BusinessException.badRequest(
                    "여행 패키지 선택 정보를 입력해주세요.",
                    "TRAVEL_032");
        }

        if (request.getPackageId() == null) {
            if (this.mapper.updateGoalPackage(
                    goalId, null, LOGIN_USER_NAME) == 0) {
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
                LOGIN_USER_NAME) == 0) {
            throw BusinessException.conflict(
                    "작성 중인 여행 목표만 수정할 수 있습니다.",
                    "TRAVEL_029");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public TravelProductRecommendationResponseDTO findProducts(
            final Long goalId) {
        final TravelGoalVO goal = this.findOwnedGoalOrThrow(goalId);
        return this.createProductRecommendations(goal);
    }

    private TravelProductRecommendationResponseDTO
            createProductRecommendations(final TravelGoalVO goal) {
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
                insurances,
                this.findSelectedProducts(goal));
    }

    private List<TravelProductSelectionDTO> findSelectedProducts(
            final TravelGoalVO goal) {
        if (goal.getProducts() == null
                || goal.getProducts().trim().isEmpty()) {
            return List.of();
        }

        try {
            return this.objectMapper.readValue(
                    goal.getProducts(),
                    new TypeReference<List<TravelProductSelectionDTO>>() {
                    });
        } catch (final JsonProcessingException exception) {
            log.warn("금융상품 JSON 파싱 오류: goalId={}",
                    goal.getGoalId(), exception);
            throw new BusinessException(
                    "저장된 금융상품 정보를 불러올 수 없습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRAVEL_034");
        }
    }

    @Transactional
    @Override
    public void updateProducts(
            final Long goalId,
            final TravelProductsUpdateRequestDTO request) {
        final TravelGoalVO goal =
                this.findOwnedDraftGoalOrThrow(goalId);
        if (request == null || request.getProducts() == null) {
            throw BusinessException.badRequest(
                    "관심 금융상품 목록을 입력해주세요.",
                    "TRAVEL_036");
        }

        this.validateProducts(request.getProducts());
        try {
            goal.setProducts(this.objectMapper.writeValueAsString(
                    request.getProducts()));
            goal.setModifiedNm(LOGIN_USER_NAME);
            if (this.mapper.updateGoalProducts(goal) == 0) {
                throw BusinessException.conflict(
                        "작성 중인 여행 목표만 수정할 수 있습니다.",
                        "TRAVEL_029");
            }
        } catch (final JsonProcessingException exception) {
            log.warn("금융상품 JSON 변환 오류: goalId={}",
                    goalId, exception);
            throw new BusinessException(
                    "금융상품 정보를 저장할 수 없습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRAVEL_035");
        }
    }

    private void validateProducts(
            final List<TravelProductSelectionDTO> products) {
        for (final TravelProductSelectionDTO product : products) {
            if (product == null
                    || product.getType() == null
                    || product.getType().trim().isEmpty()
                    || product.getProductId() == null
                    || product.getProductId().trim().isEmpty()
                    || product.getName() == null
                    || product.getName().trim().isEmpty()) {
                throw BusinessException.badRequest(
                        "관심 금융상품 정보를 확인해주세요.",
                        "TRAVEL_037");
            }
        }
    }

    @Transactional
    @Override
    public void confirmGoal(final Long goalId) {
        final TravelGoalVO goal =
                this.findOwnedDraftGoalOrThrow(goalId);
        if (goal.getProducts() == null) {
            throw BusinessException.badRequest(
                    "관심 금융상품 저장을 먼저 완료해주세요.",
                    "TRAVEL_038");
        }

        goal.setModifiedNm(LOGIN_USER_NAME);
        this.mapper.archiveConfirmedGoalByUserId(goal);
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
