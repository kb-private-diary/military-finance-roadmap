package org.scoula.product.service;

import java.util.List;

import org.scoula.product.dto.PolicyProductDetailResponseDTO;
import org.scoula.product.dto.PolicyProductListResponseDTO;
import org.scoula.product.dto.ProductSyncResponseDTO;
import org.scoula.product.dto.SavingProductDetailResponseDTO;
import org.scoula.product.dto.SavingProductListResponseDTO;

public interface ProductService {
    // category: deposits(예금) | savings(적금)
    List<SavingProductListResponseDTO> findSavingProductListByCategory(String category);

    // fin_prdt_cd 기준 예적금 상품 상세 조회
    SavingProductDetailResponseDTO findSavingProductDetail(String productId);

    // 판매중인 정책 상품 목록 조회
    List<PolicyProductListResponseDTO> findPolicyProductList();

    // policy_id 기준 정책 상품 상세 조회
    PolicyProductDetailResponseDTO findPolicyProductDetail(Long policyId);

    // 금융감독원 "금융상품 한눈에" API에서 KB국민은행 예·적금 상품을 가져와 DB에 UPSERT
    ProductSyncResponseDTO syncSavingProducts();
}
