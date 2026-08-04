package org.scoula.car.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.scoula.car.dto.CarAcquisitionTaxResponseDTO;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.car.dto.CarMaintenanceCostResponseDTO;
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
}
