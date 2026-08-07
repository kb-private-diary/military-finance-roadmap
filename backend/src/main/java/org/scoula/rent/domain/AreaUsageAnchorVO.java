package org.scoula.rent.domain;

import lombok.Data;

/**
 * 면적별 사용량 앵커 : area_usage_anchor 테이블 한 행을 담는 VO
 * 전기 kWh·난방비 기준액을 면적 앵커 5점으로 저장, 앵커 사이는 선형보간
 * 순수 데이터 VO (감사컬럼 없음, BaseVO 미상속)
 */
@Data
public class AreaUsageAnchorVO {
    private int anchorSeq;    // 앵커 순번 (1~5)
    private double areaSqm;   // 대표 전용면적 ㎡ (구간 중앙값)
    private double elecKwh;   // 월 전기 사용량 kWh
    private int heatFee;      // 월 난방비 원 (전국 기준)
    private Integer sampleN;  // 표본 가구수
}
