package org.scoula.rent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 주변 편의시설 1건 (Step5 저장 후 상세)
 * 카카오 로컬 카테고리 검색 결과를 도보 분 단위로 환산해 담는다
 */
@Data
@AllArgsConstructor
public class NearbyFacilityDTO {
    private String type;      // SUBWAY / CONVENIENCE / HOSPITAL / MART
    private String name;      // 장소명 (예: 부산대역)
    private int walkMinutes;  // 도보 분 (직선거리 기준 환산)
}
