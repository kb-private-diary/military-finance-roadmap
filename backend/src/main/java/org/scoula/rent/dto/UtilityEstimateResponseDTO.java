package org.scoula.rent.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * 거주 N개월 공과금·관리비 시뮬레이션 응답 DTO (Step5 정밀 시뮬레이션)
 * 입주 월부터 순차 월별 계산 결과와 누적 총액
 * 총액은 월비용 × 개월수가 아니라 월별 누적 (입주 월에 따라 겨울 포함 횟수가 달라 비선형)
 */
@Data
@Builder
public class UtilityEstimateResponseDTO {
    private String sidoName;                 // 시도명 (표기용)
    private double areaSqm;                  // 전용면적 ㎡
    private String startYm;                  // 입주 연월 yyyy-MM
    private int months;                      // 거주 개월수
    private int totalFee;                    // 거주기간 총 공과금·관리비 (월별 누적)
    private int monthlyAvgFee;               // 월평균 (총액 / 개월수, 참고용)
    private Integer heatSample;              // 난방 계수 표본 가구수 (불확실성 표기용)
    private List<MonthlyUtilityDTO> monthly; // 월별 breakdown

    public static UtilityEstimateResponseDTO of(String sidoName, double areaSqm, String startYm,
                                                int months, int totalFee, Integer heatSample,
                                                List<MonthlyUtilityDTO> monthly) {
        return UtilityEstimateResponseDTO.builder()
                .sidoName(sidoName)
                .areaSqm(areaSqm)
                .startYm(startYm)
                .months(months)
                .totalFee(totalFee)
                .monthlyAvgFee(months > 0 ? Math.round((float) totalFee / months) : 0)
                .heatSample(heatSample)
                .monthly(monthly)
                .build();
    }
}
