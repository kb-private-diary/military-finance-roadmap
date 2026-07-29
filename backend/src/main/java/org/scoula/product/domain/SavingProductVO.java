package org.scoula.product.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

// saving_product 테이블 매핑 VO (금감원 API 기반 KB 예·적금 상품 정보)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class SavingProductVO extends BaseVO {
    private Long savingId;
    private String productType;
    private Boolean isActive;
    private String finPrdtCd;
    private String korCoNm;
    private String productName;
    private String joinMember;
    private Long minLimit;
    private Long maxLimit;
    private String etcNote;
    private String intrRateType;
    private String rsrvType;
    private Integer saveTrm;
    private String spclCnd;
    private BigDecimal basicRate;
    private BigDecimal maxRate;
    private Boolean isTaxExempt;
    private BigDecimal govMatchRate;
    private String productLink;
}
