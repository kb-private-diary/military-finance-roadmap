package org.scoula.product.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

import java.math.BigDecimal;

// policy_product 테이블 매핑 VO (청년 정책금융 상품 정보)
@Data
@EqualsAndHashCode(callSuper = true)
public class PolicyProductVO extends BaseVO {
    private Long policyId;
    private String policyName;
    private Boolean policyStatus;
    private String benefits;
    private String joinMember;
    private Long minLimit;
    private Long maxLimit;
    private String saveTrmNote;
    private String policyLink;
    private Boolean hasCalculator;
    private Integer calcPeriodMonths;
    private BigDecimal maxRate;
    private BigDecimal normalMatchRate;
    private BigDecimal preferMatchRate;
}
