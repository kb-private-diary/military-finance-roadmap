package org.scoula.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 계좌 1건의 은행명 + 월 납입액 (장병내일준비적금은 최대 2개 은행까지 동시 가입 가능해 계좌별로 내려준다)
// 필드 2개라 @Builder는 안 씀 (컨벤션: Builder는 파라미터 3개 이상일 때만)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulatorSavingBankDTO {
    private String bankName;
    private Long monthlySave;
}
