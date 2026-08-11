package org.scoula.dashboard.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.dashboard.domain.VacationVO;

// 휴가 카드 목록의 항목 하나. 카테고리 상관없이 부여(grant) 1건이 곧 카드 1건이며,
// 잔여일수는 서비스가 사용내역 합계로 계산해서 넘겨준다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardVacationItemDTO {
    private Long vacationId;
    private String category;
    private String name;
    private Integer days;
    private Integer remainingDays;
    private LocalDate acquiredDate;
    private Boolean isUsed;

    public static DashboardVacationItemDTO of(VacationVO vo, Integer remainingDays) {
        return DashboardVacationItemDTO.builder()
                .vacationId(vo.getVacationId())
                .category(vo.getVacationCate())
                .name(vo.getVacationName())
                .days(vo.getVacationDay())
                .remainingDays(remainingDays)
                .acquiredDate(vo.getVacationGet())
                .isUsed(remainingDays <= 0)
                .build();
    }
}
