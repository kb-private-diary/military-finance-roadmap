package org.scoula.saving.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// military_saving_product 만기 구간 조회 결과 (해당 구간의 기본금리 + 정부매칭비율)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilitarySavingRateDTO {
    private BigDecimal basicRate;
    private BigDecimal govMatchRate;
}
