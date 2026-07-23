package org.scoula.simulator.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatorSavingHistoryDTO {
    private Long accountId;
    private Integer payRound;
    private Long payAmount;
    private LocalDate createdDate;
}
