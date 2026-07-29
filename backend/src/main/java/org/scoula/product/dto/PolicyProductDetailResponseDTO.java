package org.scoula.product.dto;

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
    private String benefits;
    private String joinMember;
    private Long minLimit;
    private Long maxLimit;
    private String saveTrmNote;
    private String policyLink;
    private Boolean hasCalculator;

    public static PolicyProductDetailResponseDTO of(PolicyProductVO vo) {
        return PolicyProductDetailResponseDTO.builder()
                .policyId(vo.getPolicyId())
                .policyName(vo.getPolicyName())
                .benefits(vo.getBenefits())
                .joinMember(vo.getJoinMember())
                .minLimit(vo.getMinLimit())
                .maxLimit(vo.getMaxLimit())
                .saveTrmNote(vo.getSaveTrmNote())
                .policyLink(vo.getPolicyLink())
                .hasCalculator(vo.getHasCalculator())
                .build();
    }
}
