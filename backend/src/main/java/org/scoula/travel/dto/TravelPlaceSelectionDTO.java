package org.scoula.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 여행 목표에 저장할 관심 관광지·맛집 DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlaceSelectionDTO {

    private String type;
    private String name;
    private String info;
    private String image;
}
