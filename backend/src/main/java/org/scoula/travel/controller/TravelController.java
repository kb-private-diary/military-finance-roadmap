package org.scoula.travel.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.response.ApiResponse;
import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.dto.TravelCostCreateRequestDTO;
import org.scoula.travel.dto.TravelCostResponseDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;
import org.scoula.travel.service.TravelService;

// 여행 로드맵 REST 컨트롤러
@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/travel")
@Api(tags = "여행 로드맵")
public class TravelController {

    private final TravelService service;

    @GetMapping("/cities")
    @ApiOperation(value = "도시 물가 목록 조회",
            notes = "나라별 스타일(saving/common/premium)별 1인 1일 물가를 조회한다.")
    public ResponseEntity<ApiResponse<List<CityCostResponseDTO>>> findCityCosts(
            @ApiParam(value = "나라명. 생략 시 전체")
            @RequestParam(required = false) String country) {
        return ResponseEntity.ok(ApiResponse.success(this.service.findCityCosts(country)));
    }

    @PostMapping("/goals")
    @ApiOperation(value = "여행 목표 등록",
            notes = "여행 조건(기간·스타일·예산)만 저장하고 생성된 goalId 를 반환한다. "
                    + "places 는 step3, benefits 는 step4 에서 갱신한다.")
    public ResponseEntity<ApiResponse<Long>> createGoal(
            @RequestBody TravelGoalCreateRequestDTO request) {
        Long goalId = this.service.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(goalId));
    }

    @PostMapping("/goals/{goalId}/costs")
    @ApiOperation(value = "예상 경비 산출",
            notes = "저장된 목표 기준으로 계산해 travel_cost 에 적재하고 costId 를 반환한다. "
                    + "재호출 시 기존 결과를 소프트 삭제하고 새로 적재한다.")
    public ResponseEntity<ApiResponse<Long>> createCost(
            @PathVariable Long goalId,
            @RequestBody(required = false) TravelCostCreateRequestDTO request) {
        Long costId = this.service.createCost(goalId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(costId));
    }

    @GetMapping("/goals/{goalId}/costs")
    @ApiOperation(value = "산출된 예상 경비 조회",
            notes = "항목별 경비(교통·숙박·현지 물가)와 총액·잔여 예산을 반환한다.")
    public ResponseEntity<ApiResponse<TravelCostResponseDTO>> findCost(
            @PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(this.service.findCost(goalId)));
    }

}
