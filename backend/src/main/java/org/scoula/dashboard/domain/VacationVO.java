package org.scoula.dashboard.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

// vacation 테이블 매핑 VO. 부여(grant)만 나타내며, 사용내역은 VacationHistoryVO가 별도로 가진다.
// 잔여일수는 vacationDay - SUM(VacationHistoryVO.usedDay)로 서비스에서 파생 계산한다.
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
}
