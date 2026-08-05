package org.scoula.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarRecommendationResponseDTO {
    private Long modelId;
    private String manufacturer;
    private String modelName;
    private Integer carTypeCode;
    private String fuelType;
    private Long baseNewPrice;          // 신차 기준가격(만원)
    private Integer assumedYear;        // 중고 추정 연식 (신차는 null)
    private Long estimatedPrice;        // 신차/중고 여부 반영한 추정가격(만원)
    private Long acquisitionTaxAmount;  // 추정가격 기준 취득세(만원)
    private Long totalPrice;            // 추정가격 + 취득세(만원)
    private Boolean withinBudget;
}
