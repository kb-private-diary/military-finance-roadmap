package org.scoula.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.dashboard.domain.VacationVO;

// DASH-API-05 응답. REGULAR 마스터 조회 시에만 remainingDays·usages가 채워지고,
// 그 외(포상·위로 등)는 acquiredDate·isUsed 위주로 채워진다.
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
        boolean isRegularMaster = remainingDays != null;
        return DashboardVacationDetailResponseDTO.builder()
                .vacationId(vo.getVacationId())
                .category(vo.getVacationCate())
                .name(vo.getVacationName())
                .days(vo.getVacationDay())
                .acquiredDate(isRegularMaster ? null : vo.getVacationGet())
                .remainingDays(remainingDays)
                .isUsed(vo.getVacationState())
                .usages(usages)
                .build();
    }
}
