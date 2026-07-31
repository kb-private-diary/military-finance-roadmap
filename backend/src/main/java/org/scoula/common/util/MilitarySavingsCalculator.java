package org.scoula.common.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.IntFunction;

public class MilitarySavingsCalculator {

    private static final int MAX_JOIN_MONTHS = 24;
    private static final double DAYS_IN_YEAR = 365.0;

    public interface SavingHistory {
        Integer getPayRound();
        Long getPayAmount();
        LocalDate getCreatedDate();
    }

    public static class CalcResult {
        // 금리구간 판정용(너그럽게 반올림된 총 가입기간). 실제 납입 회차수와는 다를 수 있다.
        public int totalMaturityMonths;
        // 실제로 납입이 일어나는 마지막 회차(자동이체 예정일이 만기일을 넘기면 그 이전 회차까지만).
        public int actualTotalMonths;
        public int maxCurrentPaidMonths;
        public long pastPrincipal;
        public double pastInterest;
        public long futurePrincipal;
        public double futureInterest;

        public long getTotalPrincipal() {
            return pastPrincipal + futurePrincipal;
        }

        public double getTotalInterest() {
            return pastInterest + futureInterest;
        }
    }

    public static CalcResult calculateAccount(
            LocalDate accountCreatedDate,
            Long monthlySave,
            LocalDate dischargeDate,
            List<? extends SavingHistory> histories,
            IntFunction<Double> annualInterestRateResolver) {

        CalcResult result = new CalcResult();
        long monthlySaveAmt = monthlySave != null ? monthlySave : 0L;

        // 1. 실제 첫 입금일(납입회차 1회)을 기준으로 시작일 계산
        LocalDate firstPayDate = accountCreatedDate != null ? accountCreatedDate : LocalDate.now();

        if (histories != null && !histories.isEmpty()) {
            for (SavingHistory history : histories) {
                if (history.getPayRound() != null && history.getPayRound() == 1 && history.getCreatedDate() != null) {
                    firstPayDate = history.getCreatedDate();
                    break;
                }
            }
        } else {
            // 납입 이력이 전혀 없는 경우, 가장 빠른 납입 가능일은 '오늘'
            if (firstPayDate.isBefore(LocalDate.now())) {
                firstPayDate = LocalDate.now();
            }
        }

        // 2. 만기 개월수 계산 (최대 24개월 제한) — 납입회차 수·금리구간 판정은 월 단위로 유지
        int rawMaturityMonths = (int) ChronoUnit.MONTHS.between(
                firstPayDate.withDayOfMonth(1),
                dischargeDate.withDayOfMonth(1)) + 1;

        if (rawMaturityMonths < 0) {
            rawMaturityMonths = 0;
        }
        int totalMaturityMonths = Math.min(rawMaturityMonths, MAX_JOIN_MONTHS);
        result.totalMaturityMonths = totalMaturityMonths;

        // 이자 계산은 실제 만기일 기준 예치일수(/365)로 한다(은행 상품설명서 산식과 동일).
        // 최대 가입기간 캡에 걸린 경우(실제 복무기간이 상품 최대 가입기간을 초과)엔 상품 자체 만기(첫 납입일+최대 가입개월)를,
        // 그 외엔 실제 전역일을 만기일로 본다.
        LocalDate maturityDate = rawMaturityMonths > MAX_JOIN_MONTHS
                ? firstPayDate.plusMonths(MAX_JOIN_MONTHS)
                : dischargeDate;

        // 총 가입기간(totalMaturityMonths) 기준으로 계좌 전체에 적용될 금리를 한 번만 결정한다
        // (회차별 경과개월이 아니라, 계좌가 통째로 속하는 구간 하나의 금리를 전 회차에 동일 적용).
        double annualInterestRate = annualInterestRateResolver.apply(totalMaturityMonths);

        int currentRound = 0;

        // 3. 이미 납입한 내역(과거 데이터) 원금 및 이자 계산
        // 이자 = 입금액 × 약정이율 × 예치일수(입금일~만기일 전일)/365
        if (histories != null) {
            for (SavingHistory history : histories) {
                if (history.getPayRound() != null && history.getPayRound() > currentRound) {
                    currentRound = history.getPayRound();
                }
                long amount = history.getPayAmount() != null ? history.getPayAmount() : 0L;
                result.pastPrincipal += amount;

                LocalDate payDate = history.getCreatedDate() != null
                        ? history.getCreatedDate()
                        : firstPayDate.plusMonths(
                                (history.getPayRound() != null ? history.getPayRound() : 1) - 1);

                long investedDays = ChronoUnit.DAYS.between(payDate, maturityDate);
                if (investedDays < 0) {
                    investedDays = 0;
                }
                result.pastInterest += amount * annualInterestRate * (investedDays / DAYS_IN_YEAR);
            }
        }
        result.maxCurrentPaidMonths = currentRound;

        // 4. 앞으로 납입할 내역(미래 데이터) 원금 및 이자 계산
        // 자동이체 예정일이 만기일 이후면 이미 계좌가 만기된 뒤라 그 회차부터는 납입 자체가 없다.
        int actualTotalMonths = currentRound;
        for (int round = currentRound + 1; round <= totalMaturityMonths; round++) {
            LocalDate payDate = firstPayDate.plusMonths(round - 1);
            if (!payDate.isBefore(maturityDate)) {
                break;
            }

            result.futurePrincipal += monthlySaveAmt;
            long investedDays = ChronoUnit.DAYS.between(payDate, maturityDate);
            result.futureInterest +=
                    monthlySaveAmt * annualInterestRate * (investedDays / DAYS_IN_YEAR);
            actualTotalMonths = round;
        }
        result.actualTotalMonths = actualTotalMonths;

        return result;
    }
}
