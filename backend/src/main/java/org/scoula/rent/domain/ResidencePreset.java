package org.scoula.rent.domain;

/**
 * 거주기간 프리셋 → 개월수 매핑
 * 화면은 프리셋(SEMESTER/YEAR/GRADUATE)만 보내고, 개월수(6/12/24)는 이 enum이 정의한다.
 */
public enum ResidencePreset {
    SEMESTER(6),
    YEAR(12),
    GRADUATE(24);

    private final int months;

    ResidencePreset(int months) {
        this.months = months;
    }

    public int getMonths() {
        return this.months;
    }
}
