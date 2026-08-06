package org.scoula.rent.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.response.ApiResponse;
import org.scoula.rent.dto.HousingProductRecommendResponseDTO;
import org.scoula.rent.service.HousingProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 자취 로드맵 주거 금융상품 추천 컨트롤러 (월세 전용)
 * 매물 조건(지역·보증금·월세·면적)과 사용자 복무기간으로 지원금·대출·이자지원을 그룹핑해 추천한다
 */
@RestController
@RequestMapping("/api/rent")
@RequiredArgsConstructor
public class HousingProductController {

    private final HousingProductService service;

    // GET /api/rent/listings/{listingId}/products?userId=1&months=12
    //   → 매물 조건 기반 금융상품 추천 (중복수급 그룹핑 응답)
    @GetMapping("/listings/{listingId}/products")
    public ResponseEntity<ApiResponse<HousingProductRecommendResponseDTO>> recommendProducts(
            @PathVariable Long listingId,
            @RequestParam Long userId,   // TODO: JWT 연동 후 SecurityContext 로 교체
            @RequestParam int months) {
        return ResponseEntity.ok(ApiResponse.success(service.recommend(listingId, userId, months)));
    }
}
