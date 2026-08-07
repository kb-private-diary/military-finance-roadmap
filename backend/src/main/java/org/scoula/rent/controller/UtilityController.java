package org.scoula.rent.controller;

import java.util.List;

import org.scoula.common.response.ApiResponse;
import org.scoula.rent.domain.SnapshotVO;
import org.scoula.rent.dto.UtilityConfigResponseDTO;
import org.scoula.rent.dto.UtilityEstimateResponseDTO;
import org.scoula.rent.service.UtilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 공과금·관리비 컨트롤러 (Step5 정밀 시뮬레이션)
 * 계수·요금표·앵커를 한 번에 응답해 프론트가 캐싱(Pinia)하고 슬라이더 조작 시 즉시 재계산하도록 함
 */
@RestController
@RequestMapping("/api/rent")
@RequiredArgsConstructor
public class UtilityController {

    private final UtilityService utilityService;

    // GET /api/rent/utility/config?regionCode=1168010100
    //   → Step5 진입 시 계수·요금표·앵커 일괄 응답 (프론트 캐싱용)
    @GetMapping("/utility/config")
    public ResponseEntity<ApiResponse<UtilityConfigResponseDTO>> getConfig(
            @RequestParam String regionCode) {
        return ResponseEntity.ok(ApiResponse.success(utilityService.getConfig(regionCode)));
    }

    // GET /api/rent/utility/estimate?regionCode=1168010100&areaSqm=23&startYear=2026&startMonth=1&months=12
    //   → 거주 N개월 월별 공과금·관리비 (입주 월부터 순차 누적)
    @GetMapping("/utility/estimate")
    public ResponseEntity<ApiResponse<UtilityEstimateResponseDTO>> estimate(
            @RequestParam String regionCode,
            @RequestParam double areaSqm,
            @RequestParam int startYear,
            @RequestParam int startMonth,
            @RequestParam int months) {
        return ResponseEntity.ok(ApiResponse.success(
                utilityService.estimate(regionCode, areaSqm, startYear, startMonth, months)));
    }

    // GET /api/rent/goals/{roadmapId}/utility → 로드맵 저장된 월별 스냅샷 조회 (Step5)
    @GetMapping("/goals/{roadmapId}/utility")
    public ResponseEntity<ApiResponse<List<SnapshotVO>>> findSnapshot(
            @PathVariable Long roadmapId) {
        return ResponseEntity.ok(ApiResponse.success(utilityService.findSnapshot(roadmapId)));
    }
}
