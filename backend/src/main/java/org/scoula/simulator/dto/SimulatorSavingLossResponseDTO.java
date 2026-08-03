package org.scoula.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulatorSavingLossResponseDTO {
    private Long withdrawalAmount; // 지금 중도해지 시 수령액 (원금 + 중도해지이자, 매칭지원금 제외)
    private Long lossAmount;       // 만기까지 유지했을 때 대비 손실금 (만기수령액 - 중도해지수령액)
}
