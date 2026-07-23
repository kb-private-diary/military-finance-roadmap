package org.scoula.simulator.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: 향후 openbanking 패키지 완성 시 아래 2개 DTO는 삭제하고 공식 VO로 교체
import org.scoula.simulator.dto.SimulatorCalculateResponseDTO;
import org.scoula.simulator.dto.SimulatorConstantCalcRequestDTO;
import org.scoula.simulator.dto.SimulatorVariableCalcRequestDTO;
import org.scoula.simulator.dto.SimulatorSavingAccountDTO;
import org.scoula.simulator.dto.SimulatorSavingDetailsResponseDTO;
import org.scoula.simulator.dto.SimulatorSavingHistoryDTO;
import org.scoula.simulator.dto.SimulatorUserDatesDTO;
import org.scoula.simulator.mapper.SimulatorMapper;

@Service
@RequiredArgsConstructor
@Log4j2
public class SimulatorServiceImpl implements SimulatorService {
    // TODO: SimulatorSavingAccountDTO -> openbanking 의 SavingAccountVO 로 교체
    // TODO: SimulatorSavingHistoryDTO -> openbanking 의 SavingHistoryVO 로 교체
    // (교체 후 getter 메서드명이 동일한지 확인 필요 - 예: getCreatedDate(), getMonthlySave() 등)
    private final SimulatorMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public SimulatorSavingDetailsResponseDTO findSavingDetails(Long userId) {
        SimulatorUserDatesDTO userDates = this.mapper.findUserDates(userId);
        if (userDates == null) {
            return null;
        }

        List<SimulatorSavingAccountDTO> accounts = this.mapper.findAccountListByUserId(userId);
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }

        LocalDate enlistDate = userDates.getEnlistDate() != null ? userDates.getEnlistDate() : LocalDate.now();
        LocalDate dischargeDate = userDates.getDischargeDate() != null 
                ? userDates.getDischargeDate() 
                : LocalDate.now().plusMonths(18);
        
        // 1. 복무개월수 계산 (매칭지원금 최대 한도 용도)
        int totalServiceMonths = (int) ChronoUnit.MONTHS.between(enlistDate.withDayOfMonth(1), dischargeDate.withDayOfMonth(1));
        if (totalServiceMonths <= 0) totalServiceMonths = 1;

        long monthlySaveTotal = 0L;
        long currentPaidAmountTotal = 0L;
        int maxCurrentPaidMonths = 0;
        int maxJoinableMonths = 0;
        
        long expectedPrincipalTotal = 0L;
        double expectedInterestTotal = 0.0;
        long expectedMatchingFundTotal = 0L;

        double annualInterestRate = 0.05; // 5%
        
