package org.scoula.product.service;

import lombok.RequiredArgsConstructor;
import org.scoula.common.exception.BusinessException;
import org.scoula.product.dto.PolicyProductListResponseDTO;
import org.scoula.product.dto.SavingProductListResponseDTO;
import org.scoula.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String CATEGORY_DEPOSITS = "deposits";
    private static final String CATEGORY_SAVINGS = "savings";
    private static final String PRODUCT_TYPE_DEPOSIT = "DEPOSIT";
    private static final String PRODUCT_TYPE_SAVING = "SAVING";

    private final ProductMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SavingProductListResponseDTO> findSavingProductListByCategory(String category) {
        String productType = toProductType(category);
        return mapper.findSavingProductListByType(productType).stream()
                .map(SavingProductListResponseDTO::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyProductListResponseDTO> findPolicyProductList() {
        return mapper.findPolicyProductList().stream()
                .map(PolicyProductListResponseDTO::of)
                .toList();
    }

    private String toProductType(String category) {
        if (CATEGORY_DEPOSITS.equals(category)) {
            return PRODUCT_TYPE_DEPOSIT;
        }
        if (CATEGORY_SAVINGS.equals(category)) {
            return PRODUCT_TYPE_SAVING;
        }
        throw BusinessException.badRequest("유효하지 않은 상품 카테고리입니다.", "PRODU_001");
    }
}
