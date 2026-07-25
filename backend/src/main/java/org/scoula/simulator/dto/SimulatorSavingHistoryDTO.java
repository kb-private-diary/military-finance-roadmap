package org.scoula.simulator.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.MilitarySavingsCalculator;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatorSavingHistoryDTO implements MilitarySavingsCalculator.SavingHistory {
    private Long accountId;
    private Integer payRound;
    private Long payAmount;
    private LocalDate createdDate;
}
