package org.scoula.product.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.product.domain.SavingProductVO;

// 가입기간(save_trm)별 기본금리 — 시뮬레이터가 사용자가 고른 개월수에 맞는 금리를 찾는 데 사용
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveTrmRateDTO {

    private Integer saveTrm;
    private BigDecimal basicRate;

    public static SaveTrmRateDTO of(SavingProductVO vo) {
        return new SaveTrmRateDTO(vo.getSaveTrm(), vo.getBasicRate());
    }
}
