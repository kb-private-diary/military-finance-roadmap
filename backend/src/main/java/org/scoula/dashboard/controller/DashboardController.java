package org.scoula.dashboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.response.ApiResponse;
import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.service.DashboardService;
import org.scoula.security.account.domain.CustomUser;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Log4j2
public class DashboardController {

    private final DashboardService service;

    // DASH-API-01: 복무 기본 정보 조회(이름, 계급, 현재/총복무일, 복무달성률(%) 반환)

    @GetMapping("/basic")
    public ResponseEntity<ApiResponse<DashboardBasicResponseDTO>> findBasicInfo(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        Long userId = customUser.getMember().getId();
        log.info("Fetching basic info for userId: {}", userId);
        DashboardBasicResponseDTO dto = this.service.findBasicInfo(userId);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // DASH-API-03: 적금 현황 조회(현재 납입액 및 예상 만기 수령액 반환)
    @GetMapping("/savings")
    public ResponseEntity<ApiResponse<DashboardSavingsResponseDTO>> findSavingsStatus(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        Long userId = customUser.getMember().getId();
        log.info("Fetching savings status for userId: {}", userId);
        DashboardSavingsResponseDTO dto = this.service.findSavingsStatus(userId);
        
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
