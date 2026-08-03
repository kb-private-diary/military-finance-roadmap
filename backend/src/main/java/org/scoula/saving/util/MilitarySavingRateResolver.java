package org.scoula.saving.util;

import java.math.BigDecimal;
import java.util.function.IntFunction;

import org.scoula.saving.dto.MilitarySavingRateDTO;
import org.scoula.saving.mapper.MilitarySavingProductMapper;

// MilitarySavingsCalculator.calculateAccount() 에 그대로 넘길 수 있는 금리 리졸버.
// totalMaturityMonths 확정 시점에 딱 한 번 조회되며, 그 결과(기본금리·정부매칭비율)를
// 매칭지원금 계산에도 재사용할 수 있도록 getGovMatchRate() 로 노출한다.
public class MilitarySavingRateResolver implements IntFunction<Double> {

    private static final double PERCENT_DIVISOR = 100.0;

    private final MilitarySavingProductMapper mapper;
    private final String bankCode;
    private MilitarySavingRateDTO resolved;

    public MilitarySavingRateResolver(MilitarySavingProductMapper mapper, String bankCode) {
        this.mapper = mapper;
        this.bankCode = bankCode;
    }

    @Override
    public Double apply(int totalMaturityMonths) {
        this.resolved = this.mapper.findMaturityRate(this.bankCode, totalMaturityMonths);
        return this.resolved != null
                ? this.resolved.getBasicRate().doubleValue() / PERCENT_DIVISOR
                : 0.0;
    }

    public double getGovMatchRate() {
        return this.resolved != null && this.resolved.getGovMatchRate() != null
                ? this.resolved.getGovMatchRate().doubleValue() / PERCENT_DIVISOR
                : 0.0;
    }

    // 중도해지금리 계산("기본이율 × ...")처럼 원본 퍼센트 값 그대로가 필요한 곳에서 사용
    public BigDecimal getBasicRate() {
        return this.resolved != null ? this.resolved.getBasicRate() : BigDecimal.ZERO;
    }
}
