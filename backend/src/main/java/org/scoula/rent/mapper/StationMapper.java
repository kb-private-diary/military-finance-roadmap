package org.scoula.rent.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.rent.dto.NearestStationDTO;

import java.math.BigDecimal;

/**
 * 지하철역(station) 마스터 매퍼 - 자취 지역 모드 대중교통 뱃지용
 * (RentListingMapper 와 분리 - 역 마스터는 매물과 무관한 별도 도메인 데이터)
 */
public interface StationMapper {

    // 매물 좌표(lat,lng) 기준 800m(도보 10분) 이내 최근접 지하철역 1건 (없으면 null)
    //   ST_Distance_Sphere 로 실제 직선거리(m)를 재 역명 + 거리를 반환 (RentListingMapper 의 학교 거리 패턴과 동일)
    NearestStationDTO findNearestStation(@Param("lat") BigDecimal lat,
                                         @Param("lng") BigDecimal lng);
}
