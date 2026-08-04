package org.scoula.dashboard.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.response.ApiResponse;
import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationCreateRequestDTO;
import org.scoula.dashboard.dto.DashboardVacationDetailResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationListResponseDTO;
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

    // DASH-API-04: 휴가 목록(요약 + 카드 목록) 조회
    @GetMapping("/vacations")
    public ResponseEntity<ApiResponse<DashboardVacationListResponseDTO>> findVacations(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        Long userId = customUser.getMember().getId();
        log.info("Fetching vacation list for userId: {}", userId);
        DashboardVacationListResponseDTO dto = this.service.findVacations(userId);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // DASH-API-05: 휴가 상세 조회
    @GetMapping("/vacations/{vacationId}")
    public ResponseEntity<ApiResponse<DashboardVacationDetailResponseDTO>> findVacationDetail(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long vacationId
    ) {
        Long userId = customUser.getMember().getId();
        log.info("Fetching vacation detail for userId: {}, vacationId: {}", userId, vacationId);
        DashboardVacationDetailResponseDTO dto =
                this.service.findVacationDetail(userId, vacationId);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // DASH-API-06: 휴가 등록
    @PostMapping("/vacations")
    public ResponseEntity<ApiResponse<Long>> createVacation(
            @AuthenticationPrincipal CustomUser customUser,
            @Valid @RequestBody DashboardVacationCreateRequestDTO request
    ) {
        Long userId = customUser.getMember().getId();
        log.info("Creating vacation for userId: {}", userId);
        Long vacationId = this.service.createVacation(userId, customUser.getUsername(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(vacationId));
    }
}
