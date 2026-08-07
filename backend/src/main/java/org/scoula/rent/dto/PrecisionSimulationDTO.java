package org.scoula.rent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Step5 진짜 정밀 시뮬레이션 (킬러 기능)
 * 매달 주거비(월세+관리비+공과금) + 소비 패턴(후회소비 분석) → 만기금으로 자취 가능 개월수
 */
@Data
@Builder
public class PrecisionSimulationDTO {

    private MonthlyHousingCost monthlyHousingCost; // 이 매물 월 주거비
    private UserSpending userSpending;             // 사용자 소비 패턴 (최근 3개월)
    private long totalMonthlyNeed;                 // 진짜 필요한 월 자금 = 주거비 + 월평균지출
    private double possibleMonths;                 // 자취 가능 개월수 = 만기금 / 필요자금
    private long reducedMonthlyNeed;               // 후회소비 절감 시 월 자금
    private double reducedPossibleMonths;          // 절감 시 자취 가능 개월수

    /** 이 매물 월 주거비 breakdown */
    @Data
    @Builder
    @AllArgsConstructor
    public static class MonthlyHousingCost {
        private long rentAndFee; // 월세 + 관리비
        private long utilityFee; // 공과금 (전기+난방+수도)
        private long total;      // 매달 주거비 합
    }

    /** 사용자 소비 패턴 (후회소비 최근 3개월 평균) */
    @Data
    @Builder
    @AllArgsConstructor
    public static class UserSpending {
        private long avgMonthlySpending; // 월 평균 지출
        private long avgRegretSpending;  // 월 평균 후회소비
    }
}
