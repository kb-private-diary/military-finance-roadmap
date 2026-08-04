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
public class CarAcquisitionTaxResponseDTO {
    private Long goalId;
    private Long vehiclePrice;              // 차량가격(만원, car_model.base_price)
    private BigDecimal acquisitionTaxRate;   // 취득세율(%)
    private Long acquisitionTaxAmount;       // 취득세액(만원)
    private Integer bondExemptEngineCc;      // 공채매입 면제 배기량 기준(cc)
    private Boolean bondExempt;              // 공채매입 면제 여부
}
