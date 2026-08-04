package org.scoula.car.dto;

import java.math.BigDecimal;

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
    private Long fuelPricePerLiter;      // 원/리터(오피넷 전국 평균) 또는 전기차는 원/kWh
    private Integer annualMileageKm;     // 연간 예상 주행거리(km) 가정값
    private Integer fuelEfficiencyKmPerLiter; // km/L 또는 전기차는 km/kWh 가정값
    private Long estimatedFuelCostAnnual;     // 연간 예상 연료비(만원)
    private Long annualVehicleTaxBase;        // 연간 자동차세 기준액(만원, 연납할인 전)
    private BigDecimal prepayDiscountRate;    // 적용된 연납할인율(%)
    private Long annualVehicleTaxAfterDiscount; // 연납할인 적용 후 연간 자동차세(만원)
    private Long insurancePremiumMin;         // 연간 예상 보험료 최소(만원)
    private Long insurancePremiumMax;         // 연간 예상 보험료 최대(만원)
    private Long totalMaintenanceCostMin;     // 연간 유지비(연료비+자동차세+보험료) 최소 합계(만원)
    private Long totalMaintenanceCostMax;     // 연간 유지비 최대 합계(만원)
    private Long totalMaintenanceCost3YearMin; // 3년 유지비 최소 합계(만원)
    private Long totalMaintenanceCost3YearMax; // 3년 유지비 최대 합계(만원)
}
