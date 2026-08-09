package org.scoula.saving.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.scoula.common.util.MilitarySavingsCalculator.SavingHistory;
import org.scoula.saving.dto.MilitarySavingWithdrawalRateDTO;
import org.scoula.saving.mapper.MilitarySavingProductMapper;

// 이미 납입한 회차(과거 이력)를 오늘 중도해지했을 때 받는 금액(원금+중도해지이자) 계산.
// 미래 회차는 애초에 납입되지 않았으므로 대상이 아니다. 정부매칭지원금도 중도해지 시엔 지급되지 않는다.
public class MilitarySavingWithdrawalCalculator {

    private static final double PERCENT_DIVISOR = 100.0;
    private static final double DAYS_IN_YEAR = 365.0;
    // 소수점 절사 자릿수. 100(=10^2)은 "소수 2자리까지 남긴다" = 그 다음 자리(셋째자리)부터 절사된다는 뜻.
    private static final double RATE_TRUNCATE_SCALE = 100.0;

    private final MilitarySavingProductMapper mapper;

    public MilitarySavingWithdrawalCalculator(MilitarySavingProductMapper mapper) {
        this.mapper = mapper;
    }

    public long calculateWithdrawalAmount(
            String bankCode,
            BigDecimal basicRate,
            int contractMonths,
            LocalDate firstPayDate,
            LocalDate maturityDate,
            List<? extends SavingHistory> histories,
            LocalDate today) {

        long contractDays = ChronoUnit.DAYS.between(firstPayDate, maturityDate);

        long totalAmount = 0L;
        double totalInterest = 0.0;

        if (histories != null) {
            for (SavingHistory history : histories) {
                long amount = history.getPayAmount() != null ? history.getPayAmount() : 0L;
                LocalDate payDate =
                        history.getPaidDate() != null ? history.getPaidDate() : firstPayDate;

                totalAmount += amount;

                double withdrawalRate = this.resolveWithdrawalRate(
                        bankCode, basicRate, contractMonths, contractDays, payDate, today);

                long investedDays = ChronoUnit.DAYS.between(payDate, today);
                if (investedDays < 0) {
                    investedDays = 0;
                }
                totalInterest +=
                        amount * (withdrawalRate / PERCENT_DIVISOR) * (investedDays / DAYS_IN_YEAR);
            }
        }

        return totalAmount + (long) totalInterest;
    }

    // 회차 하나의 중도해지금리(연 %) 계산
    private double resolveWithdrawalRate(
            String bankCode,
            BigDecimal basicRate,
            int contractMonths,
            long contractDays,
            LocalDate payDate,
            LocalDate today) {

        int elapsedMonths = (int) ChronoUnit.MONTHS.between(payDate, today);
        if (elapsedMonths < 0) {
            elapsedMonths = 0;
        }
        elapsedMonths = Math.min(elapsedMonths, contractMonths);

        long elapsedDays = ChronoUnit.DAYS.between(payDate, today);
        if (elapsedDays < 0) {
            elapsedDays = 0;
        }
        BigDecimal elapsedRatio = contractDays > 0
                ? BigDecimal.valueOf(elapsedDays * PERCENT_DIVISOR / contractDays)
                : BigDecimal.ZERO;

        MilitarySavingWithdrawalRateDTO tier =
                this.mapper.findWithdrawalRate(bankCode, elapsedMonths, elapsedRatio);
        if (tier == null) {
            return 0.0;
        }
        // rate_ratio가 없는 구간(경과 1개월 미만)은 floor_rate를 그대로 적용
        if (tier.getRateRatio() == null) {
            return tier.getFloorRate() != null ? tier.getFloorRate().doubleValue() : 0.0;
        }

        double appliedRatio = tier.getRateRatio().doubleValue() / PERCENT_DIVISOR;
        double rawRate;
        if ("MONTH".equals(tier.getValueUnit())) {
            double monthProgress =
                    contractMonths > 0 ? (elapsedMonths / (double) contractMonths) : 0.0;
            rawRate = basicRate.doubleValue() * appliedRatio * monthProgress;
        } else {
            rawRate = basicRate.doubleValue() * appliedRatio;
        }

        double floorRate = tier.getFloorRate() != null ? tier.getFloorRate().doubleValue() : 0.0;
        double rate = Math.max(rawRate, floorRate);

        // ×100 → floor → ÷100 = 소수 셋째자리에서 절사(둘째자리까지 표시). 예: 1.359 → 135.9 → 135 → 1.35
        return Math.floor(rate * RATE_TRUNCATE_SCALE) / RATE_TRUNCATE_SCALE;
    }
}
