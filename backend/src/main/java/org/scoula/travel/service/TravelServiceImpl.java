package org.scoula.travel.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
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
import org.scoula.travel.client.BookingApiClient;
import org.scoula.travel.client.FlightApiClient;
import org.scoula.travel.client.OdsayClient;
import org.scoula.travel.client.SerpApiClient;
import org.scoula.travel.domain.CityCostVO;
import org.scoula.travel.domain.TravelCostVO;
import org.scoula.travel.domain.TravelGoalVO;
import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.dto.TravelCostCreateRequestDTO;
import org.scoula.travel.dto.TravelCostResponseDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;
import org.scoula.travel.dto.TravelPlaceResponseDTO;
import org.scoula.travel.dto.TravelPlaceSelectionDTO;
import org.scoula.travel.dto.TravelPlacesUpdateRequestDTO;
import org.scoula.travel.mapper.TravelMapper;

@Log4j2
@Service
@RequiredArgsConstructor
public class TravelServiceImpl implements TravelService {

    private final TravelMapper mapper;
    private final OdsayClient odsayClient;
    private final FlightApiClient flightApiClient;
    private final BookingApiClient bookingApiClient;
    private final SerpApiClient serpApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 로그인 사용자 임시 고정값
    // 인증 모듈 완성 후 컨트롤러에서 CustomUser를 받아 넘기도록 교체.
    private static final Long LOGIN_USER_ID = 1L;
    private static final String LOGIN_USER_NAME = "hobin@kbthink.com";

    // 목표 상태값 (DRAFT / CONFIRMED / ARCHIVED)
    private static final String STATUS_DRAFT = "DRAFT";

    private static final String DOMESTIC_COUNTRY = "대한민국";

    // 여행 스타일. city_cost 의 saving_cost / common_cost / premium_cost 에 대응.
    private static final List<String> VALID_STYLES =
            Arrays.asList("saving", "common", "premium");

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
    public Long createCost(Long goalId, TravelCostCreateRequestDTO request) {
        TravelGoalVO goal = this.getGoalOrThrow(goalId);
        CityCostVO cityCost = this.findCityCostOrThrow(goal.getDestination());

        long flightCost = Boolean.TRUE.equals(goal.getIsDomestic())
                ? this.odsayClient.estimateRoundTripCost(
                        goal.getDeparture(), goal.getDestination())
                : this.flightApiClient.estimateRoundTripCost(
                        cityCost.getCountry(),
                        goal.getStartDate(),
                        goal.getEndDate());
        long hotelCost = this.bookingApiClient.estimateHotelCost(
                cityCost.getCountry(),
                goal.getDestination(),
                goal.getStartDate(),
                goal.getEndDate());

        int days = this.calculateDays(goal.getStartDate(), goal.getEndDate());
        long dailyCost = this.findDailyCost(cityCost, goal.getStyle());
        long livingCost = dailyCost * days;
        long totalCost = flightCost + hotelCost + livingCost;
        long remainingBudget = this.nvl(goal.getTotalBudget()) - totalCost;

        log.info(String.format(
                "경비 산출: goalId=%d %s(%s) %,d원 x %d일 = %,d원 / 총 %,d원 / 잔여 %,d원",
                goalId, goal.getDestination(), goal.getStyle(),
                dailyCost, days, livingCost, totalCost, remainingBudget));

        TravelCostVO cost = new TravelCostVO();
        cost.setGoalId(goalId);
        cost.setFlightCost(flightCost);
        cost.setHotelCost(hotelCost);
        cost.setLivingCost(livingCost);
        cost.setTotalCost(totalCost);
        cost.setRemainingBudget(remainingBudget);
        cost.setCreatedNm(LOGIN_USER_NAME);

        // 같은 목표로 다시 호출되면(뒤로가기·새로고침 등) 기존 결과를 소프트 삭제하고 새로 적재한다.
        this.mapper.deleteCostByGoalId(goalId, LOGIN_USER_NAME);
        this.mapper.insertCost(cost);

        return cost.getCostId();
    }

    private TravelGoalVO getGoalOrThrow(Long goalId) {
        TravelGoalVO goal = this.mapper.findGoal(goalId);
        if (goal == null) {
            throw BusinessException.notFound(
                    "여행 목표를 찾을 수 없습니다.", "TRAVEL_005");
        }
        return goal;
    }

    // 총 일수 = 종료일 - 시작일 + 1 (3박4일이면 4)
    private int calculateDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
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

    @Override
    public TravelCostResponseDTO findCost(Long goalId) {
        this.getGoalOrThrow(goalId);
        TravelCostVO cost = this.mapper.findCostByGoalId(goalId);
        if (cost == null) {
            throw BusinessException.notFound(
                    "산출된 예상 경비가 없습니다.", "TRAVEL_006");
        }
        return TravelCostResponseDTO.of(cost);
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

}
