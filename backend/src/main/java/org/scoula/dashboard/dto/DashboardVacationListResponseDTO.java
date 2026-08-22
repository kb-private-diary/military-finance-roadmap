package org.scoula.dashboard.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DASH-API-03 응답. totalDays/usedDays/remainingDays는 전체 카테고리 합산값이다.
// REGULAR는 마스터의 days만 총량에 반영하고(사용내역은 그 안에서 차감되는 몫이라 중복 합산하지 않음),
// 그 외 카테고리는 건별 days를 그대로 합산한다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardVacationListResponseDTO {
    private Integer totalDays;
    private Integer usedDays;
    private Integer remainingDays;
    private List<DashboardVacationItemDTO> vacations;
}
