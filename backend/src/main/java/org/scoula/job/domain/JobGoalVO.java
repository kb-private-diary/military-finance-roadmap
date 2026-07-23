package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobGoalVO extends BaseVO {
    private Long goalId;
    private Long userId;
    private String goalType;
    private Long jobCodeId;
    private String expectedDate;
    private String status;
}
