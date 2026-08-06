package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobTransferMajorVO extends BaseVO {

    private Long majorId;

    private Long univId;

    private String majorCode;

    // JOIN으로 가져올 컬럼
    private String majorName;

    private Integer admissionYear;
}
