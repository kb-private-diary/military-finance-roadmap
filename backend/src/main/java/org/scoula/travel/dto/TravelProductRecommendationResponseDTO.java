package org.scoula.travel.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelProductRecommendationResponseDTO {

    private List<TravelFinancialProductResponseDTO> cards;
    private List<TravelFinancialProductResponseDTO> savings;
    private List<TravelFinancialProductResponseDTO> insurances;
    private List<TravelProductSelectionDTO> selectedProducts;

    public static TravelProductRecommendationResponseDTO of(
            final List<TravelFinancialProductResponseDTO> cards,
            final List<TravelFinancialProductResponseDTO> savings,
            final List<TravelFinancialProductResponseDTO> insurances,
            final List<TravelProductSelectionDTO> selectedProducts) {
        return TravelProductRecommendationResponseDTO.builder()
                .cards(cards)
                .savings(savings)
                .insurances(insurances)
                .selectedProducts(selectedProducts)
                .build();
    }
}
