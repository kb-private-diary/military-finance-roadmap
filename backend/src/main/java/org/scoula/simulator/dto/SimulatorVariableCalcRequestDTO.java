package org.scoula.simulator.dto;

import java.util.List;
import lombok.Data;

@Data
public class SimulatorVariableCalcRequestDTO {
    private List<Period> periods;

    @Data
    public static class Period {
        // "yyyy-MM" 형식 (예: "2024-01")
        private String startMonth;
        private String endMonth;
        private Long amount;
    }
}
