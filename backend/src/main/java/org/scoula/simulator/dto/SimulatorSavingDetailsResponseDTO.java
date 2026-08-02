package org.scoula.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatorSavingDetailsResponseDTO {
    private Long monthlySaveTotal;       // 계좌 2개를 합친 월 납입액
    private Integer joinableMonths;      // 실제 납입 개월수 (자동이체 예정일이 만기일을 넘기면 그 전 회차까지만)
    
    private Long currentPaidAmount;      // 현재납입액
    private Integer currentPaidMonths;   // 현재 납입개월수 (최대 회차)
    
    private Long expectedPrincipal;      // 납입원금
    private Long expectedInterest;       // 이자
    private Long expectedMatchingFund;   // 정부매칭지원금 (복무개월수에 따른 한도 제한 적용됨)
    
    private Long totalReceiptAmount;     // 총 수령액
}