        for (SimulatorSavingAccountDTO account : accounts) {
            long monthlySave = account.getMonthlySave() != null ? account.getMonthlySave() : 0L;
            monthlySaveTotal += monthlySave;
            
            List<SimulatorSavingHistoryDTO> histories = 
                    this.mapper.findHistoryListByAccountId(account.getAccountId());
            
            // 2. 가입 인정 개월수 (시작 달 포함 + 1)
            // 실제 첫 입금일(납입회차 1회)을 기준으로 계산
            LocalDate firstPayDate = account.getCreatedDate() != null 
                    ? account.getCreatedDate() 
                    : LocalDate.now();
            
            if (histories != null && !histories.isEmpty()) {
                for (SimulatorSavingHistoryDTO history : histories) {
                    if (history.getPayRound() != null 
                            && history.getPayRound() == 1 
                            && history.getCreatedDate() != null) {
                        firstPayDate = history.getCreatedDate();
                        break;
                    }
                }
            } else {
                // 납입 이력이 전혀 없는 경우 (스킵된 과거를 보상받을 수 없으므로)
                // 계좌 개설일이 과거라도 가장 빠른 첫 납입 가능일은 '오늘(이번 달)'이 됩니다.
                if (firstPayDate.isBefore(LocalDate.now())) {
                    firstPayDate = LocalDate.now();
                }
            }
            
            int totalMaturityMonths = (int) ChronoUnit.MONTHS.between(
                    firstPayDate.withDayOfMonth(1), 
                    dischargeDate.withDayOfMonth(1)) + 1;
            if (totalMaturityMonths <= 0) {
                totalMaturityMonths = 0;
            }
            if (totalMaturityMonths > 24) {
                totalMaturityMonths = 24;
            }
            
            if (totalMaturityMonths > maxJoinableMonths) {
                maxJoinableMonths = totalMaturityMonths;
            }
            
            int currentRound = 0;
            long pastPrincipal = 0L;
            double pastInterest = 0.0;
            
            if (histories != null) {
                for (SimulatorSavingHistoryDTO history : histories) {
                    if (history.getPayRound() != null && history.getPayRound() > currentRound) {
                        currentRound = history.getPayRound();
                    }
                    long amount = history.getPayAmount() != null 
                            ? history.getPayAmount() 
                            : 0L;
                    pastPrincipal += amount;
                    
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
                    pastInterest += amount * annualInterestRate * (investedMonths / 12.0);
                }
            }
            
            currentPaidAmountTotal += pastPrincipal;
            if (currentRound > maxCurrentPaidMonths) {
                maxCurrentPaidMonths = currentRound;
            }
            
            long futurePrincipal = 0L;
            double futureInterest = 0.0;
            for (int round = currentRound + 1; round <= totalMaturityMonths; round++) {
                futurePrincipal += monthlySave;
                int investedMonths = totalMaturityMonths - round + 1;
                futureInterest += monthlySave * annualInterestRate * (investedMonths / 12.0);
            }
            
            long accountTotalPrincipal = pastPrincipal + futurePrincipal;
            expectedPrincipalTotal += accountTotalPrincipal;
            expectedInterestTotal += (pastInterest + futureInterest);
            
            // 3. 계좌별 매칭지원금 및 최대 한도(복무개월수 * 월납입액) 제한 적용
            long accountMatchingFund = (long) (accountTotalPrincipal * 1.0);
            long maxAccountMatchingFund = (long) totalServiceMonths * monthlySave;
            if (accountMatchingFund > maxAccountMatchingFund) {
                accountMatchingFund = maxAccountMatchingFund;
            }
            expectedMatchingFundTotal += accountMatchingFund;
        }
        
        long totalReceiptAmount = expectedPrincipalTotal + (long) expectedInterestTotal + expectedMatchingFundTotal;
        
