package org.scoula.travel.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 외부 가격 목록에서 대표 가격을 계산한다.
 */
public final class TravelPriceCalculator {

    private TravelPriceCalculator() {
    }

    public static long findMedian(final List<BigDecimal> prices) {
        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("가격 목록이 비어 있습니다.");
        }

        final List<BigDecimal> sortedPrices = prices.stream()
                .filter(price -> price != null && price.signum() > 0)
                .sorted()
                .toList();
        if (sortedPrices.isEmpty()) {
            throw new IllegalArgumentException("유효한 가격이 없습니다.");
        }

        final int middle = sortedPrices.size() / 2;
        final BigDecimal median;
        if (sortedPrices.size() % 2 == 0) {
            median = sortedPrices.get(middle - 1)
                    .add(sortedPrices.get(middle))
                    .divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
        } else {
            median = sortedPrices.get(middle);
        }
        return median.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public static long applyRate(
            final long commonCost,
            final BigDecimal rate) {
        return BigDecimal.valueOf(commonCost)
                .multiply(rate)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
