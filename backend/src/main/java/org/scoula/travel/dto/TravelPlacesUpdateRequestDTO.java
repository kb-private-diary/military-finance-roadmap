package org.scoula.travel.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 여행 목표 관심 관광지·맛집 수정 요청 DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlacesUpdateRequestDTO {

    private List<TravelPlaceSelectionDTO> places;
}
