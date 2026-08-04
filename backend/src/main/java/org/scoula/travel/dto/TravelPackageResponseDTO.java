package org.scoula.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.scoula.travel.domain.TravelPackageVO;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPackageResponseDTO {

    private Long packageId;
    private String goodsCode;
    private String country;
    private String regionName;
    private String name;
    private String imageUrl;
    private String description;
    private Long minPrice;
    private String departurePeriod;
    private String detailUrl;
    private Boolean selected;

    public static TravelPackageResponseDTO of(
            final TravelPackageVO travelPackage,
            final Long selectedPackageId) {
        return TravelPackageResponseDTO.builder()
                .packageId(travelPackage.getPackageId())
                .goodsCode(travelPackage.getGoodsCode())
                .country(travelPackage.getCountry())
                .regionName(travelPackage.getRegionName())
                .name(travelPackage.getName())
                .imageUrl(travelPackage.getImageUrl())
                .description(travelPackage.getDescription())
                .minPrice(travelPackage.getMinPrice())
                .departurePeriod(travelPackage.getDeparturePeriod())
                .detailUrl(travelPackage.getDetailUrl())
                .selected(travelPackage.getPackageId() != null
                        && travelPackage.getPackageId()
                        .equals(selectedPackageId))
                .build();
    }
}
