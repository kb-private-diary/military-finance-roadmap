package org.scoula.travel.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.product.dto.CardProductListResponseDTO;
import org.scoula.product.domain.SavingProductVO;
import org.scoula.travel.domain.TravelInsuranceVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelFinancialProductResponseDTO {

    private String type;
    private String productId;
    private String name;
    private String description;
    private String qualification;
    private String url;

    public static TravelFinancialProductResponseDTO ofCard(
            final CardProductListResponseDTO card) {
        return TravelFinancialProductResponseDTO.builder()
                .type("card")
                .productId(String.valueOf(card.getCardId()))
                .name(card.getCardName())
                .description(card.getCardDesc())
                .qualification(card.getCardQual())
                .url(card.getCardUrl())
                .build();
    }

    public static TravelFinancialProductResponseDTO ofSaving(
            final SavingProductVO saving) {
        return TravelFinancialProductResponseDTO.builder()
                .type("saving")
                .productId(saving.getFinPrdtCd())
                .name(saving.getProductName())
                .description(createSavingDescription(saving))
                .qualification(saving.getJoinMember())
                .url(saving.getProductLink())
                .build();
    }

    public static TravelFinancialProductResponseDTO ofInsurance(
            final TravelInsuranceVO insurance) {
        return TravelFinancialProductResponseDTO.builder()
                .type("insurance")
                .productId(String.valueOf(insurance.getInsuranceId()))
                .name(insurance.getTitle())
                .description(insurance.getInsuranceInf())
                .qualification(createInsuranceQualification(insurance))
                .url(insurance.getInsuranceUrl())
                .build();
    }

    private static String createSavingDescription(
            final SavingProductVO saving) {
        final StringBuilder description = new StringBuilder();
        final BigDecimal maxRate = saving.getMaxRate();
        if (maxRate != null) {
            description.append("최고 연 ")
                    .append(maxRate.stripTrailingZeros().toPlainString())
                    .append('%');
        }
        if (saving.getSaveTrm() != null) {
            if (description.length() > 0) {
                description.append(" · ");
            }
            description.append(saving.getSaveTrm()).append("개월");
        }
        return description.toString();
    }

    private static String createInsuranceQualification(
            final TravelInsuranceVO insurance) {
        if (insurance.getInsurancePeriod() == null) {
            return null;
        }
        return "가입 가능 여행기간 최대 "
                + insurance.getInsurancePeriod()
                + "일";
    }
}
