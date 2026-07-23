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
    private Integer joinableMonths;      // 납입가능 개월 (계좌 개설일부터 전역일까지 +1 포함)
    
    private Long currentPaidAmount;      // 현재납입액
    private Integer currentPaidMonths;   // 현재 납입개월수 (최대 회차)
    
    private Long expectedPrincipal;      // 납입원금
    private Long expectedInterest;       // 이자
    private Long expectedMatchingFund;   // 정부매칭지원금 (복무개월수에 따른 한도 제한 적용됨)
    
    private Long totalReceiptAmount;     // 총 수령액
}
