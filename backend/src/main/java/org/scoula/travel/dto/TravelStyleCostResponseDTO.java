package org.scoula.travel.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 여행 스타일별 예상 경비 응답 DTO.
 */
@Getter
@Builder
public class TravelStyleCostResponseDTO {

    private final String style;
    private final Long flightCost;
    private final Long hotelCost;
    private final Long livingCost;
    private final Long totalCost;
    private final Long remainingBudget;

    public static TravelStyleCostResponseDTO of(
            final String style,
            final long flightCost,
            final long hotelCost,
            final long livingCost,
            final long totalCost,
            final long remainingBudget) {
        return TravelStyleCostResponseDTO.builder()
                .style(style)
                .flightCost(flightCost)
                .hotelCost(hotelCost)
                .livingCost(livingCost)
                .totalCost(totalCost)
                .remainingBudget(remainingBudget)
                .build();
    }
}
