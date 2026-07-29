package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class PrepItemCriteriaVO extends BaseVO {
    private Long prepCritId;
    private String goalType;
    private String itemType;
    private Long jobCodeId;
    private String itemName;
    private String infoUrl;
    private String applyUrl;
    private Long amount;
    private String amountSource;
    private String feeDetail;
    private String externalCode;
}
