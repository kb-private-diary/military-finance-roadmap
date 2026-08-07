package org.scoula.rent.domain;

import java.time.LocalDate;

import lombok.Data;

/**
 * 로드맵 월별 공과금·관리비 스냅샷 : roadmap_utility_snapshot 테이블 한 행을 담는 VO
 * 로드맵 저장 시 거주 N개월치를 월별로 저장, 요금 개정 후에도 저장 당시 금액 유지
 * 순수 데이터 VO (감사컬럼 없음, BaseVO 미상속)
 */
@Data
public class SnapshotVO {
    private Long roadmapId;      // 로드맵 ID (rent_goal.goal_id)
    private int monthSeq;        // 거주 N개월차 (1부터)
    private String calendarYm;   // 달력 연월 yyyy-MM
    private double elecKwh;      // 월 전기 사용량 kWh
    private int elecFee;         // 전기요금 원
    private int heatFee;         // 난방비 원
    private int waterFee;        // 수도요금 원
    private int mgmtFee;         // 공용관리비 원
    private int totalFee;        // 합계 원 (전기+난방+수도+관리비)
    private LocalDate rateBaseDt; // 적용 요금표 기준일
}
