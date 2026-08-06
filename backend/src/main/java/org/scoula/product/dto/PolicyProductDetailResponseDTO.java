package org.scoula.product.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.product.domain.PolicyProductVO;

// 정책 상품 상세 조회 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyProductDetailResponseDTO {

    private Long policyId;
    private String policyName;
    private Long minLimit;
    private Long maxLimit;
    private String saveTrmNote;
    private String policyLink;
    private Boolean hasCalculator;
    private Integer calcPeriodMonths;
    private BigDecimal minRate;
    private BigDecimal maxRate;
    private BigDecimal normalMatchRate;
    private BigDecimal preferMatchRate;

    public static PolicyProductDetailResponseDTO of(PolicyProductVO vo) {
        return PolicyProductDetailResponseDTO.builder()
                .policyId(vo.getPolicyId())
                .policyName(vo.getPolicyName())
                .minLimit(vo.getMinLimit())
                .maxLimit(vo.getMaxLimit())
                .saveTrmNote(vo.getSaveTrmNote())
                .policyLink(vo.getPolicyLink())
                .hasCalculator(vo.getHasCalculator())
                .calcPeriodMonths(vo.getCalcPeriodMonths())
                .minRate(vo.getMinRate())
                .maxRate(vo.getMaxRate())
                .normalMatchRate(vo.getNormalMatchRate())
                .preferMatchRate(vo.getPreferMatchRate())
                .build();
    }
}
