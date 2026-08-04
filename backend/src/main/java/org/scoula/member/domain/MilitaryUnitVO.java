package org.scoula.member.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class MilitaryUnitVO extends BaseVO {
    private String unitCode;
    private String unitName;
    private Integer typeId;
}
