package org.scoula.travel.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.travel.domain.TravelGoalVO;
import org.scoula.travel.dto.CityCostResponseDTO;
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

    // 작성 중 상태 테스트
    private static final String STATUS_DRAFT = "DRAFT";


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

        LocalDate startDate = this.parseDate(request.getStartDate(), "startDate");
        LocalDate endDate = this.parseDate(request.getEndDate(), "endDate");
        this.validatePeriod(startDate, endDate);
        this.validateDraftLimit();

        // 도착지가 city_cost 에 없으면 step2 경비 산출이 불가하므로 등록 단계에서 막는다.
        this.validateDestination(request.getDestination());

        TravelGoalVO goal = new TravelGoalVO();
        goal.setUserId(LOGIN_USER_ID);
        goal.setTitle(request.getTitle());
        goal.setDeparture(request.getDeparture());
        goal.setDestination(request.getDestination());
        goal.setIsDomestic(request.getIsDomestic());
        goal.setStyle(request.getStyle());
        goal.setStartDate(Date.valueOf(startDate));
        goal.setEndDate(Date.valueOf(endDate));
        goal.setTotalBudget(request.getTotalBudget());
        goal.setStatus(STATUS_DRAFT);
        goal.setCreatedNm(LOGIN_USER_NAME);

        this.mapper.insertGoal(goal);

        return goal.getGoalId();
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 은(는) 필수입니다. (yyyy-MM-dd)");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    fieldName + " 형식이 잘못되었습니다: " + value + " (yyyy-MM-dd)");
        }
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate 가 startDate 보다 빠릅니다.");
        }
    }

    // DRAFT 상태라면 추가적인 목표 등록을 차단.
    private void validateDraftLimit() {
        int draftCount = this.mapper.countGoalByStatus(LOGIN_USER_ID, STATUS_DRAFT);
        if (draftCount > 0) {
            throw new IllegalStateException("작성 중인 여행 목표가 이미 있습니다.");
        }
    }

    private void validateDestination(String destination) {
        Optional.ofNullable(this.mapper.getCityCostByCity(destination))
                .orElseThrow(() -> new NoSuchElementException(
                        "city_cost 에 등록되지 않은 도시입니다: " + destination));
    }

}
