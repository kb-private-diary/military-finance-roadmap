package org.scoula.car.domain;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarGoalVO extends BaseVO {
    private Long goalId;
    private Long userId;
    private Long budget;
    private Integer carTypeCode;
    private Boolean isNew;
    private Integer experienceYears;
    private LocalDate targetDate;
    private String region;
    private Long selectedModelId;
    private Integer selectedYear;
    private String status;
}
