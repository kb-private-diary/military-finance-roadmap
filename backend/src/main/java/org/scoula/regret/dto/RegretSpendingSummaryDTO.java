package org.scoula.regret.dto;

import lombok.Data;

/**
 * 후회소비 요약 (최근 N개월 월평균)
 * 자취 Step5 정밀 시뮬레이션이 "당신의 소비 패턴" 블록에 쓰려고 조회한다
 */
@Data
public class RegretSpendingSummaryDTO {
    private long avgMonthlySpending; // 최근 N개월 월평균 지출 (총지출 / 개월수)
    private long avgRegretSpending;  // 최근 N개월 월평균 후회소비 (REGRET 태그 총액 / 개월수)
}
