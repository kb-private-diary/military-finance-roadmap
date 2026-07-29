package org.scoula.rent.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.response.ApiResponse;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.service.RentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/rent")
@RequiredArgsConstructor
public class RentController {

    private final RentService service;

    // GET /api/rent/regions            → 시도 목록
    // GET /api/rent/regions?sido=서울   → 시군구 목록
    // GET /api/rent/regions?sido=서울&sigunguCode=11680 → 읍면동 목록
    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<RegionResponseDTO>>> findRegions(
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String sigunguCode) {

        List<RegionResponseDTO> result = service.findRegions(sido, sigunguCode);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // POST /api/rent/goals → 자취 목표 등록 (생성된 goalId 반환)
    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<Long>> createGoal(
            @Valid @RequestBody RentGoalCreateRequestDTO request,
            @RequestParam Long userId) { // TODO: JWT 연동 후 SecurityContext 로 교체

        Long goalId = service.createGoal(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(goalId));
    }
}
