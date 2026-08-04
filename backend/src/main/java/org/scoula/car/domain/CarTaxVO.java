package org.scoula.car.domain;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarTaxVO extends BaseVO {
    private Long taxId;
    private Integer carTypeCode;
    private BigDecimal acquisitionTaxRate;
    private Integer bondExemptEngineCc;
    private String region;
}
