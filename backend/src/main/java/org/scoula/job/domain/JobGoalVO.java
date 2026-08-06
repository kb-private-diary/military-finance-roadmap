package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobGoalVO extends BaseVO {
    private Long goalId;
    private Long userId;

    // J01:취업, J02:공무원, J03:편입
    private String goalType;

    // 취업/공무원
    private Long categoryId;

    // 편입
    private Long univId;
    private Long majorId;

    // YYYY-MM
    private String expectedDate;

    // DRAFT, CONFIRMED
    private String status;
}
