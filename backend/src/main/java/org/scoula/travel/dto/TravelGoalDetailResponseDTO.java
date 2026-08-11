package org.scoula.travel.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.travel.domain.TravelGoalVO;

/**
 * 저장된 여행 로드맵 상세 응답.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelGoalDetailResponseDTO {

    private Long goalId;
    private String title;
    private String departure;
    private String destination;
    private Boolean isDomestic;
    private String style;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalBudget;
    private String status;
    private TravelCostResponseDTO cost;
    private List<TravelPlaceSelectionDTO> places;
    private TravelPackageResponseDTO selectedPackage;
    private TravelProductRecommendationResponseDTO products;
    private TravelBudgetPlanResponseDTO budgetPlan;
    private TravelRegretInsightResponseDTO regretInsight;

    public static TravelGoalDetailResponseDTO of(
            final TravelGoalVO goal,
            final TravelCostResponseDTO cost,
            final List<TravelPlaceSelectionDTO> places,
            final TravelPackageResponseDTO selectedPackage,
            final TravelProductRecommendationResponseDTO products,
            final TravelBudgetPlanResponseDTO budgetPlan,
            final TravelRegretInsightResponseDTO regretInsight) {
        return TravelGoalDetailResponseDTO.builder()
                .goalId(goal.getGoalId())
                .title(goal.getTitle())
                .departure(goal.getDeparture())
                .destination(goal.getDestination())
                .isDomestic(goal.getIsDomestic())
                .style(goal.getStyle())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .totalBudget(goal.getTotalBudget())
                .status(goal.getStatus())
                .cost(cost)
                .places(places)
                .selectedPackage(selectedPackage)
                .products(products)
                .budgetPlan(budgetPlan)
                .regretInsight(regretInsight)
                .build();
    }
}
