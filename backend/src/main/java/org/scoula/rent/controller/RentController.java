package org.scoula.rent.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.response.ApiResponse;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.SchoolSearchResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.RentListingResponseDTO;
import org.scoula.rent.dto.RentListingDetailResponseDTO;
import org.scoula.rent.dto.RentCostResponseDTO;
import org.scoula.rent.dto.RentAffordabilityResponseDTO;
import org.scoula.rent.service.RentService;
import org.scoula.rent.service.RentListingLoadService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/rent")
@RequiredArgsConstructor
public class RentController {

    private final RentService service;
    private final RentListingLoadService loadService;

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
            @AuthenticationPrincipal CustomUser customUser) {

        Long goalId = service.createGoal(request, customUser.getMember().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(goalId));
    }

    // GET /api/rent/schools?keyword=부산 → 학교 검색 (자동완성)
    @GetMapping("/schools")
    public ResponseEntity<ApiResponse<List<SchoolSearchResponseDTO>>> findSchools(
            @RequestParam String keyword) {

        return ResponseEntity.ok(ApiResponse.success(service.findSchools(keyword)));
    }

    // GET /api/rent/goals/current → 진행중(DRAFT) 목표 조회 (없으면 null)
    // ※ /goals/{goalId} 보다 먼저 선언 - 리터럴 경로가 우선 매칭됨
    @GetMapping("/goals/current")
    public ResponseEntity<ApiResponse<RentGoalDetailResponseDTO>> findCurrentGoal(
            @AuthenticationPrincipal CustomUser customUser) {
        return ResponseEntity.ok(ApiResponse.success(
                service.findCurrentGoal(customUser.getMember().getId())));
    }

    // GET /api/rent/goals/{goalId} → 목표 상세 (목표 정보)
    @GetMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<RentGoalDetailResponseDTO>> findGoal(@PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(service.findGoal(goalId)));
    }

    // DELETE /api/rent/goals/{goalId} → 목표 삭제 (soft delete)
    @DeleteMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long goalId) {
        service.deleteGoal(goalId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // GET /api/rent/goals/{goalId}/listings → 조건 매칭 매물 리스트 (Step2)
    @GetMapping("/goals/{goalId}/listings")
    public ResponseEntity<ApiResponse<List<RentListingResponseDTO>>> findListings(@PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(service.findListings(goalId)));
    }

    // GET /api/rent/listings/{listingId} → 매물 상세 (Step3)
    @GetMapping("/listings/{listingId}")
    public ResponseEntity<ApiResponse<RentListingDetailResponseDTO>> findListingDetail(@PathVariable Long listingId) {
        return ResponseEntity.ok(ApiResponse.success(service.findListingDetail(listingId)));
    }

    // GET /api/rent/listings/{listingId}/cost?months=12 → 총 필요자금 (보증금 + 월세×거주개월)
    @GetMapping("/listings/{listingId}/cost")
    public ResponseEntity<ApiResponse<RentCostResponseDTO>> calculateCost(
            @PathVariable Long listingId,
            @RequestParam int months) {
        return ResponseEntity.ok(ApiResponse.success(service.calculateCost(listingId, months)));
    }

    // GET /api/rent/listings/{listingId}/affordability?months=6 → 부족분·감당도 (Step4 빨간 카드)
    @GetMapping("/listings/{listingId}/affordability")
    public ResponseEntity<ApiResponse<RentAffordabilityResponseDTO>> findAffordability(
            @PathVariable Long listingId,
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam int months) {
        return ResponseEntity.ok(ApiResponse.success(
                service.findAffordability(listingId, customUser.getMember().getId(), months)));
    }

    // POST /api/rent/goals/{goalId}/confirm?months=6 → 로드맵 저장 (DRAFT → CONFIRMED, months 선택)
    @PostMapping("/goals/{goalId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmGoal(
            @PathVariable Long goalId,
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam(required = false) Integer months) {
        service.confirmGoal(goalId, customUser.getMember().getId(), months);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // POST /api/rent/listings/load?dealYm=202605 → 국토부 실거래가 매물 적재 (개발용, 대상 시군구 3개)
    @PostMapping("/listings/load")
    public ResponseEntity<ApiResponse<Integer>> loadListings(@RequestParam String dealYm) {
        int count = loadService.load(List.of("26410", "26230", "26440"), dealYm);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // POST /api/rent/listings/load-nationwide?dealYm=202605 → 전국 매물 적재 (좌표 없이, REGION 검색용)
    @PostMapping("/listings/load-nationwide")
    public ResponseEntity<ApiResponse<Integer>> loadNationwide(@RequestParam String dealYm) {
        return ResponseEntity.ok(ApiResponse.success(loadService.loadNationwide(dealYm)));
    }
}