        return SimulatorSavingDetailsResponseDTO.builder()
                .monthlySaveTotal(monthlySaveTotal)
                .joinableMonths(maxJoinableMonths)
                .currentPaidAmount(currentPaidAmountTotal)
                .currentPaidMonths(maxCurrentPaidMonths)
                .expectedPrincipal(expectedPrincipalTotal)
                .expectedInterest((long) expectedInterestTotal)
                .expectedMatchingFund(expectedMatchingFundTotal)
                .totalReceiptAmount(totalReceiptAmount)
                .build();
    }
    
    @Override
    public SimulatorCalculateResponseDTO calculateConstant(SimulatorConstantCalcRequestDTO request) {
        if (request == null || request.getMonthlySave() == null || request.getSaveMonths() == null) {
            return null;
        }
        
        long amount = request.getMonthlySave();
        
        // 법정 최대 납입 한도(55만원) 검증
        if (amount > 550000) {
            return null;
        }
        
        int totalMonths = request.getSaveMonths();
        
        // 법정 최대 가입기간(24개월) 검증
        if (totalMonths > 24) {
            return null;
        }
        
        long totalPrincipal = 0L;
        double totalInterest = 0.0;
        double annualInterestRate = 0.05;
        double governmentMatchingRate = 1.0;
        
        for (int i = 1; i <= totalMonths; i++) {
            totalPrincipal += amount;
            int investedMonths = totalMonths - i + 1;
            totalInterest += amount * annualInterestRate * (investedMonths / 12.0);
        }
        
        double matchingFund = totalPrincipal * governmentMatchingRate;
        long receiptAmount = totalPrincipal + (long) totalInterest + (long) matchingFund;
        
        return SimulatorCalculateResponseDTO.builder()
                .totalPrincipal(totalPrincipal)
                .totalInterest((long) totalInterest)
                .totalMatchingFund((long) matchingFund)
                .totalReceiptAmount(receiptAmount)
                .build();
    }
    
    @Override
    public SimulatorCalculateResponseDTO calculateVariable(SimulatorVariableCalcRequestDTO request) {
        if (request == null || request.getPeriods() == null || request.getPeriods().isEmpty()) {
            return null;
        }
        
        long totalPrincipal = 0L;
        double totalInterest = 0.0;
        double annualInterestRate = 0.05;
        double governmentMatchingRate = 1.0;
        
        YearMonth minStart = null;
        YearMonth maxEnd = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        try {
            // 1. 모든 구간 유효성 검사 및 최소 시작일/최대 종료일 계산
            for (SimulatorVariableCalcRequestDTO.Period period : request.getPeriods()) {
                if (period.getStartMonth() == null || period.getEndMonth() == null || period.getAmount() == null) {
                    return null;
                }
                
                // 법정 최대 납입 한도(55만원) 검증
                if (period.getAmount() > 550000) {
                    return null;
                }
                
                YearMonth start = YearMonth.parse(period.getStartMonth(), formatter);
                YearMonth end = YearMonth.parse(period.getEndMonth(), formatter);
                
                // 시작일이 종료일보다 늦은 역전 구간 방어
                if (start.isAfter(end)) {
                    return null;
                }
                
                if (minStart == null || start.isBefore(minStart)) {
                    minStart = start;
                }
                if (maxEnd == null || end.isAfter(maxEnd)) {
                    maxEnd = end;
                }
            }
            
            if (minStart == null || maxEnd == null || minStart.isAfter(maxEnd)) {
                return null;
            }
            
            // 법정 최대 가입기간(24개월) 검증
            long totalDurationMonths = ChronoUnit.MONTHS.between(minStart, maxEnd) + 1;
            if (totalDurationMonths > 24) {
                return null;
            }
            
            // 2. 각 구간별 이자 계산
            for (SimulatorVariableCalcRequestDTO.Period period : request.getPeriods()) {
                YearMonth start = YearMonth.parse(period.getStartMonth(), formatter);
                YearMonth end = YearMonth.parse(period.getEndMonth(), formatter);
                long amount = period.getAmount();
                
                YearMonth current = start;
                while (!current.isAfter(end)) {
                    totalPrincipal += amount;
                    int investedMonths = (int) ChronoUnit.MONTHS.between(current, maxEnd) + 1;
                    totalInterest += amount * annualInterestRate * (investedMonths / 12.0);
                    current = current.plusMonths(1);
                }
            }
        } catch (java.time.format.DateTimeParseException e) {
            // 날짜 포맷 에러 (예: "2025-1", "가나다라") 발생 시 400 Bad Request 유도
            return null;
        }
        
        // 매칭지원금 계산 (기본 100% 매칭, 1회성 계산기이므로 한도는 입력된 원금 자체를 상한선으로 봄)
        double matchingFund = totalPrincipal * governmentMatchingRate;
        
        // 최종 비과세 적용 후 반환
        long receiptAmount = totalPrincipal + (long) totalInterest + (long) matchingFund;
        
        return SimulatorCalculateResponseDTO.builder()
                .totalPrincipal(totalPrincipal)
                .totalInterest((long) totalInterest)
                .totalMatchingFund((long) matchingFund)
                .totalReceiptAmount(receiptAmount)
                .build();
    }
}
