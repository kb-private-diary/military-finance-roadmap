package org.scoula.simulator.dto;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulatorUserDatesDTO {
    private LocalDate enlistDate;
    private LocalDate dischargeDate;
}
