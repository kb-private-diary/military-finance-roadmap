package org.scoula.simulator.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatorSavingDetailsResponseDTO {
    // 대표 계좌(가장 먼저 개설된 계좌) 기준. 계좌가 여러 개여도 개설일·만기일·가입개월수·현재납입개월수는 하나로 통일해서 내려준다.
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate accountOpenDate;   // 계좌 개설일자 (대표 계좌)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maturityDate;      // 만기일자 (대표 계좌)

    private Long monthlySaveTotal;       // 계좌 2개를 합친 월 납입액
    private Integer joinableMonths;      // 가입개월수 (대표 계좌 기준)

    private Long currentPaidAmount;      // 현재납입액 (전 계좌 합산)
    private Integer currentPaidMonths;   // 현재 납입개월수 (대표 계좌 기준)
    private Long currentPaidInterest;    // 현재까지 낸 원금에 대한 누적 이자 (전 계좌 합산)

    private Long expectedPrincipal;      // 납입원금
    private Long expectedInterest;       // 이자
    private Long expectedMatchingFund;   // 정부매칭지원금 (복무개월수에 따른 한도 제한 적용됨)

    private Long totalReceiptAmount;     // 총 수령액
}
