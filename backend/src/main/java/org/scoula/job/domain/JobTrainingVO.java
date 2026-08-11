package org.scoula.job.domain;
;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobTrainingVO extends BaseVO {

    private Long goalTrainingId;
    private Long goalId;

    // 고용24 훈련과정 식별 정보
    private String externalCode;
    private Integer trainingRound;
    private String institutionId;

    // 훈련과정 기본 정보
    private String trainingName;
    private String institutionName;
    private String trainingType;
    private String address;

    // 선택 당시 전체 훈련비
    private Long trainingCost;
    // 선택 당시 본인부담금
    private Long selectedCost;

    // 훈련기간
    private LocalDate startDate;
    private LocalDate endDate;

    // 고용24 상세 페이지
    private String detailUrl;
}