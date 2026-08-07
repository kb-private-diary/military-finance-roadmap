package org.scoula.rent.domain;

import java.time.LocalDate;

import lombok.Data;

/**
 * 시도별 공과금 계수·단가 : region_utility 테이블 한 행을 담는 VO
 * 매물 법정동코드 앞 2자리(시도)로 조회하여 전기·난방·수도 계산에 주입
 * 순수 데이터 VO (감사컬럼 없음, BaseVO 미상속)
 */
@Data
public class RegionUtilityVO {
    private String sidoCode;      // 시도코드 2자리
    private String sidoName;      // 시도명
    private double elecCoef;      // 전기 사용량 계수 (전국=1.000)
    private double heatCoef;      // 난방비 계수 (전국=1.000)
    private Integer heatSample;   // 난방 계수 표본 가구수 (불확실성 표기용)
    private double waterUsageM3;  // 1인 월 물사용량 ㎥
    private double waterRate;     // 상수도 원/㎥
    private double sewerRate;     // 하수도 원/㎥
    private LocalDate effectiveFrom; // 요율 적용 시작일 (스냅샷 기준일)
}
