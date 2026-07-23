package org.scoula.simulator.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.scoula.common.exception.BusinessException;
import org.scoula.common.util.MilitarySavingsCalculator;
import org.scoula.common.util.MilitarySavingsCalculator.CalcResult;

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
            throw BusinessException.notFound("유저 정보를 찾을 수 없습니다.", "SIM_001");
        }

        List<SimulatorSavingAccountDTO> accounts = this.mapper.findAccountListByUserId(userId);
        if (accounts == null || accounts.isEmpty()) {
            throw BusinessException.notFound("시뮬레이션을 위한 군적금 가입 내역을 찾을 수 없습니다.", "SIM_002");
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
            
            CalcResult calc = MilitarySavingsCalculator.calculateAccount(
                    account.getCreatedDate(),
                    monthlySave,
                    dischargeDate,
                    histories,
                    annualInterestRate
            );
            
            if (calc.totalMaturityMonths > maxJoinableMonths) {
                maxJoinableMonths = calc.totalMaturityMonths;
            }
            if (calc.maxCurrentPaidMonths > maxCurrentPaidMonths) {
                maxCurrentPaidMonths = calc.maxCurrentPaidMonths;
            }
            
            currentPaidAmountTotal += calc.pastPrincipal;
            expectedPrincipalTotal += calc.getTotalPrincipal();
            expectedInterestTotal += calc.getTotalInterest();
            
            // 3. 계좌별 매칭지원금 및 최대 한도(복무개월수 * 월납입액) 제한 적용
            long accountMatchingFund = (long) (calc.getTotalPrincipal() * 1.0);
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
            throw BusinessException.badRequest("잘못된 입력값입니다.", "SIM_003");
        }
        
        long amount = request.getMonthlySave();
        
        // 법정 최대 납입 한도(55만원) 검증
        if (amount > 550000) {
            throw BusinessException.badRequest("납입 한도 55만 원을 초과했습니다.", "SIM_003");
        }
        
        int totalMonths = request.getSaveMonths();
        
        // 법정 최대 가입기간(24개월) 검증
        if (totalMonths > 24) {
            throw BusinessException.badRequest("최대 가입기간 24개월을 초과했습니다.", "SIM_003");
        }
        
        long totalPrincipal = 0L;
        double totalInterest = 0.0;
        double annualInterestRate = 0.05;
        double governmentMatchingRate = 1.0;
        
        for (int i = 1; i <= totalMonths; i++) {
            totalPrincipal += amount;
            int investedMonths = totalMonths - i + 1;
            totalInterest += this.calculateSimpleInterest(amount, annualInterestRate, investedMonths);
        }
        
        return this.buildSimulationResponse(totalPrincipal, totalInterest);
    }
    
    @Override
    public SimulatorCalculateResponseDTO calculateVariable(SimulatorVariableCalcRequestDTO request) {
        if (request == null || request.getPeriods() == null || request.getPeriods().isEmpty()) {
            throw BusinessException.badRequest("잘못된 입력값입니다.", "SIM_003");
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
                    throw BusinessException.badRequest("잘못된 입력값입니다.", "SIM_003");
                }
                
                // 법정 최대 납입 한도(55만원) 검증
                if (period.getAmount() > 550000) {
                    throw BusinessException.badRequest("납입 한도 55만 원을 초과했습니다.", "SIM_003");
                }
                
                YearMonth start = YearMonth.parse(period.getStartMonth(), formatter);
                YearMonth end = YearMonth.parse(period.getEndMonth(), formatter);
                
                // 시작일이 종료일보다 늦은 역전 구간 방어
                if (start.isAfter(end)) {
                    throw BusinessException.badRequest("시작일이 종료일보다 늦을 수 없습니다.", "SIM_003");
                }
                
                if (minStart == null || start.isBefore(minStart)) {
                    minStart = start;
                }
                if (maxEnd == null || end.isAfter(maxEnd)) {
                    maxEnd = end;
                }
            }
            
            if (minStart == null || maxEnd == null || minStart.isAfter(maxEnd)) {
                throw BusinessException.badRequest("잘못된 입력값입니다.", "SIM_003");
            }
            
            // 법정 최대 가입기간(24개월) 검증
            long totalDurationMonths = ChronoUnit.MONTHS.between(minStart, maxEnd) + 1;
            if (totalDurationMonths > 24) {
                throw BusinessException.badRequest("최대 가입기간 24개월을 초과했습니다.", "SIM_003");
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
                    totalInterest += this.calculateSimpleInterest(amount, annualInterestRate, investedMonths);
                    current = current.plusMonths(1);
                }
            }
        } catch (java.time.format.DateTimeParseException e) {
            // 날짜 포맷 에러 (예: "2025-1", "가나다라") 발생 시 400 Bad Request 유도
            throw BusinessException.badRequest("날짜 포맷이 올바르지 않습니다.", "SIM_003");
        }
        
        return this.buildSimulationResponse(totalPrincipal, totalInterest);
    }
    
    private double calculateSimpleInterest(long amount, double annualRate, int investedMonths) {
        return amount * annualRate * (investedMonths / 12.0);
    }
    
    private SimulatorCalculateResponseDTO buildSimulationResponse(long totalPrincipal, double totalInterest) {
        double governmentMatchingRate = 1.0;
        double matchingFund = totalPrincipal * governmentMatchingRate;
        long receiptAmount = totalPrincipal + (long) totalInterest + (long) matchingFund;
        
        return SimulatorCalculateResponseDTO.builder()
                .totalPrincipal(totalPrincipal)
                .totalInterest((long) totalInterest)
                .totalMatchingFund((long) matchingFund)
                .totalReceiptAmount(receiptAmount)
                .build();
    }
}
