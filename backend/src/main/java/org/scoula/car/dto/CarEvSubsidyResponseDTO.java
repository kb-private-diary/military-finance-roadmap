package org.scoula.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarEvSubsidyResponseDTO {
    private Long goalId;
    private String modelName;
    private Long basePrice;         // 신차 기준가격(만원)
    private String region;
    private Long nationalSubsidy;   // 국비보조금(만원)
    private Long localSubsidy;      // 지방비보조금(만원)
    private Long totalSubsidy;      // 보조금 합계(만원)
    private Integer baseYear;
    private Long finalPrice;        // 보조금 반영 최종가격(만원)
}
