package org.scoula.travel.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TravelInsuranceVO extends BaseVO {

    private Long insuranceId;
    private String title;
    private String insuranceInf;
    private Integer insurancePeriod;
    private String insuranceUrl;
}
