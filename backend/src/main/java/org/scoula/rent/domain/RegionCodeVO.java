package org.scoula.rent.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegionCodeVO extends BaseVO {
    private String regionCode;
    private String sidoName;
    private String sigunguName;
    private String umdName;
    private String sigunguCode;
    private String isAbolished;
}