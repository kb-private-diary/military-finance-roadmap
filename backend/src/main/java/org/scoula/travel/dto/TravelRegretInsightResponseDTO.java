package org.scoula.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 후회소비를 줄였을 때 여행 자금 부족분을 얼마나 채울 수 있는지 보여주는 응답.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelRegretInsightResponseDTO {

    private Long avgRegretSpending;
    private Integer regretLookbackMonths;
    private Integer regretSavingsMonths;
    private Long regretSavingsAmount;
    private Long remainingAmount;
}
