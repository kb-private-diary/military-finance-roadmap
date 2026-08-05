package org.scoula.simulator.dto;

import java.util.List;
import lombok.Data;

@Data
public class SimulatorVariableCalcRequestDTO {
    private List<Period> periods;

    @Data
    public static class Period {
        // 가입 개월차 (1부터 시작). 예: 1~5개월차, 6~18개월차
        private Integer startMonthOffset;
        private Integer endMonthOffset;
        private Long amount;
    }
}
