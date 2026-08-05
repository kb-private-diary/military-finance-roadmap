package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobQualificationVO extends BaseVO {

    private Long qualId;

    // Q01: 자격증, Q02: 어학
    private String qualType;
    private String externalCode;
    // D01: 수기, D02: Q-Net, D03: 기타API
    private String dataSource;
    private String qualName;
    private String qualSummary;
    private String organizationName;
    private Long writtenFee;
    private Long practicalFee;
    private Long militaryFee;
    // 목표에 선택될 당시 적용된 총비용
    private Long selectedCost;
    private String detailUrl;
    private LocalDateTime lastSyncedDate;

    private Integer examYear;
    private String examRound;
    private LocalDate writtenRegStartDate;
    private LocalDate writtenRegEndDate;
    private LocalDate writtenExamStartDate;
    private LocalDate writtenExamEndDate;
    private LocalDate writtenResultDate;
    private LocalDate practicalRegStartDate;
    private LocalDate practicalRegEndDate;
    private LocalDate practicalExamStartDate;
    private LocalDate practicalExamEndDate;
    private LocalDate practicalResultDate;

}
