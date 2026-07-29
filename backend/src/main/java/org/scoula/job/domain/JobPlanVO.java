package org.scoula.job.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobPlanVO extends BaseVO {
    private Long planId;
    private Long goalId;
    private String itemType;
    private String itemName;
    private String infoUrl;
    private String applyUrl;
    private Long amount;
}
