package org.scoula.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarUsedPriceResponseDTO {
    private Long goalId;
    private String modelName;
    private Long baseNewPrice;         // 신차 기준가격(만원)
    private Integer selectedYear;
    private Integer selectedMileageKm;
    private Integer ageYears;
    private Long estimatedUsedPrice;   // 추정 중고 시세(만원)
    private Long acquisitionTaxAmount; // 추정 시세 기준 취득세(만원)
    private Long totalPrice;           // 시세 + 취득세(만원)
}
