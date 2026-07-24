package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobInterestedTypeVO extends BaseVO  {
    private Long interestedId;
    private Long goalId;
    private String itemType;
}
