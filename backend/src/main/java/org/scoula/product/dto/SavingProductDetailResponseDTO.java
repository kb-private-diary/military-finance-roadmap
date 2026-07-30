package org.scoula.product.dto;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.product.domain.SavingProductVO;

// 예적금 상품 상세 조회 응답 DTO
// 상품(fin_prdt_cd)은 save_trm(가입기간)마다 여러 행으로 저장돼 있어, 기간과 무관하게
// 금리·저축기간은 전체 행 중 최소~최대로 집계하고, 그 외 값은 아무 행이나 대표로 사용한다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingProductDetailResponseDTO {

    private String productId;
    private String productType; // DEPOSIT(예금) | SAVING(적금)
    private String productName;
    private String joinMember;
    private BigDecimal minRate;
    private BigDecimal maxRate;
    private Integer minSaveTrm;
    private Integer maxSaveTrm;
    private Long minLimit;
    private Long maxLimit;
    private String productLink;
    private Boolean isTaxExempt;
    private List<SaveTrmRateDTO> saveTrmRates;

    public static SavingProductDetailResponseDTO of(List<SavingProductVO> rows) {
        SavingProductVO representative = rows.get(0);

        List<SaveTrmRateDTO> saveTrmRates = rows.stream()
                .map(SaveTrmRateDTO::of)
                .toList();

        BigDecimal minRate = rows.stream()
                .map(SavingProductVO::getBasicRate)
                .min(Comparator.naturalOrder())
                .orElse(null);
        BigDecimal maxRate = rows.stream()
                .map(SavingProductVO::getMaxRate)
                .max(Comparator.naturalOrder())
                .orElse(null);
        Integer minSaveTrm = rows.stream()
                .map(SavingProductVO::getSaveTrm)
                .min(Comparator.naturalOrder())
                .orElse(null);
        Integer maxSaveTrm = rows.stream()
                .map(SavingProductVO::getSaveTrm)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return SavingProductDetailResponseDTO.builder()
                .productId(representative.getFinPrdtCd())
                .productType(representative.getProductType())
                .productName(representative.getProductName())
                .joinMember(representative.getJoinMember())
                .minRate(minRate)
                .maxRate(maxRate)
                .minSaveTrm(minSaveTrm)
                .maxSaveTrm(maxSaveTrm)
                .minLimit(representative.getMinLimit())
                .maxLimit(representative.getMaxLimit())
                .productLink(representative.getProductLink())
                .isTaxExempt(representative.getIsTaxExempt())
                .saveTrmRates(saveTrmRates)
                .build();
    }
}
