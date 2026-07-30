package org.scoula.travel.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 도시와 출발 분기를 기준으로 DB 예상 비용을 조회할 때 사용하는 조건 DTO.
 */
@Getter
@RequiredArgsConstructor
public class TravelQuarterCostSearchDTO {

    private final Long cityCostId;
    private final Integer quarter;
}
