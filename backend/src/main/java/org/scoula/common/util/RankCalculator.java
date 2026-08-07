package org.scoula.common.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

// 계급 진급 개월차 계산기. 입대일이 속한 달을 며칠 복무했든 1개월째로 인정한다
// (예: 1/28 입대해도 1월은 꽉 채운 1개월째로 계산 - 진급일은 입대일자와 무관하게 매달 1일).
public final class RankCalculator {

    private RankCalculator() {
    }

    public static int monthsSinceEnlist(LocalDate enlistDate, LocalDate referenceDate) {
        long diff = ChronoUnit.MONTHS.between(
                YearMonth.from(enlistDate), YearMonth.from(referenceDate));
        return (int) diff + 1;
    }
}
