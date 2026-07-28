package org.scoula.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관광지·맛집 추천 응답 DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlaceResponseDTO {

    private String placeId;
    private String title;
    private String type;
    private String address;
    private Double rating;
    private Integer reviews;
    private String price;
    private String thumbnail;
    private Double latitude;
    private Double longitude;
}
