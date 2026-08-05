package org.scoula.car.domain;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarTaxPrepayVO extends BaseVO {
    private Long prepayId;
    private Integer year;
    private String prepayMonth;
    private BigDecimal discountRate;
}
