package org.scoula.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.dashboard.domain.VacationVO;

// DASH-API-05 응답. 카테고리 상관없이 부여(grant) 정보 + 사용내역 목록을 같이 내려준다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardVacationDetailResponseDTO {
    private Long vacationId;
    private String category;
    private String name;
    private Integer days;
    private LocalDate acquiredDate;
    private Integer remainingDays;
    private Boolean isUsed;
    private List<DashboardVacationUsageDTO> usages;

    public static DashboardVacationDetailResponseDTO of(
            VacationVO vo, Integer remainingDays, List<DashboardVacationUsageDTO> usages) {
        return DashboardVacationDetailResponseDTO.builder()
                .vacationId(vo.getVacationId())
                .category(vo.getVacationCate())
                .name(vo.getVacationName())
                .days(vo.getVacationDay())
                .acquiredDate(vo.getVacationGet())
                .remainingDays(remainingDays)
                .isUsed(remainingDays <= 0)
                .usages(usages)
                .build();
    }
}
