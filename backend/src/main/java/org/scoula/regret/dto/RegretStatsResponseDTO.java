package org.scoula.regret.dto;

import java.util.List;

import lombok.Data;

/**
 * 월별 후회소비 통계 응답 DTO
 * 만족/후회 금액·건수 + 카테고리별 후회 순위
 */
@Data
public class RegretStatsResponseDTO {
    private Long totalSpending;     // 총 지출 금액
    private Long regretAmount;      // 후회 지출 합계
    private Long sosoAmount;        // 애매 지출 합계
    private Long satisfiedAmount;   // 만족 지출 합계
    private Integer regretCount;    // 후회 건수
    private Integer sosoCount;      // 애매 건수
    private Integer satisfiedCount; // 만족 건수
    private Integer untaggedCount;  // 아직 태깅 안 한 건수
    private List<CategoryAmountDTO> categoryRegrets; // 카테고리별 후회 (금액 내림차순)
    private Long monthlyIncome;                      // 해당 월 총 수입 (income 합계)
    private Double regretRatio;                      // 수입 대비 후회소비 비율 (%) = 후회금액 / 월수입 × 100
}
