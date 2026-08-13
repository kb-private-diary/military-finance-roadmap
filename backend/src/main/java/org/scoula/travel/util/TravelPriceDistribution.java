package org.scoula.travel.util;

import lombok.Builder;
import lombok.Getter;

/**
 * 여행 스타일별 가격 분포를 담는 불변 값 객체.
 */
@Getter
@Builder
public class TravelPriceDistribution {

    private final long savingCost;
    private final long commonCost;
    private final long premiumCost;

    public static TravelPriceDistribution fixed(final long cost) {
        return TravelPriceDistribution.builder()
                .savingCost(cost)
                .commonCost(cost)
                .premiumCost(cost)
                .build();
    }
}
