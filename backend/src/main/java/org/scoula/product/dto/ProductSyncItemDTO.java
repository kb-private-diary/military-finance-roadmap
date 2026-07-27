package org.scoula.product.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.product.domain.SavingProductVO;

// FSS 동기화로 갱신된 상품 1건(상품명 + 가입기간 + 금리) 요약
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSyncItemDTO {
    private String productName;
    private Integer saveTrm;
    private BigDecimal basicRate;
    private BigDecimal maxRate;

    public static ProductSyncItemDTO of(SavingProductVO vo) {
        return ProductSyncItemDTO.builder()
                .productName(vo.getProductName())
                .saveTrm(vo.getSaveTrm())
                .basicRate(vo.getBasicRate())
                .maxRate(vo.getMaxRate())
                .build();
    }
}
