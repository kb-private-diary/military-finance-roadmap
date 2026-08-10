package org.scoula.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarBudgetStatusResponseDTO {
    private Long goalId;
    private Long effectiveBudget;  // 실제 기준 예산(만원) — 수동 입력값 또는 군적금 만기예상액
    private Long purchaseTotal;    // 선택 차량 구매 총액(만원) — 가격+취득세
    private Boolean withinBudget;

    // 후회소비 인사이트 (오픈뱅킹 미연동 또는 후회소비 0원이면 전부 null)
    private Long avgRegretSpending;    // 최근 N개월 월평균 후회소비(만원)
    private Integer regretSavingsMonths; // 절감 기준 개월수
    private Long regretSavingsAmount;  // avgRegretSpending × regretSavingsMonths(만원)
    private Long remainingAmount;      // 목표금액까지 남은 금액(만원) — effectiveBudget - 현재 저축액
}
