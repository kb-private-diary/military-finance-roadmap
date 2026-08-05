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
import org.scoula.rent.service.RentService;
import org.scoula.rent.service.RentListingLoadService;
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
            @RequestParam Long userId) { // TODO: JWT 연동 후 SecurityContext 로 교체

        Long goalId = service.createGoal(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(goalId));
    }

    // GET /api/rent/schools?keyword=부산 → 학교 검색 (자동완성)
    @GetMapping("/schools")
    public ResponseEntity<ApiResponse<List<SchoolSearchResponseDTO>>> findSchools(
            @RequestParam String keyword) {

        return ResponseEntity.ok(ApiResponse.success(service.findSchools(keyword)));
    }

    // GET /api/rent/goals/{goalId} → 목표 상세 (목표 정보)
    @GetMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<RentGoalDetailResponseDTO>> findGoal(@PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(service.findGoal(goalId)));
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

    // POST /api/rent/listings/load?dealYm=202605 → 국토부 실거래가 매물 적재 (개발용, 대상 시군구 3개)
    @PostMapping("/listings/load")
    public ResponseEntity<ApiResponse<Integer>> loadListings(@RequestParam String dealYm) {
        int count = loadService.load(List.of("26410", "26230", "26440"), dealYm);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
