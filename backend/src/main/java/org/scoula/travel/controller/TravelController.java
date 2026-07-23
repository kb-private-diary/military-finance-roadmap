package org.scoula.travel.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
