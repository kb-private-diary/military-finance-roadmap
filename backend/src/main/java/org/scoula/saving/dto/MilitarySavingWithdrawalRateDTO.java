package org.scoula.saving.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// military_saving_product 중도해지 구간 조회 결과. valueUnit(MONTH/RATIO)에 따라
// 적용 공식이 달라서, 어느 구간 기준으로 매칭됐는지 같이 내려준다.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilitarySavingWithdrawalRateDTO {
    private BigDecimal rateRatio;
    private BigDecimal floorRate;
    private String valueUnit;
}
