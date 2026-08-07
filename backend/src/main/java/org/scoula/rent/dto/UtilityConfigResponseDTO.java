package org.scoula.rent.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.AreaMgmtAnchorVO;
import org.scoula.rent.domain.AreaUsageAnchorVO;
import org.scoula.rent.domain.ElectricRateVO;
import org.scoula.rent.domain.MonthCoefVO;
import org.scoula.rent.domain.RegionMgmtFeeVO;
import org.scoula.rent.domain.RegionUtilityVO;

/**
 * Step5 진입 시 공과금 계산에 필요한 계수·요금표·앵커 일괄 응답 DTO
 * 프론트가 한 번 받아 캐싱(Pinia)하고 거주기간 슬라이더 조작 시 즉시 재계산하도록 함
 * 계산 로직과 요금 단가를 분리 - 요금 개정은 DB UPDATE만으로 반영
 */
@Data
@Builder
public class UtilityConfigResponseDTO {
    private RegionUtilityVO regionUtility;      // 시도 전기·난방·수도 계수·단가
    private RegionMgmtFeeVO regionMgmtFee;      // 관리비 지역계수
    private List<AreaUsageAnchorVO> usageAnchors; // 면적 사용량 앵커 (전기·난방)
    private List<AreaMgmtAnchorVO> mgmtAnchors;   // 면적 관리비 앵커 (원/㎡)
    private List<MonthCoefVO> monthCoefs;         // 월별 계수 (전기·난방)
    private List<ElectricRateVO> electricRates;   // 전기 누진 요금표
    private Map<String, Object> constants;        // 공통 상수 (const_key → const_value)
}
