package org.scoula.rent.dto;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.util.UtilityCalculator;

/**
 * 거주 N개월 중 한 달치 공과금·관리비 breakdown DTO
 * 입주 월부터 순차로 계산한 월별 항목 (총액은 월별 누적)
 */
@Data
@Builder
public class MonthlyUtilityDTO {
    private int monthSeq;      // 거주 N개월차 (1부터)
    private String calendarYm; // 달력 연월 yyyy-MM
    private int month;         // 달력 월 1~12 (계절 판정용)
    private double elecKwh;    // 월 전기 사용량 kWh
    private int elecFee;       // 전기요금 원
    private int heatFee;       // 난방비 원
    private int waterFee;      // 수도요금 원
    private int mgmtFee;       // 공용관리비 원
    private int totalFee;      // 합계 원 (전기+난방+수도+관리비)

    /** UtilityCalculator.Result + 거주회차·연월 → DTO */
    public static MonthlyUtilityDTO of(int monthSeq, String calendarYm, UtilityCalculator.Result r) {
        return MonthlyUtilityDTO.builder()
                .monthSeq(monthSeq)
                .calendarYm(calendarYm)
                .month(r.month)
                .elecKwh(r.kwh)
                .elecFee(r.electric)
                .heatFee(r.heating)
                .waterFee(r.water)
                .mgmtFee(r.mgmt)
                .totalFee(r.total)
                .build();
    }
}
