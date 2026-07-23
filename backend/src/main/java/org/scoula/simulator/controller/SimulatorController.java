package org.scoula.simulator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.scoula.common.response.ApiResponse;
import org.scoula.simulator.dto.SimulatorCalculateResponseDTO;
import org.scoula.simulator.dto.SimulatorConstantCalcRequestDTO;
import org.scoula.simulator.dto.SimulatorVariableCalcRequestDTO;
import org.scoula.simulator.dto.SimulatorSavingDetailsResponseDTO;
import org.scoula.simulator.service.SimulatorService;

@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
@Log4j2
public class SimulatorController {
    
    private final SimulatorService service;

    // SIM-API-01: 군적금 예상 만기 수령액 상세 조회
    @GetMapping("/saving-details")
    public ResponseEntity<ApiResponse<SimulatorSavingDetailsResponseDTO>> findSavingDetails(
            // TODO: 추후 Spring Security 도입 시 SecurityContext에서 유저 ID 추출로 변경
            @RequestParam Long userId
    ) {
        log.info("Fetching simulator saving details for userId: {}", userId);
        
        SimulatorSavingDetailsResponseDTO dto = this.service.findSavingDetails(userId);
        
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("시뮬레이션을 위한 군적금 가입 내역을 찾을 수 없습니다.", "SIMULATOR_002"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
    
    // SIM-API-02: 만기수령액 1회성 시뮬레이션 (동일 금액)
    @PostMapping("/calculate/constant")
    public ResponseEntity<ApiResponse<SimulatorCalculateResponseDTO>> calculateConstant(
            @RequestBody SimulatorConstantCalcRequestDTO request
    ) {
        log.info("Calculating hypothetical military savings for CONSTANT");
        
        SimulatorCalculateResponseDTO dto = this.service.calculateConstant(request);
        
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("잘못된 입력값입니다.", "SIMULATOR_003"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
    
    // SIM-API-02: 만기수령액 1회성 시뮬레이션 (구간별 금액)
    @PostMapping("/calculate/variable")
    public ResponseEntity<ApiResponse<SimulatorCalculateResponseDTO>> calculateVariable(
            @RequestBody SimulatorVariableCalcRequestDTO request
    ) {
        log.info("Calculating hypothetical military savings for VARIABLE");
        
        SimulatorCalculateResponseDTO dto = this.service.calculateVariable(request);
        
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("잘못된 입력값입니다.", "SIMULATOR_003"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
