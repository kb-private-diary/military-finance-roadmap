package org.scoula.simulator.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
import org.scoula.simulator.dto.SimulatorSavingBankDTO;
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
    // 장병내일준비적금 제도상 월 납입 총한도(여러 은행 계좌 합산 기준, 특정 은행 상품 한도와는 별개)
    private static final long MAX_SAVE_AMOUNT = 550000;
    private static final double MONTHS_IN_YEAR = 12.0;
    // 전역일이 없을 때(연동 전 등) 쓰는 폴백 복무기간(개월).
    private static final int DEFAULT_SERVICE_MONTHS = 24;

    // 금융결제원 표준 은행코드 → 은행명. military_saving_product에 실제 존재하는 코드 기준.
    // 매핑에 없는 코드가 오면 코드값을 그대로 이름 대신 보여준다(방어).
    private static final Map<String, String> BANK_NAMES = Map.ofEntries(
            Map.entry("003", "IBK기업은행"),
            Map.entry("004", "KB국민은행"),
            Map.entry("007", "수협은행"),
            Map.entry("011", "NH농협은행"),
            Map.entry("020", "우리은행"),
            Map.entry("031", "대구은행"),
            Map.entry("032", "부산은행"),
            Map.entry("034", "광주은행"),
            Map.entry("035", "제주은행"),
            Map.entry("037", "전북은행"),
            Map.entry("039", "경남은행"),
            Map.entry("071", "우체국"),
            Map.entry("081", "하나은행"),
            Map.entry("088", "신한은행")
    );

    @Transactional(readOnly = true)
    @Override
    public SimulatorSavingDetailsResponseDTO findSavingDetails(Long userId) {
        SimulatorUserDatesDTO userDates = this.findUserDatesOrThrow(userId);
        // 유저가 가진 군적금 계좌 목록 조회 (SimulatorMapper.findAccountListByUserId, 계좌 여러 개 가능)
        List<SimulatorSavingAccountDTO> accounts = this.findAccountsOrThrow(userId);

        LocalDate dischargeDate = this.resolveDischargeDate(userDates);

        long monthlySaveTotal = 0L;
        long currentPaidAmountTotal = 0L;
        long currentPaidInterestTotal = 0L;
        int currentPaidMonths = 0;
        int joinableMonths = 0;

        LocalDate accountOpenDate = null;
        LocalDate maturityDate = null;

        long expectedPrincipalTotal = 0L;
        double expectedInterestTotal = 0.0;
        long expectedMatchingFundTotal = 0L;

        List<SimulatorSavingBankDTO> banks = new ArrayList<>();

        // 계좌마다 은행(bankCode)이 다를 수 있어 계좌별로 따로 계산한 뒤 아래 total 변수들에 합산한다.
        for (SimulatorSavingAccountDTO account : accounts) {
            long monthlySave = account.getMonthlySave() != null ? account.getMonthlySave() : 0L;
            monthlySaveTotal += monthlySave;

            String bankCode = account.getBankCode();
            String bankName = bankCode != null
                    ? BANK_NAMES.getOrDefault(bankCode, bankCode)
                    : "은행 정보 없음";
            banks.add(new SimulatorSavingBankDTO(bankName, monthlySave));

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

            // 대표 계좌(가장 먼저 개설된 계좌) 기준으로 개설일·만기일·가입개월수·현재납입개월수를 통일한다.
            if (accountOpenDate == null || calc.firstPayDate.isBefore(accountOpenDate)) {
                accountOpenDate = calc.firstPayDate;
                maturityDate = calc.maturityDate;
                joinableMonths = calc.actualTotalMonths;
                currentPaidMonths = calc.maxCurrentPaidMonths;
            }

            currentPaidAmountTotal += calc.pastPrincipal;
            currentPaidInterestTotal += (long) calc.pastInterest;
            expectedPrincipalTotal += calc.getTotalPrincipal();
            expectedInterestTotal += calc.getTotalInterest();
            expectedMatchingFundTotal += calc.matchingFund;
        }

        long totalReceiptAmount = expectedPrincipalTotal + (long) expectedInterestTotal + expectedMatchingFundTotal;

        return SimulatorSavingDetailsResponseDTO.builder()
                .accountOpenDate(accountOpenDate)
                .maturityDate(maturityDate)
                .monthlySaveTotal(monthlySaveTotal)
                .joinableMonths(joinableMonths)
                .banks(banks)
                .currentPaidAmount(currentPaidAmountTotal)
                .currentPaidMonths(currentPaidMonths)
                .currentPaidInterest(currentPaidInterestTotal)
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
        LocalDate today = LocalDate.now();

        if (!today.isBefore(dischargeDate)) {
            throw BusinessException.badRequest("이미 전역하여 중도해지 대상이 아닙니다.", "SIMUL_008");
        }

        MilitarySavingWithdrawalCalculator withdrawalCalculator =
                new MilitarySavingWithdrawalCalculator(this.militarySavingProductMapper);

        long totalWithdrawalAmount = 0L;
        long maturityPrincipalTotal = 0L;
        double maturityInterestTotal = 0.0;
        long maturityMatchingFundTotal = 0L;

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

            // 이자는 계좌별로 버리지 않고 double로 다 더한 뒤 마지막에 한 번만 버려야
            // findSavingDetails의 만기수령액과 1원 단위까지 일치한다.
            maturityPrincipalTotal += calc.getTotalPrincipal();
            maturityInterestTotal += calc.getTotalInterest();
            maturityMatchingFundTotal += calc.matchingFund;

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

        long totalMaturityAmount =
                maturityPrincipalTotal + (long) maturityInterestTotal + maturityMatchingFundTotal;
        long lossAmount = totalMaturityAmount - totalWithdrawalAmount;

        return new SimulatorSavingLossResponseDTO(totalWithdrawalAmount, lossAmount);
    }

    @Transactional(readOnly = true)
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
    
    @Transactional(readOnly = true)
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
        long minSaveAmount = this.resolveMinSaveAmount();
        this.validateSaveAmount(request.getMonthlySave(), minSaveAmount);
        this.validateTotalMonths(request.getSaveMonths());
    }

    private void validateVariableRequest(SimulatorVariableCalcRequestDTO request) {
        if (request == null || request.getPeriods() == null || request.getPeriods().isEmpty()) {
            throw BusinessException.badRequest("잘못된 입력값입니다.", "SIMUL_003");
        }
        long minSaveAmount = this.resolveMinSaveAmount();
        for (SimulatorVariableCalcRequestDTO.Period period : request.getPeriods()) {
            if (period.getStartMonthOffset() == null || period.getEndMonthOffset() == null
                    || period.getAmount() == null) {
                throw BusinessException.badRequest("잘못된 입력값입니다.", "SIMUL_003");
            }
            if (period.getStartMonthOffset() < 1) {
                throw BusinessException.badRequest("가입 개월차는 1 이상이어야 합니다.", "SIMUL_009");
            }
            this.validatePeriodOrder(period.getStartMonthOffset(), period.getEndMonthOffset());
            this.validateSaveAmount(period.getAmount(), minSaveAmount);
        }
        this.validateNoOverlap(request.getPeriods());
    }

    // 기준 은행(SIMULATION_BANK_CODE)의 월 최소납입한도. 상품 데이터에 하한이 없으면 0(제한 없음)
    private long resolveMinSaveAmount() {
        Long minLimit = this.militarySavingProductMapper.findMinLimit(SIMULATION_BANK_CODE);
        return minLimit != null ? minLimit : 0L;
    }

    // 기준 은행(SIMULATION_BANK_CODE)의 최대 가입가능 개월수. 상품 데이터가 없으면 제도상 기본값으로 방어
    private int resolveMaxJoinMonths() {
        Integer maxJoinMonth = this.militarySavingProductMapper.findMaxJoinMonth(SIMULATION_BANK_CODE);
        return maxJoinMonth != null ? maxJoinMonth : MilitarySavingsCalculator.MAX_JOIN_MONTHS;
    }

    private void validateSaveAmount(long amount, long minSaveAmount) {
        if (amount > MAX_SAVE_AMOUNT) {
            throw BusinessException.badRequest("납입 한도 55만 원을 초과했습니다.", "SIMUL_004");
        }
        if (amount < minSaveAmount) {
            throw BusinessException.badRequest(
                    "월 납입액은 " + minSaveAmount + "원 이상이어야 합니다.", "SIMUL_007");
        }
    }
    
    private void validateTotalMonths(long totalMonths) {
        int maxJoinMonths = this.resolveMaxJoinMonths();
        if (totalMonths > maxJoinMonths) {
            throw BusinessException.badRequest(
                    "최대 가입기간 " + maxJoinMonths + "개월을 초과했습니다.",
                    "SIMUL_005");
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
        return amount * annualRate * (investedMonths / MONTHS_IN_YEAR);
    }

    private LocalDate resolveDischargeDate(SimulatorUserDatesDTO userDates) {
        return userDates.getDischargeDate() != null
                ? userDates.getDischargeDate()
                : LocalDate.now().plusMonths(DEFAULT_SERVICE_MONTHS);
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
