package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

// service_criteria 테이블 매핑 VO (목표유형별 정부 정책·KB 금융서비스 기준 정보)
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceCriteriaVO extends BaseVO {
    private Long svcCritId;
    private String goalType;
    private String serviceType;
    private String serviceName;
    private String serviceDesc;
    private String useTime;
    private String infoUrl;
}
