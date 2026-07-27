package org.scoula.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.product.domain.SavingProductVO;

import java.math.BigDecimal;

// 예적금 상품 목록 조회 응답 DTO
// 상품(fin_prdt_cd) 단위로 그룹핑해 1건만 노출하며, saveTrm/maxRate는 그 상품의
// 기간별 행 중 max_rate가 가장 높은 "같은 행"에서 함께 가져온 값이다 (기간·금리 짝 유지).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingProductListResponseDTO {

    private String productId;
    private String korCoNm;
    private String productName;
    private Integer saveTrm;
    private BigDecimal basicRate;
    private BigDecimal maxRate;
    private Long minLimit;
    private Long maxLimit;
    private Boolean isTaxExempt;
    private BigDecimal govMatchRate;

    public static SavingProductListResponseDTO of(SavingProductVO vo) {
        return SavingProductListResponseDTO.builder()
                .productId(vo.getFinPrdtCd())
                .korCoNm(vo.getKorCoNm())
                .productName(vo.getProductName())
                .saveTrm(vo.getSaveTrm())
                .basicRate(vo.getBasicRate())
                .maxRate(vo.getMaxRate())
                .minLimit(vo.getMinLimit())
                .maxLimit(vo.getMaxLimit())
                .isTaxExempt(vo.getIsTaxExempt())
                .govMatchRate(vo.getGovMatchRate())
                .build();
    }
}
