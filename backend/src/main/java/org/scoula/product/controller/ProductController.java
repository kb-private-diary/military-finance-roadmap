package org.scoula.product.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.response.ApiResponse;
import org.scoula.product.dto.PolicyProductListResponseDTO;
import org.scoula.product.dto.SavingProductListResponseDTO;
import org.scoula.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    // GET /api/products/policies → 정책 상품 목록 조회
    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<List<PolicyProductListResponseDTO>>> findPolicyProductList() {
        List<PolicyProductListResponseDTO> result = service.findPolicyProductList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
