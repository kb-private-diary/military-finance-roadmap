package org.scoula.car.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
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
}
