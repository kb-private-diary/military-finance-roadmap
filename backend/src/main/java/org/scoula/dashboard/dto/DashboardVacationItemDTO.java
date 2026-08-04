package org.scoula.dashboard.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.dashboard.domain.VacationVO;

// 휴가 카드 목록의 항목 하나. REGULAR(정기휴가)는 마스터 1건으로 집계해서 카드 하나로,
// 그 외 카테고리는 행 하나가 곧 카드 하나로 내려간다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardVacationItemDTO {
    private Long vacationId;
    private String category;
    private String name;
    private Integer days;
    private Integer remainingDays;  // REGULAR만 값 있음(부여일수 - 사용일수 합계)
    private LocalDate acquiredDate; // REGULAR 외엔 값 있음
    private Boolean isUsed;

    public static DashboardVacationItemDTO of(VacationVO vo) {
        return DashboardVacationItemDTO.builder()
                .vacationId(vo.getVacationId())
                .category(vo.getVacationCate())
                .name(vo.getVacationName())
                .days(vo.getVacationDay())
                .acquiredDate(vo.getVacationGet())
                .isUsed(vo.getVacationState())
                .build();
    }

    public static DashboardVacationItemDTO ofRegularMaster(
            VacationVO master, Integer remainingDays) {
        return DashboardVacationItemDTO.builder()
                .vacationId(master.getVacationId())
                .category(master.getVacationCate())
                .name(master.getVacationName())
                .days(master.getVacationDay())
                .remainingDays(remainingDays)
                .isUsed(remainingDays <= 0)
                .build();
    }
}
