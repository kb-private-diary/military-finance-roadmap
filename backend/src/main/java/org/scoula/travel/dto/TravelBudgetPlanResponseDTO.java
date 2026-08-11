package org.scoula.travel.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 예산 부족 시 여행 출발 시점에 따라 제공하는 자금 준비 안내.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelBudgetPlanResponseDTO {

    private String planType;
    private Long shortfall;
    private LocalDate dischargeDate;
    private Long monthlySalary;
    private Long monthlySaving;
    private Long monthlyAvailableAmount;
    private Integer requiredMonths;
    private Long expectedMaturityAmount;
    private Long remainingAfterTravel;
}
