package org.scoula.dashboard.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

// vacation_history 테이블 매핑 VO. vacation(부여) 한 건에 걸린 사용내역 한 건을 나타낸다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class VacationHistoryVO extends BaseVO {
    private Long historyId;
    private Long vacationId;
    private LocalDate usedDate;
    private Integer usedDay;
}
