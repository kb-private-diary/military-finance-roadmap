package org.scoula.regret.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.response.ApiResponse;
import org.scoula.regret.dto.SpendingResponseDTO;
import org.scoula.regret.dto.SpendingReviewRequestDTO;
import org.scoula.regret.dto.RegretStatsResponseDTO;
import org.scoula.regret.dto.RegretSpendingSummaryDTO;
import org.scoula.regret.service.RegretService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/regret")
@RequiredArgsConstructor
public class RegretController {

    private final RegretService service;

    // GET /api/regret/spendings?userId=1 → 지출 목록 (회고 태깅 상태 포함)
    @GetMapping("/spendings")
    public ResponseEntity<ApiResponse<List<SpendingResponseDTO>>> findSpendings(
            @RequestParam Long userId) { // TODO: JWT 연동 후 SecurityContext 로 교체
        return ResponseEntity.ok(ApiResponse.success(service.findSpendings(userId)));
    }

    // POST /api/regret/reviews → 지출 만족/후회 태깅
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<Void>> tagReview(
            @Valid @RequestBody SpendingReviewRequestDTO request,
            @RequestParam Long userId) { // TODO: JWT 연동 후 SecurityContext 로 교체
        service.tagReview(request, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // GET /api/regret/stats?userId=1&yearMonth=202608 → 월별 후회소비 통계 (yearMonth=yyyyMM, dealYm과 동일 형식)
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<RegretStatsResponseDTO>> getMonthlyStats(
            @RequestParam Long userId,
            @RequestParam String yearMonth) { // TODO: JWT 연동 후 SecurityContext 로 교체
        return ResponseEntity.ok(ApiResponse.success(service.getMonthlyStats(userId, yearMonth)));
    }

    // GET /api/regret/spending/summary?userId=1&months=3 → 최근 N개월 월평균 지출·후회 (자취 Step5 연동)
    @GetMapping("/spending/summary")
    public ResponseEntity<ApiResponse<RegretSpendingSummaryDTO>> getSpendingSummary(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "3") int months) { // TODO: JWT 연동 후 SecurityContext 로 교체
        return ResponseEntity.ok(ApiResponse.success(service.getSpendingSummary(userId, months)));
    }
}
