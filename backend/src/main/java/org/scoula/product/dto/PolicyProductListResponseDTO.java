package org.scoula.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.product.domain.PolicyProductVO;

import java.math.BigDecimal;

// 정책 상품 목록 조회 응답 DTO (카드 노출용 요약 정보만 포함, 가입대상·상세혜택은 상세조회에서 제공)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyProductListResponseDTO {

    private Long policyId;
    private String policyName;
    private String saveTrmNote;
    private Long minLimit;
    private Long maxLimit;
    private BigDecimal maxRate;
    private BigDecimal normalMatchRate;
    private BigDecimal preferMatchRate;
    private Boolean hasCalculator;

    public static PolicyProductListResponseDTO of(PolicyProductVO vo) {
        return PolicyProductListResponseDTO.builder()
                .policyId(vo.getPolicyId())
                .policyName(vo.getPolicyName())
                .saveTrmNote(vo.getSaveTrmNote())
                .minLimit(vo.getMinLimit())
                .maxLimit(vo.getMaxLimit())
                .maxRate(vo.getMaxRate())
                .normalMatchRate(vo.getNormalMatchRate())
                .preferMatchRate(vo.getPreferMatchRate())
                .hasCalculator(vo.getHasCalculator())
                .build();
    }
}
