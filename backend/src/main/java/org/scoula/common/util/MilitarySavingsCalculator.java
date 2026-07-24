package org.scoula.common.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MilitarySavingsCalculator {
    
    public interface SavingHistory {
        Integer getPayRound();
        Long getPayAmount();
        LocalDate getCreatedDate();
    }

    public static class CalcResult {
        public int totalMaturityMonths;
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
            double annualInterestRate) {
        
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
        
        // 2. 만기 개월수 계산 (최대 24개월 제한)
        int totalMaturityMonths = (int) ChronoUnit.MONTHS.between(
                firstPayDate.withDayOfMonth(1), 
                dischargeDate.withDayOfMonth(1)) + 1;
                
        if (totalMaturityMonths <= 0) {
            totalMaturityMonths = 0;
        }
        if (totalMaturityMonths > 24) {
            totalMaturityMonths = 24;
        }
        result.totalMaturityMonths = totalMaturityMonths;
        
        int currentRound = 0;
        
        // 3. 이미 납입한 내역(과거 데이터) 원금 및 이자 계산
        if (histories != null) {
            for (SavingHistory history : histories) {
                if (history.getPayRound() != null && history.getPayRound() > currentRound) {
                    currentRound = history.getPayRound();
                }
                long amount = history.getPayAmount() != null ? history.getPayAmount() : 0L;
                result.pastPrincipal += amount;
                
                int investedMonths;
                if (history.getCreatedDate() != null) {
                    investedMonths = (int) ChronoUnit.MONTHS.between(
                            history.getCreatedDate().withDayOfMonth(1), 
                            dischargeDate.withDayOfMonth(1)) + 1;
                } else {
                    investedMonths = totalMaturityMonths 
                            - (history.getPayRound() != null ? history.getPayRound() : 1) 
                            + 1;
                }
                
                if (investedMonths < 0) {
                    investedMonths = 0;
                }
                result.pastInterest += amount * annualInterestRate * (investedMonths / 12.0);
            }
        }
        result.maxCurrentPaidMonths = currentRound;
        
        // 4. 앞으로 납입할 내역(미래 데이터) 원금 및 이자 계산
        for (int round = currentRound + 1; round <= totalMaturityMonths; round++) {
            result.futurePrincipal += monthlySaveAmt;
            int investedMonths = totalMaturityMonths - round + 1;
            result.futureInterest += monthlySaveAmt * annualInterestRate * (investedMonths / 12.0);
        }
        
        return result;
    }
}
