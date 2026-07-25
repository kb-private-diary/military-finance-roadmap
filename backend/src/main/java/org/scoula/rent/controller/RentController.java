package org.scoula.rent.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.scoula.common.response.ApiResponse;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.RentGoalCreateResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.RentGoalRegionCreateRequestDTO;
import org.scoula.rent.service.RentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rent")
@RequiredArgsConstructor
public class RentController {

    private final RentService service;

    // 지역 3단계 조회
    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<RegionResponseDTO>>> findRegions(
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String sigunguCode) {

        return ResponseEntity.ok(ApiResponse.success(service.findRegions(sido, sigunguCode)));
    }

    // 월세 목표 등록 (DRAFT / 기존 DRAFT 대체) — userId 는 임시 @RequestParam
    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<RentGoalCreateResponseDTO>> createRentGoal(
            @RequestParam Long userId,
            @RequestBody RentGoalCreateRequestDTO request) {

        RentGoalCreateResponseDTO result = service.createRentGoal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    // 진행중 목표 조회 (없으면 data=null)
    @GetMapping("/goals/current")
    public ResponseEntity<ApiResponse<RentGoalDetailResponseDTO>> findCurrentRentGoal(
            @RequestParam Long userId) {

        return ResponseEntity.ok(ApiResponse.success(service.findCurrentRentGoal(userId)));
    }

    // 목표 상세 조회 (본인 것만)
    @GetMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<RentGoalDetailResponseDTO>> findRentGoal(
            @PathVariable Long goalId,
            @RequestParam Long userId) {

        return ResponseEntity.ok(ApiResponse.success(service.findRentGoal(goalId, userId)));
    }

    // 목표 삭제 (soft)
    @DeleteMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<Void>> deleteRentGoal(
            @PathVariable Long goalId,
            @RequestParam Long userId) {

        service.deleteRentGoal(goalId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 희망 지역 등록 (최대 5개, 재등록 시 대체)
    @PostMapping("/goals/{goalId}/regions")
    public ResponseEntity<ApiResponse<Void>> addGoalRegions(
            @PathVariable Long goalId,
            @RequestParam Long userId,
            @RequestBody RentGoalRegionCreateRequestDTO request) {

        service.addGoalRegions(goalId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }
}
