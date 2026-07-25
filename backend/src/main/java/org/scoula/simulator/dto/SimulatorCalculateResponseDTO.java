package org.scoula.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatorCalculateResponseDTO {
    private Long totalPrincipal;
    private Long totalInterest;
    private Long totalMatchingFund;
    private Long totalReceiptAmount;
}
