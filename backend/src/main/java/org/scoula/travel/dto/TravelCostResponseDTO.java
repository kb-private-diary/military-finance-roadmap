package org.scoula.travel.dto;

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

    public static TravelCostResponseDTO of(TravelCostVO vo) {
        return TravelCostResponseDTO.builder()
                .costId(vo.getCostId())
                .goalId(vo.getGoalId())
                .flightCost(vo.getFlightCost())
                .hotelCost(vo.getHotelCost())
                .livingCost(vo.getLivingCost())
                .totalCost(vo.getTotalCost())
                .remainingBudget(vo.getRemainingBudget())
                .build();
    }

}