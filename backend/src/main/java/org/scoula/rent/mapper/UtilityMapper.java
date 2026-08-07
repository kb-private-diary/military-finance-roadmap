package org.scoula.rent.mapper;

import java.util.List;
import java.util.Map;

import org.scoula.rent.domain.AreaMgmtAnchorVO;
import org.scoula.rent.domain.AreaUsageAnchorVO;
import org.scoula.rent.domain.ElectricRateVO;
import org.scoula.rent.domain.MonthCoefVO;
import org.scoula.rent.domain.RegionMgmtFeeVO;
import org.scoula.rent.domain.RegionUtilityVO;
import org.scoula.rent.domain.SnapshotVO;

/**
 * 공과금·관리비 조회 매퍼
 * 핵심 조회키: 매물 법정동코드 10자리 앞 2자리로 시도 계수 매칭 (LEFT(regionCode, 2))
 */
public interface UtilityMapper {

    // 매물 법정동코드로 시도 전기·난방·수도 계수·단가 조회 (현행 요금)
    RegionUtilityVO findRegionUtility(String regionCode);

    // 면적 사용량 앵커 전량 조회 (5행, 보간은 애플리케이션에서 수행)
    List<AreaUsageAnchorVO> findAreaUsageAnchors();

    // 관리비 면적 앵커 전량 조회 (5행)
    List<AreaMgmtAnchorVO> findAreaMgmtAnchors();

    // 월별 계수 전체 조회 (12행, Step5 진입 시 일괄 응답용)
    List<MonthCoefVO> findAllMonthCoef();

    // 전기 누진 요금표 조회 (현행)
    List<ElectricRateVO> findElectricRates();

    // 공통 상수 조회 (const_key → const_value 맵)
    List<Map<String, Object>> findConstants();

    // 매물 법정동코드로 관리비 지역계수 조회
    RegionMgmtFeeVO findRegionMgmtFee(String regionCode);

    // 로드맵 저장 시 월별 스냅샷 일괄 INSERT
    void insertSnapshot(List<SnapshotVO> list);

    // 로드맵 저장된 월별 스냅샷 조회 (Step5)
    List<SnapshotVO> findSnapshotByRoadmap(Long roadmapId);
}
