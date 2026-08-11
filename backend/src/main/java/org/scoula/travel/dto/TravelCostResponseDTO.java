package org.scoula.travel.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.travel.domain.TravelCostVO;

// 예상 경비 산출 결과 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelCostResponseDTO {

    private Long costId;
    private Long goalId;
    private Long flightCost;
    private Long hotelCost;
    private Long livingCost;
    private Long totalCost;
    private Long remainingBudget;
    private String selectedStyle;
    private List<TravelStyleCostResponseDTO> styleCosts;

    public static TravelCostResponseDTO of(
            final TravelCostVO vo,
            final String selectedStyle,
            final List<TravelStyleCostResponseDTO> styleCosts) {
        final TravelStyleCostResponseDTO selectedCost = styleCosts.stream()
                .filter(cost -> cost.getStyle().equals(selectedStyle))
                .findFirst()
                .orElse(null);
        return TravelCostResponseDTO.builder()
                .costId(vo.getCostId())
                .goalId(vo.getGoalId())
                .flightCost(selectedCost == null
                        ? vo.getFlightCost()
                        : selectedCost.getFlightCost())
                .hotelCost(selectedCost == null
                        ? vo.getHotelCost()
                        : selectedCost.getHotelCost())
                .livingCost(selectedCost == null
                        ? vo.getLivingCost()
                        : selectedCost.getLivingCost())
                .totalCost(selectedCost == null
                        ? vo.getTotalCost()
                        : selectedCost.getTotalCost())
                .remainingBudget(selectedCost == null
                        ? vo.getRemainingBudget()
                        : selectedCost.getRemainingBudget())
                .selectedStyle(selectedStyle)
                .styleCosts(styleCosts)
                .build();
    }

}
