package org.scoula.dashboard.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.response.ApiResponse;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Log4j2
public class DashboardController {

    private final DashboardService service;


    // DASH-API-03: 적금 현황 조회(현재 납입액 및 예상 만기 수령액 반환)
    @GetMapping("/savings")
    public ResponseEntity<ApiResponse<DashboardSavingsResponseDTO>> findSavingsStatus(
            // TODO: 실제 구현에서는 JWT 기반 보안(SecurityContext)에서 userId를 가져와야 합니다.
            // 개발 편의를 위해 임시로 param에서 받도록 설정합니다.
            @RequestParam Long userId
    ) {
        log.info("Fetching savings status for userId: {}", userId);
        DashboardSavingsResponseDTO dto = service.findSavingsStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
