package org.scoula.travel.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.travel.domain.CityCostVO;
import org.scoula.travel.domain.TravelCostVO;
import org.scoula.travel.domain.TravelGoalVO;
import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.dto.TravelCostCreateRequestDTO;
import org.scoula.travel.dto.TravelCostResponseDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;
import org.scoula.travel.mapper.TravelMapper;

@Log4j2
@Service
@RequiredArgsConstructor
public class TravelServiceImpl implements TravelService {

    private final TravelMapper mapper;

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
        return this.mapper.getCityCostList(country).stream()
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
            throw BusinessException.badRequest("도착일은 필수입니다.", "TRAVEL_001");
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
        CityCostVO cityCost = this.mapper.getCityCostByCity(destination);
        if (cityCost == null) {
            throw BusinessException.badRequest(
                    "선택할 수 없는 도착지입니다: " + destination, "TRAVEL_004");
        }
        return cityCost;
    }

    // 저장된 목표를 기준으로 예상 경비를 산출해 저장한다.
    // flightCost와 hotelCost는 아직 외부 API 미연동 구간
    @Transactional
    @Override
    public Long createCost(Long goalId, TravelCostCreateRequestDTO request) {
        TravelGoalVO goal = this.getGoalOrThrow(goalId);

        long flightCost = this.nvl(request == null ? null : request.getFlightCost());
        long hotelCost = this.nvl(request == null ? null : request.getHotelCost());

        int days = this.calculateDays(goal.getStartDate(), goal.getEndDate());
        long dailyCost = this.findDailyCost(goal.getDestination(), goal.getStyle());
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
        TravelGoalVO goal = this.mapper.getGoal(goalId);
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

    private long findDailyCost(String destination, String style) {
        CityCostVO cityCost = this.mapper.getCityCostByCity(destination);
        Long dailyCost = cityCost == null ? null : cityCost.costByStyle(style);
        if (dailyCost == null) {
            throw BusinessException.badRequest(
                    "물가 정보를 찾을 수 없습니다: " + destination, "TRAVEL_004");
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
        TravelCostVO cost = this.mapper.getCostByGoalId(goalId);
        if (cost == null) {
            throw BusinessException.notFound(
                    "산출된 예상 경비가 없습니다.", "TRAVEL_006");
        }
        return TravelCostResponseDTO.of(cost);
    }

}
