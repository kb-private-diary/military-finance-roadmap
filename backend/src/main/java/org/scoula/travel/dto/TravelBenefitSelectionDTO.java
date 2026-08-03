package org.scoula.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelBenefitSelectionDTO {

    private String type;
    private String productId;
    private String name;

    public static TravelBenefitSelectionDTO of(
            final TravelFinancialProductResponseDTO product) {
        return TravelBenefitSelectionDTO.builder()
                .type(product.getType())
                .productId(product.getProductId())
                .name(product.getName())
                .build();
    }
}
