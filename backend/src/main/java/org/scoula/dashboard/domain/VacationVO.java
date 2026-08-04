package org.scoula.dashboard.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

// vacation 테이블 매핑 VO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class VacationVO extends BaseVO {
    private Long vacationId;
    private Long userId;
    private String vacationCate;
    private String vacationName;
    private LocalDate vacationGet;
    private Integer vacationDay;
    private Boolean vacationState;
}
