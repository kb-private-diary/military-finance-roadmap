package org.scoula.rent.domain;

import lombok.Data;

/**
 * 면적별 관리비 앵커 : area_mgmt_anchor 테이블 한 행을 담는 VO
 * 공용관리비 원/㎡ 단가를 면적 앵커 5점으로 저장, 42㎡ 미만은 첫 앵커 단가 고정
 * 순수 데이터 VO (감사컬럼 없음, BaseVO 미상속)
 */
@Data
public class AreaMgmtAnchorVO {
    private int anchorSeq;       // 앵커 순번 (1~5)
    private double areaSqm;      // 대표 전용면적 ㎡
    private double feePerSqm;    // 공용관리비 원/㎡
    private Integer sampleCount; // 표본 단지수
}
