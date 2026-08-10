package org.scoula.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Step 2에서 선택한 여행 스타일 저장 요청 DTO.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelCostStyleUpdateRequestDTO {

    private String style;
}
