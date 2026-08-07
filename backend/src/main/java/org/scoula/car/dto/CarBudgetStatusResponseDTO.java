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
}
