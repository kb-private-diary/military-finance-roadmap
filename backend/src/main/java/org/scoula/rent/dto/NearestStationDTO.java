package org.scoula.rent.dto;

import lombok.Data;

/**
 * 매물 최근접 지하철역 조회 결과 (지역 모드 대중교통 뱃지용)
 * StationMapper.findNearestStation 이 매물 800m 내 가장 가까운 역 1건을 담아 반환한다.
 */
@Data
public class NearestStationDTO {
    private String stationName; // 역명 (일부 원본은 이미 '역'으로 끝남 - 라벨 조립 시 중복 방지 필요)
    private Double distanceM;   // 매물 ↔ 역 직선거리(m), ST_Distance_Sphere 값 (도보시간 = 거리 ÷ 80)
}
