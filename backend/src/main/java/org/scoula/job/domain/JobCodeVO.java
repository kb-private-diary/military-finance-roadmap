package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;


@Data
@EqualsAndHashCode(callSuper = true)
public class JobCodeVO extends BaseVO {
    private Long jobCodeId;
    private String goalType;
    private String codeName;
    private String infoUrl;
}
