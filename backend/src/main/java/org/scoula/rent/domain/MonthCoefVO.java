package org.scoula.rent.domain;

import lombok.Data;

/**
 * 월별 사용량 계수 : month_utility_coef 테이블 한 행을 담는 VO
 * 연평균=1.00, 전기는 8월 최대(냉방)·난방은 2월 최대로 정반대 패턴
 * 순수 데이터 VO (감사컬럼 없음, BaseVO 미상속)
 */
@Data
public class MonthCoefVO {
    private int month;       // 월 1~12
    private double elecCoef; // 전기 월별 계수
    private double heatCoef; // 난방 월별 계수
}
