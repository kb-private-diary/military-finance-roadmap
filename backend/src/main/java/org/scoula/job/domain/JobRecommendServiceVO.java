package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobRecommendServiceVO extends BaseVO {
    private Long serviceId;

    // P01: 정책, P02: KB서비스
    private String serviceType;

    // J01: 취업, J02: 공무원, J03: 편입, NULL: 공통
    private String goalType;

    private String serviceName;

    private String serviceQual;

    private String serviceDesc;

    private String serviceUrl;

    private String externalCode;

    private LocalDateTime lastSyncedDate;

    private Integer sortOrder;
}
