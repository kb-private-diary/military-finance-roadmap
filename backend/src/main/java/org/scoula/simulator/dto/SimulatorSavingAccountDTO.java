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
public class SimulatorSavingAccountDTO {
    private Long accountId;
    private Long monthlySave;
    private LocalDate createdDate;
}
