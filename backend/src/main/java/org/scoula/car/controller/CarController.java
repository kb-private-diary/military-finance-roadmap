package org.scoula.car.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.scoula.car.dto.CarAcquisitionTaxResponseDTO;
import org.scoula.car.dto.CarEvSubsidyResponseDTO;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.car.dto.CarGoalResponseDTO;
import org.scoula.car.dto.CarMaintenanceCostResponseDTO;
import org.scoula.car.dto.CarModelSelectRequestDTO;
import org.scoula.car.dto.CarRecommendationResponseDTO;
import org.scoula.car.dto.CarUsedPriceResponseDTO;
import org.scoula.car.service.CarService;
import org.scoula.common.response.ApiResponse;

@RestController
@RequestMapping("/api/car")
@RequiredArgsConstructor
@Log4j2
public class CarController {

    private final CarService carService;

    // CAR-API-01: 자동차 목표 신규 등록
    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<CarGoalCreateResponseDTO>> createCarGoal(
            @RequestBody CarGoalCreateRequestDTO requestDTO) {
        log.info("Creating car goal for userId: {}", requestDTO.getUserId());
        CarGoalCreateResponseDTO responseDTO = this.carService.createCarGoal(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDTO));
    }

    // CAR-API-03: 자동차 목표 목록 조회
    @GetMapping("/goals")
    public ResponseEntity<ApiResponse<List<CarGoalResponseDTO>>> getCarGoals(
            @RequestParam Long userId) {
        log.info("Fetching car goals for userId: {}", userId);
        List<CarGoalResponseDTO> responseDTO = this.carService.findCarGoals(userId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-04: 자동차 목표 상세 조회
    @GetMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<CarGoalResponseDTO>> getCarGoalDetail(
            @PathVariable Long goalId) {
        log.info("Fetching car goal detail for goalId: {}", goalId);
        CarGoalResponseDTO responseDTO = this.carService.findCarGoalDetail(goalId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-07: 예산/차종 기반 차량 추천 목록 조회
    @GetMapping("/goals/{goalId}/recommendations")
    public ResponseEntity<ApiResponse<List<CarRecommendationResponseDTO>>> getRecommendations(
            @PathVariable Long goalId) {
        log.info("Fetching car recommendations for goalId: {}", goalId);
        List<CarRecommendationResponseDTO> responseDTO = this.carService.recommendCars(goalId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-08: 추천 목록 중 차량 모델 선택 반영
    @PatchMapping("/goals/{goalId}/select-model")
    public ResponseEntity<ApiResponse<CarGoalResponseDTO>> selectCarModel(
            @PathVariable Long goalId, @RequestBody CarModelSelectRequestDTO requestDTO) {
        log.info("Selecting car model for goalId: {}", goalId);
        CarGoalResponseDTO responseDTO = this.carService.selectCarModel(goalId, requestDTO);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-09: 오피넷 유가 연동 기반 연간 유지비(연료비+보험료) 조회
    @GetMapping("/goals/{goalId}/maintenance-cost")
    public ResponseEntity<ApiResponse<CarMaintenanceCostResponseDTO>> getMaintenanceCost(
            @PathVariable Long goalId) {
        log.info("Calculating maintenance cost for goalId: {}", goalId);
        CarMaintenanceCostResponseDTO responseDTO = this.carService.calculateMaintenanceCost(goalId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-10: 취득세 및 공채매입 여부 조회
    @GetMapping("/goals/{goalId}/acquisition-tax")
    public ResponseEntity<ApiResponse<CarAcquisitionTaxResponseDTO>> getAcquisitionTax(
            @PathVariable Long goalId) {
        log.info("Calculating acquisition tax for goalId: {}", goalId);
        CarAcquisitionTaxResponseDTO responseDTO = this.carService.calculateAcquisitionTax(goalId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-13: 중고차 시세 추정
    @GetMapping("/goals/{goalId}/used-price")
    public ResponseEntity<ApiResponse<CarUsedPriceResponseDTO>> getUsedPrice(
            @PathVariable Long goalId) {
        log.info("Calculating used price for goalId: {}", goalId);
        CarUsedPriceResponseDTO responseDTO = this.carService.calculateUsedPrice(goalId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-12: 지역 기준 전기차 보조금 계산
    @GetMapping("/goals/{goalId}/ev-subsidy")
    public ResponseEntity<ApiResponse<CarEvSubsidyResponseDTO>> getEvSubsidy(
            @PathVariable Long goalId) {
        log.info("Calculating EV subsidy for goalId: {}", goalId);
        CarEvSubsidyResponseDTO responseDTO = this.carService.calculateEvSubsidy(goalId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }
}
