package org.scoula.product.mapper;

import org.scoula.product.domain.PolicyProductVO;
import org.scoula.product.domain.SavingProductVO;

import java.util.List;

public interface ProductMapper {
    // productType(DEPOSIT/SAVING) 기준 판매중인 예적금 상품 목록 조회
    List<SavingProductVO> findSavingProductListByType(String productType);

    // 판매중인 정책 상품 목록 조회
    List<PolicyProductVO> findPolicyProductList();
}
