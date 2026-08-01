package org.scoula.member.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class MilitaryTypeVO extends BaseVO {
    private Integer typeId;
    private String typeName;
}
