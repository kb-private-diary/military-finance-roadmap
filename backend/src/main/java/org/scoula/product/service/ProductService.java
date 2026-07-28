package org.scoula.product.service;

import org.scoula.product.dto.PolicyProductListResponseDTO;
import org.scoula.product.dto.SavingProductListResponseDTO;

import java.util.List;

public interface ProductService {
    // category: deposits(예금) | savings(적금)
    List<SavingProductListResponseDTO> findSavingProductListByCategory(String category);

    // 판매중인 정책 상품 목록 조회
    List<PolicyProductListResponseDTO> findPolicyProductList();
}
