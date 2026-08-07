package org.scoula.rent.dto;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.RentAffordability;

/**
 * 부족분 계산 응답 DTO (Step4 빨간 카드)
 * 총 필요자금(calculateCost) vs 만기금(군적금 예상 만기 수령액) 비교
 * 부족분(대출 필요액)·여유분·감당도 판정을 함께 내려줌
 */
@Data
@Builder
public class RentAffordabilityResponseDTO {
    private Long listingId;            // 매물번호
    private Integer months;            // 거주 개월수 (Step3에서 확정)
    private Long totalRequired;        // 총 필요자금 (보증금 + 월주거비)
    private Long maturityAmount;       // 만기금 (군적금 예상 만기 수령액)
    private Long shortfall;            // 부족분 = max(0, 총필요 - 만기금) → 대출 필요액 (0이면 충분)
    private Long surplus;              // 여유분 = max(0, 만기금 - 총필요)
    private String affordability;      // 감당도 코드 (SUFFICIENT / TIGHT / OVER)
    private String affordabilityLabel; // 감당도 라벨 (딱 맞아요 / 빠듯해요 / 예산 초과)

    public static RentAffordabilityResponseDTO of(Long listingId, int months,
                                                  long totalRequired, long maturityAmount) {
        long shortfall = Math.max(0L, totalRequired - maturityAmount); // 음수면 0 (부족 없음)
        long surplus = Math.max(0L, maturityAmount - totalRequired);   // 남는 만기금
        RentAffordability level = RentAffordability.judge(totalRequired, maturityAmount);
        return RentAffordabilityResponseDTO.builder()
                .listingId(listingId)
                .months(months)
                .totalRequired(totalRequired)
                .maturityAmount(maturityAmount)
                .shortfall(shortfall)
                .surplus(surplus)
                .affordability(level.name())
                .affordabilityLabel(level.getLabel())
                .build();
    }
}
