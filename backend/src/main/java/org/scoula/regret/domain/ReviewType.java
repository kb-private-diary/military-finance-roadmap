package org.scoula.regret.domain;

/**
 * 지출 회고 구분
 * 화면에서 만족/후회 둘 중 하나로 태깅, 잘못된 값 검증에 사용
 */
public enum ReviewType {
    SATISFIED, // 만족한 소비
    REGRET     // 후회하는 소비
}
