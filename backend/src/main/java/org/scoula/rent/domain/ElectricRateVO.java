package org.scoula.rent.domain;

import lombok.Data;

/**
 * 전기 누진 요금표 : electric_rate 테이블 한 행을 담는 VO
 * 전국 단일 요금제, SUMMER(7~8월)는 누진 구간 완화
 * 서버 계산은 UtilityCalculator 상수를 쓰고, 이 요금표는 프론트 캐싱·문서화용
 * 순수 데이터 VO (감사컬럼 없음, BaseVO 미상속)
 */
@Data
public class ElectricRateVO {
    private String season;    // SUMMER(7~8월) / NORMAL
    private int tier;         // 누진 구간 1~3
    private int kwhFrom;      // 구간 시작 kWh
    private Integer kwhTo;    // 구간 끝 kWh (NULL=무제한)
    private int baseFee;      // 기본요금 원
    private double unitPrice; // 전력량 요금 원/kWh
}
