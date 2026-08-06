package org.scoula.car.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarEvVO extends BaseVO {
    private Long subsidyId;
    private String region;
    private Long nationalSubsidy;
    private Long localSubsidy;
    private Integer baseYear;
}
