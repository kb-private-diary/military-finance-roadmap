package org.scoula.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarMaintenanceCostResponseDTO {
    private Long goalId;
    private String fuelType;
    private Long fuelPricePerLiter;      // 원/리터 (오피넷 전국 평균)
    private Integer annualMileageKm;     // 연간 예상 주행거리(km) 가정값
    private Integer fuelEfficiencyKmPerLiter; // 차종별 평균 연비(km/L) 가정값
    private Long estimatedFuelCostAnnual;     // 연간 예상 연료비(만원)
    private Long insurancePremiumMin;         // 연간 예상 보험료 최소(만원)
    private Long insurancePremiumMax;         // 연간 예상 보험료 최대(만원)
    private Long totalMaintenanceCostMin;     // 연료비 + 보험료 최소 합계(만원)
    private Long totalMaintenanceCostMax;     // 연료비 + 보험료 최대 합계(만원)
}
