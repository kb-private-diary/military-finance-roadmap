package org.scoula.travel.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 여행 기간과 분기를 계산하는 순수 계산 유틸리티.
 */
public final class TravelDateCalculator {

    private TravelDateCalculator() {
    }

    /**
     * 시작일과 종료일을 모두 포함한 전체 여행 일수를 계산한다.
     */
    public static int calculateTravelDays(
            final LocalDate startDate,
            final LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * 시작일 밤부터 종료일 체크아웃까지의 숙박 일수를 계산한다.
     */
    public static int calculateNights(
            final LocalDate startDate,
            final LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 날짜가 속한 분기(1~4)를 반환한다.
     */
    public static int findQuarter(final LocalDate date) {
        return ((date.getMonthValue() - 1) / 3) + 1;
    }
}
