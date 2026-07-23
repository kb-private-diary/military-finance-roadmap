package org.scoula.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
// openbanking 패키지에서 VO 객체 import
import org.scoula.dashboard.dto.DashboardSavingAccountDTO;
import org.scoula.dashboard.dto.DashboardSavingHistoryDTO;
//
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.mapper.DashboardMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class DashboardServiceImpl implements DashboardService {
    // DashboardSavingAccountDTO account -> SavingAccountVO account 교체
    // VO getter 가 같은지 확인 필요(getCreatedDate(), getMonthlySave() 등)
    private final DashboardMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardSavingsResponseDTO findSavingsStatus(Long userId) {
        Long currentTotalSavings = mapper.getCurrentTotalSavings(userId);
        List<DashboardSavingAccountDTO> accounts = mapper.getSavingAccountsByUserId(userId);
        Date dischargeDateUtil = mapper.getDischargeDateByUserId(userId);
        
        LocalDate dischargeDate = (dischargeDateUtil != null) 
            ? dischargeDateUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() 
            : LocalDate.now().plusMonths(24); // fallback
        
        Long expectedMaturityTotal = 0L;
        
        // 군적금 기본 금리 및 지원금 조건 (현재 스펙 기준 가정)
        double annualInterestRate = 0.05; // 연이율 5%
        double governmentMatchingRate = 1.0; // 정부 매칭 기여금 100% (2024년 기준)
        
        if (accounts != null) {
            for(DashboardSavingAccountDTO account : accounts) {
                // 계좌별 동적 만기 개월수 계산 (계좌 생성일 ~ 전역일)
                LocalDate createdDate = (account.getCreatedDate() != null) 
                    ? account.getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() 
                    : LocalDate.now();
                    
                int totalMaturityMonths = (int) ChronoUnit.MONTHS.between(createdDate.withDayOfMonth(1), dischargeDate.withDayOfMonth(1)) + 1;
                if (totalMaturityMonths < 0) totalMaturityMonths = 0;
                if (totalMaturityMonths > 24) totalMaturityMonths = 24; // 장병내일준비적금 법정 최대 가입기간(24개월) 제한
                
                // 1. 해당 계좌의 실제 납입 내역(saving_history) 가져오기
                List<DashboardSavingHistoryDTO> histories = mapper.getSavingHistoryByAccountId(account.getAccountId());
                
                long pastPrincipal = 0L;
                double pastInterest = 0.0;
                int currentRound = 0;
                
                // 2. 이미 납입한 내역(과거 데이터)에 대한 원금 및 이자 계산
                if (histories != null) {
                    for (DashboardSavingHistoryDTO history : histories) {
                        currentRound = history.getPayRound();
                        long amount = history.getPayAmount();
                        pastPrincipal += amount;
                        
                        // 거치개월수 = (전체만기개월수 - 현재회차 + 1)
                        int investedMonths = totalMaturityMonths - currentRound + 1;
                        if (investedMonths < 0) investedMonths = 0;
                        pastInterest += amount * annualInterestRate * (investedMonths / 12.0);
                    }
                }
                
                // 3. 앞으로 납입할 내역(미래 데이터)에 대한 원금 및 이자 계산
                long futurePrincipal = 0L;
                double futureInterest = 0.0;
                Long monthlySave = account.getMonthlySave() != null ? account.getMonthlySave() : 0L;
                
                for (int round = currentRound + 1; round <= totalMaturityMonths; round++) {
                    futurePrincipal += monthlySave;
                    
                    int investedMonths = totalMaturityMonths - round + 1;
                    futureInterest += monthlySave * annualInterestRate * (investedMonths / 12.0);
                }
                
                // 4. 총 원금 및 총 이자 합산
                long totalPrincipal = pastPrincipal + futurePrincipal;
                double totalInterest = pastInterest + futureInterest;
                
                // 5. 정부 매칭지원금 계산 (납입 원금의 100%)
                double matchingFund = totalPrincipal * governmentMatchingRate;
                
                // 6. 비과세 처리 적용(세금 0) 및 최종 예상 만기 수령액 합산
                expectedMaturityTotal += (long) (totalPrincipal + totalInterest + matchingFund);
            }
        }
        
        return new DashboardSavingsResponseDTO(currentTotalSavings, expectedMaturityTotal);
    }
}
