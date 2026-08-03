package org.scoula.car.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarModelVO extends BaseVO {
    private Long modelId;
    private String manufacturer;
    private String modelName;
    private Integer carTypeCode;
    private String fuelType;
    private Long basePrice;
}
