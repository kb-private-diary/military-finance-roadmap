package org.scoula.simulator.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.scoula.common.exception.BusinessException;
import org.scoula.common.util.MilitarySavingsCalculator;
import org.scoula.common.util.MilitarySavingsCalculator.CalcResult;
import org.scoula.saving.mapper.MilitarySavingProductMapper;
import org.scoula.saving.util.MilitarySavingRateResolver;
import org.scoula.saving.util.MilitarySavingWithdrawalCalculator;

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
import org.scoula.simulator.dto.SimulatorSavingLossResponseDTO;
import org.scoula.simulator.dto.SimulatorUserDatesDTO;
import org.scoula.simulator.mapper.SimulatorMapper;

@Service
@RequiredArgsConstructor
@Log4j2
public class SimulatorServiceImpl implements SimulatorService {
    // TODO: SimulatorSavingAccountDTO -> openbanking 의 SavingAccountVO 로 교체
    // TODO: SimulatorSavingHistoryDTO -> openbanking 의 SavingHistoryVO 로 교체
    // (교체 후 getter 메서드명이 동일한지 확인 필요 - 예: getOpenDate(), getMonthlySave() 등)
    private final SimulatorMapper mapper;
    private final MilitarySavingProductMapper militarySavingProductMapper;

    // 가입 전 시뮬레이터(calculate/variable)는 아직 은행을 선택하기 전 단계라 특정 계좌가 없다.
    // KB국민(004) 금리를 기준으로 계산한다.
    private static final String SIMULATION_BANK_CODE = "004";

