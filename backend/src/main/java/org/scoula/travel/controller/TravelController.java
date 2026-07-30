package org.scoula.travel.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
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
import org.scoula.travel.dto.TravelCostResponseDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;
import org.scoula.travel.dto.TravelGoalDraftResponseDTO;
import org.scoula.travel.dto.TravelPlaceResponseDTO;
import org.scoula.travel.dto.TravelPlaceSelectionDTO;
import org.scoula.travel.dto.TravelPlacesUpdateRequestDTO;
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

    @GetMapping("/goals/current")
    @ApiOperation(
            value = "현재 작성 중인 여행 목표 조회",
            notes = "DRAFT 상태의 여행 목표가 있으면 이어쓰기 정보를 반환한다.")
    public ResponseEntity<ApiResponse<TravelGoalDraftResponseDTO>>
            findCurrentDraft() {
        return ResponseEntity.ok(
                ApiResponse.success(this.service.findCurrentDraft()));
    }

    @PatchMapping("/goals/{goalId}")
    @ApiOperation(
            value = "작성 중인 여행 목표 수정",
            notes = "Step 1에서 변경한 DRAFT 여행 목표 정보를 갱신한다.")
    public ResponseEntity<ApiResponse<Void>> updateGoal(
            @PathVariable final Long goalId,
            @RequestBody final TravelGoalCreateRequestDTO request) {
        this.service.updateGoal(goalId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/goals/{goalId}/costs")
    @ApiOperation(value = "예상 경비 산출",
            notes = "저장된 목표 기준으로 계산해 travel_cost 에 적재하고 costId 를 반환한다. "
                    + "재호출 시 기존 결과를 갱신한다.")
    public ResponseEntity<ApiResponse<Long>> createCost(
            @PathVariable final Long goalId) {
        final Long costId = this.service.createCost(goalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(costId));
    }

    @GetMapping("/goals/{goalId}/costs")
    @ApiOperation(value = "산출된 예상 경비 조회",
            notes = "항목별 경비(교통·숙박·현지 물가)와 총액·잔여 예산을 반환한다.")
    public ResponseEntity<ApiResponse<TravelCostResponseDTO>> findCost(
            @PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(this.service.findCost(goalId)));
    }

    @GetMapping("/goals/{goalId}/places")
    @ApiOperation(
            value = "관광지·맛집 추천 조회",
            notes = "저장된 목표의 도착지를 기준으로 관광지 또는 맛집을 검색한다.")
    public ResponseEntity<ApiResponse<List<TravelPlaceResponseDTO>>> searchPlaces(
            @PathVariable final Long goalId,
            @ApiParam(value = "검색 유형: attraction 또는 restaurant", required = true)
            @RequestParam final String category) {
        return ResponseEntity.ok(ApiResponse.success(
                this.service.searchPlaces(goalId, category)));
    }

    @PatchMapping("/goals/{goalId}/places")
    @ApiOperation(
            value = "관심 관광지·맛집 저장",
            notes = "사용자가 선택한 관광지와 맛집을 여행 목표의 JSON 목록으로 저장한다.")
    public ResponseEntity<ApiResponse<Void>> updatePlaces(
            @PathVariable final Long goalId,
            @RequestBody final TravelPlacesUpdateRequestDTO request) {
        this.service.updatePlaces(goalId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/goals/{goalId}/places/selected")
    @ApiOperation(
            value = "선택한 관광지·맛집 조회",
            notes = "여행 목표에 JSON으로 저장된 관심 관광지와 맛집을 조회한다.")
    public ResponseEntity<ApiResponse<List<TravelPlaceSelectionDTO>>>
            getSelectedPlaces(@PathVariable final Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(
                this.service.getSelectedPlaces(goalId)));
    }

}
