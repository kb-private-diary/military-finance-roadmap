package org.scoula.car.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarInsuranceVO extends BaseVO {
    private Long insuranceId;
    private Integer carTypeCode;
    private String experienceBracket;
    private Long estimatedPremiumMin;
    private Long estimatedPremiumMax;
}
