package org.scoula.dashboard.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.dashboard.domain.VacationHistoryVO;

// 휴가 사용내역 한 건. 상세페이지에서 등록/삭제로 관리되는 대상이다.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVacationUsageDTO {
    private Long historyId;
    private Integer days;
    private LocalDate usedDate;

    public static DashboardVacationUsageDTO of(VacationHistoryVO vo) {
        return new DashboardVacationUsageDTO(
                vo.getHistoryId(),
                vo.getUsedDay(),
                vo.getUsedDate()
        );
    }
}