    @Transactional(readOnly = true)
    @Override
    public SimulatorSavingDetailsResponseDTO findSavingDetails(Long userId) {
        SimulatorUserDatesDTO userDates = this.findUserDatesOrThrow(userId);
        // 유저가 가진 군적금 계좌 목록 조회 (SimulatorMapper.findAccountListByUserId, 계좌 여러 개 가능)
        List<SimulatorSavingAccountDTO> accounts = this.findAccountsOrThrow(userId);

        LocalDate dischargeDate = this.resolveDischargeDate(userDates);
        int totalServiceMonths = this.resolveTotalServiceMonths(userDates, dischargeDate);

        long monthlySaveTotal = 0L;
        long currentPaidAmountTotal = 0L;
        int maxCurrentPaidMonths = 0;
        int maxJoinableMonths = 0;

        long expectedPrincipalTotal = 0L;
        double expectedInterestTotal = 0.0;
        long expectedMatchingFundTotal = 0L;

        // 계좌마다 은행(bankCode)이 다를 수 있어 계좌별로 따로 계산한 뒤 아래 total 변수들에 합산한다.
        for (SimulatorSavingAccountDTO account : accounts) {
            long monthlySave = account.getMonthlySave() != null ? account.getMonthlySave() : 0L;
            monthlySaveTotal += monthlySave;

            List<SimulatorSavingHistoryDTO> histories =
                    this.mapper.findHistoryListByAccountId(account.getAccountId());

            MilitarySavingRateResolver rateResolver = new MilitarySavingRateResolver(
                    this.militarySavingProductMapper, account.getBankCode());
            CalcResult calc = MilitarySavingsCalculator.calculateAccount(
                    account.getOpenDate(),
                    monthlySave,
                    dischargeDate,
                    histories,
                    rateResolver
            );

            if (calc.actualTotalMonths > maxJoinableMonths) {
                maxJoinableMonths = calc.actualTotalMonths;
            }
            if (calc.maxCurrentPaidMonths > maxCurrentPaidMonths) {
                maxCurrentPaidMonths = calc.maxCurrentPaidMonths;
            }

            currentPaidAmountTotal += calc.pastPrincipal;
            expectedPrincipalTotal += calc.getTotalPrincipal();
            expectedInterestTotal += calc.getTotalInterest();

            // 3. 계좌별 매칭지원금 및 최대 한도(복무개월수 * 월납입액) 제한 적용
            expectedMatchingFundTotal += this.calculateMatchingFund(
                    calc.getTotalPrincipal(), rateResolver.getGovMatchRate(),
                    totalServiceMonths, monthlySave);
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

    @Transactional(readOnly = true)
    @Override
    public SimulatorSavingLossResponseDTO findSavingLoss(Long userId) {
        SimulatorUserDatesDTO userDates = this.findUserDatesOrThrow(userId);
        // 유저가 가진 군적금 계좌 목록 조회 (SimulatorMapper.findAccountListByUserId, 계좌 여러 개 가능)
        List<SimulatorSavingAccountDTO> accounts = this.findAccountsOrThrow(userId);

        LocalDate dischargeDate = this.resolveDischargeDate(userDates);
        int totalServiceMonths = this.resolveTotalServiceMonths(userDates, dischargeDate);
        LocalDate today = LocalDate.now();

        if (!today.isBefore(dischargeDate)) {
            throw BusinessException.badRequest("이미 전역하여 중도해지 대상이 아닙니다.", "SIMUL_008");
        }

        MilitarySavingWithdrawalCalculator withdrawalCalculator =
                new MilitarySavingWithdrawalCalculator(this.militarySavingProductMapper);

        long totalWithdrawalAmount = 0L;
        long totalMaturityAmount = 0L;

        // 계좌마다 은행(bankCode)이 다를 수 있어 계좌별로 따로 계산한 뒤 아래 total 변수들에 합산한다.
        for (SimulatorSavingAccountDTO account : accounts) {
            long monthlySave = account.getMonthlySave() != null ? account.getMonthlySave() : 0L;

            List<SimulatorSavingHistoryDTO> histories =
                    this.mapper.findHistoryListByAccountId(account.getAccountId());

            MilitarySavingRateResolver rateResolver = new MilitarySavingRateResolver(
                    this.militarySavingProductMapper, account.getBankCode());
            CalcResult calc = MilitarySavingsCalculator.calculateAccount(
                    account.getOpenDate(),
                    monthlySave,
                    dischargeDate,
                    histories,
                    rateResolver
            );

            long accountMatchingFund = this.calculateMatchingFund(
                    calc.getTotalPrincipal(), rateResolver.getGovMatchRate(),
                    totalServiceMonths, monthlySave);
            totalMaturityAmount +=
                    calc.getTotalPrincipal() + (long) calc.getTotalInterest() + accountMatchingFund;

            // 중도해지는 정부매칭지원금 없음. 이미 낸 회차(과거 이력)만 대상.
            BigDecimal basicRate = rateResolver.getBasicRate();
            totalWithdrawalAmount += withdrawalCalculator.calculateWithdrawalAmount(
                    account.getBankCode(),
                    basicRate,
                    calc.totalMaturityMonths,
                    calc.firstPayDate,
                    calc.maturityDate,
                    histories,
                    today
            );
        }

        long lossAmount = totalMaturityAmount - totalWithdrawalAmount;

        return new SimulatorSavingLossResponseDTO(totalWithdrawalAmount, lossAmount);
    }

    @Override
    public SimulatorCalculateResponseDTO calculateConstant(SimulatorConstantCalcRequestDTO request) {
        this.validateConstantRequest(request);
        
        long amount = request.getMonthlySave();
        int totalMonths = request.getSaveMonths();

        MilitarySavingRateResolver rateResolver = new MilitarySavingRateResolver(
                this.militarySavingProductMapper, SIMULATION_BANK_CODE);
        double annualInterestRate = rateResolver.apply(totalMonths);

        long totalPrincipal = 0L;
        double totalInterest = 0.0;

        for (int i = 1; i <= totalMonths; i++) {
            totalPrincipal += amount;
            int investedMonths = totalMonths - i + 1;
            totalInterest += this.calculateSimpleInterest(amount, annualInterestRate, investedMonths);
        }

        return this.buildSimulationResponse(
                totalPrincipal, totalInterest, rateResolver.getGovMatchRate());
    }
    
    @Override
    public SimulatorCalculateResponseDTO calculateVariable(SimulatorVariableCalcRequestDTO request) {
        this.validateVariableRequest(request);

        int minStart = Integer.MAX_VALUE;
        int maxEnd = Integer.MIN_VALUE;
        for (SimulatorVariableCalcRequestDTO.Period period : request.getPeriods()) {
            minStart = Math.min(minStart, period.getStartMonthOffset());
            maxEnd = Math.max(maxEnd, period.getEndMonthOffset());
        }

        long totalDurationMonths = maxEnd - minStart + 1;
        this.validateTotalMonths(totalDurationMonths);

        MilitarySavingRateResolver rateResolver = new MilitarySavingRateResolver(
                this.militarySavingProductMapper, SIMULATION_BANK_CODE);
        double annualInterestRate = rateResolver.apply((int) totalDurationMonths);

        long totalPrincipal = 0L;
        double totalInterest = 0.0;

        for (SimulatorVariableCalcRequestDTO.Period period : request.getPeriods()) {
            long amount = period.getAmount();
            int startMonth = period.getStartMonthOffset();
            int endMonth = period.getEndMonthOffset();
            for (int month = startMonth; month <= endMonth; month++) {
                totalPrincipal += amount;
                int investedMonths = maxEnd - month + 1;
                totalInterest += this.calculateSimpleInterest(
                        amount, annualInterestRate, investedMonths);
            }
        }

        return this.buildSimulationResponse(
                totalPrincipal, totalInterest, rateResolver.getGovMatchRate());
    }

    // ------------------------- 유효성 검증 헬퍼 -------------------------

    private void validateConstantRequest(SimulatorConstantCalcRequestDTO request) {
        if (request == null || request.getMonthlySave() == null || request.getSaveMonths() == null) {
            throw BusinessException.badRequest("잘못된 입력값입니다.", "SIMUL_003");
        }
        this.validateSaveAmount(request.getMonthlySave());
        this.validateTotalMonths(request.getSaveMonths());
    }
    
    private void validateVariableRequest(SimulatorVariableCalcRequestDTO request) {
        if (request == null || request.getPeriods() == null || request.getPeriods().isEmpty()) {
            throw BusinessException.badRequest("잘못된 입력값입니다.", "SIMUL_003");
        }
        for (SimulatorVariableCalcRequestDTO.Period period : request.getPeriods()) {
            if (period.getStartMonthOffset() == null || period.getEndMonthOffset() == null
                    || period.getAmount() == null) {
                throw BusinessException.badRequest("잘못된 입력값입니다.", "SIMUL_003");
            }
            if (period.getStartMonthOffset() < 1) {
                throw BusinessException.badRequest("가입 개월차는 1 이상이어야 합니다.", "SIMUL_009");
            }
            this.validatePeriodOrder(period.getStartMonthOffset(), period.getEndMonthOffset());
            this.validateSaveAmount(period.getAmount());
        }
        this.validateNoOverlap(request.getPeriods());
    }
    
    private void validateSaveAmount(long amount) {
        if (amount > 550000) {
            throw BusinessException.badRequest("납입 한도 55만 원을 초과했습니다.", "SIMUL_004");
        }
    }
    
    private void validateTotalMonths(long totalMonths) {
        if (totalMonths > 24) {
            throw BusinessException.badRequest("최대 가입기간 24개월을 초과했습니다.", "SIMUL_005");
        }
    }
    
    private void validatePeriodOrder(int startMonthOffset, int endMonthOffset) {
        if (startMonthOffset > endMonthOffset) {
            throw BusinessException.badRequest("시작월차가 종료월차보다 늦을 수 없습니다.", "SIMUL_006");
        }
    }

    // 구간끼리 겹치면 해당 개월이 이중으로 계산되므로 겹침을 금지한다.
    private void validateNoOverlap(List<SimulatorVariableCalcRequestDTO.Period> periods) {
        List<SimulatorVariableCalcRequestDTO.Period> sorted = new ArrayList<>(periods);
        sorted.sort(Comparator.comparing(
                SimulatorVariableCalcRequestDTO.Period::getStartMonthOffset));

        for (int i = 1; i < sorted.size(); i++) {
            int prevEnd = sorted.get(i - 1).getEndMonthOffset();
            int currentStart = sorted.get(i).getStartMonthOffset();
            if (currentStart <= prevEnd) {
                throw BusinessException.badRequest("납입 구간이 겹칠 수 없습니다.", "SIMUL_010");
            }
        }
    }
    
    // ------------------------- 조회 헬퍼 -------------------------

    private SimulatorUserDatesDTO findUserDatesOrThrow(Long userId) {
        SimulatorUserDatesDTO userDates = this.mapper.findUserDates(userId);
        if (userDates == null) {
            throw BusinessException.notFound("유저 정보를 찾을 수 없습니다.", "SIMUL_001");
        }
        return userDates;
    }

    private List<SimulatorSavingAccountDTO> findAccountsOrThrow(Long userId) {
        List<SimulatorSavingAccountDTO> accounts = this.mapper.findAccountListByUserId(userId);
        if (accounts == null || accounts.isEmpty()) {
            throw BusinessException.notFound("시뮬레이션을 위한 군적금 가입 내역을 찾을 수 없습니다.", "SIMUL_002");
        }
        return accounts;
    }

    // ------------------------- 계산 헬퍼 -------------------------

    private double calculateSimpleInterest(long amount, double annualRate, int investedMonths) {
        return amount * annualRate * (investedMonths / 12.0);
    }

    private LocalDate resolveDischargeDate(SimulatorUserDatesDTO userDates) {
        return userDates.getDischargeDate() != null
                ? userDates.getDischargeDate()
                : LocalDate.now().plusMonths(18);
    }

    // 복무개월수 계산 (매칭지원금 최대 한도 용도)
    private int resolveTotalServiceMonths(
            SimulatorUserDatesDTO userDates, LocalDate dischargeDate) {
        LocalDate enlistDate =
                userDates.getEnlistDate() != null ? userDates.getEnlistDate() : LocalDate.now();
        int totalServiceMonths = (int) ChronoUnit.MONTHS.between(
                enlistDate.withDayOfMonth(1), dischargeDate.withDayOfMonth(1));
        return totalServiceMonths > 0 ? totalServiceMonths : 1;
    }

    // 계좌별 정부매칭지원금 (복무개월수 * 월납입액 한도 적용)
    private long calculateMatchingFund(
            long totalPrincipal, double govMatchRate, int totalServiceMonths, long monthlySave) {
        long matchingFund = (long) (totalPrincipal * govMatchRate);
        long maxMatchingFund = (long) totalServiceMonths * monthlySave;
        return Math.min(matchingFund, maxMatchingFund);
    }

    private SimulatorCalculateResponseDTO buildSimulationResponse(
            long totalPrincipal, double totalInterest, double governmentMatchingRate) {
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
