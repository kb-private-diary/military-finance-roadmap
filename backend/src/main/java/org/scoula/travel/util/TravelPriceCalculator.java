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
        return findPriceDistribution(prices).getCommonCost();
    }

    public static TravelPriceDistribution findPriceDistribution(
            final List<BigDecimal> prices) {
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

        return TravelPriceDistribution.builder()
                .savingCost(findPercentile(sortedPrices, 0.25))
                .commonCost(findPercentile(sortedPrices, 0.50))
                .premiumCost(findPercentile(sortedPrices, 0.75))
                .build();
    }

    private static long findPercentile(
            final List<BigDecimal> sortedPrices,
            final double percentile) {
        if (sortedPrices.size() == 1) {
            return roundToLong(sortedPrices.get(0));
        }

        final double position = (sortedPrices.size() - 1) * percentile;
        final int lowerIndex = (int) Math.floor(position);
        final int upperIndex = (int) Math.ceil(position);
        if (lowerIndex == upperIndex) {
            return roundToLong(sortedPrices.get(lowerIndex));
        }

        final BigDecimal weight = BigDecimal.valueOf(position - lowerIndex);
        final BigDecimal interpolated = sortedPrices.get(lowerIndex)
                .multiply(BigDecimal.ONE.subtract(weight))
                .add(sortedPrices.get(upperIndex).multiply(weight));
        return roundToLong(interpolated);
    }

    private static long roundToLong(final BigDecimal price) {
        return price.setScale(0, RoundingMode.HALF_UP).longValueExact();
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
