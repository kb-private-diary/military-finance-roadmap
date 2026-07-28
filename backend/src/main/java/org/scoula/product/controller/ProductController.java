package org.scoula.product.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.scoula.common.response.ApiResponse;
import org.scoula.product.dto.PolicyProductListResponseDTO;
import org.scoula.product.dto.ProductSyncResponseDTO;
import org.scoula.product.dto.SavingProductDetailResponseDTO;
import org.scoula.product.dto.SavingProductListResponseDTO;
import org.scoula.product.service.ProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // GET /api/products/savings/{category} → category: deposits(예금) | savings(적금)
    @GetMapping("/savings/{category}")
    public ResponseEntity<ApiResponse<List<SavingProductListResponseDTO>>> findSavingProductList(
            @PathVariable String category) {

        List<SavingProductListResponseDTO> result = service.findSavingProductListByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // GET /api/products/saving-details/{productId} → 예적금 상품 상세 조회 (productId = fin_prdt_cd)
    @GetMapping("/saving-details/{productId}")
    public ResponseEntity<ApiResponse<SavingProductDetailResponseDTO>> findSavingProductDetail(
            @PathVariable String productId) {

        SavingProductDetailResponseDTO result = service.findSavingProductDetail(productId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // GET /api/products/policies → 정책 상품 목록 조회
    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<List<PolicyProductListResponseDTO>>> findPolicyProductList() {
        List<PolicyProductListResponseDTO> result = service.findPolicyProductList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // POST /api/products/sync → FSS API에서 KB 예·적금 상품을 가져와 DB에 반영 (운영/디버깅용 수동 트리거, 스케줄러와 동일 로직)
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<ProductSyncResponseDTO>> syncSavingProducts() {
        ProductSyncResponseDTO result = service.syncSavingProducts();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
