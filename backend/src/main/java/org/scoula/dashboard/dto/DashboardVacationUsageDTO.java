package org.scoula.dashboard.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.dashboard.domain.VacationVO;

// 정기휴가(REGULAR) 사용내역 한 건. 상세페이지에서 등록/삭제로 관리되는 대상이다.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVacationUsageDTO {
    private Long vacationId;
    private String name;
    private Integer days;
    private LocalDate acquiredDate;

    public static DashboardVacationUsageDTO of(VacationVO vo) {
        return new DashboardVacationUsageDTO(
                vo.getVacationId(),
                vo.getVacationName(),
                vo.getVacationDay(),
                vo.getVacationGet()
        );
    }
}
