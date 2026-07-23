package org.scoula.dashboard.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
// openbanking 패키지에서 VO 객체 import (향후 교체)
import org.scoula.dashboard.dto.DashboardSavingAccountDTO;
import org.scoula.dashboard.dto.DashboardSavingHistoryDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.mapper.DashboardMapper;

@Service
@RequiredArgsConstructor
@Log4j2
public class DashboardServiceImpl implements DashboardService {
    // DashboardSavingAccountDTO account -> SavingAccountVO account 교체
    // VO getter 가 같은지 확인 필요(getCreatedDate(), getMonthlySave() 등)
    private final DashboardMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardBasicResponseDTO findBasicInfo(Long userId) {
        // 1. DB에서 조인된 기본 정보 가져오기
        DashboardBasicResponseDTO dto = this.mapper.getBasicInfoByUserId(userId);
        if (dto == null) {
            return null; // 프론트엔드나 컨트롤러에서 404 예외 처리
        }
        
        LocalDate enlistDate = dto.getEnlistDate();
        LocalDate dischargeDate = dto.getDischargeDate();
        LocalDate today = LocalDate.now();
        
        // 날짜가 없으면 기본값(0) 반환 (방어 로직)
        if (enlistDate == null || dischargeDate == null) {
            dto.setTotalServiceDays(0L);
            dto.setCurrentServiceDays(0L);
            dto.setServiceRate(0.0);
            return dto;
        }
        
        // 2. 총 복무일 (입대일 ~ 전역일 + 1)
        long totalServiceDays = ChronoUnit.DAYS.between(enlistDate, dischargeDate) + 1;
        if (totalServiceDays <= 0) totalServiceDays = 1; // 0으로 나누기 방지
        
        // 3. 현재 복무일 (입대일 ~ 오늘 + 1)
        long currentServiceDays;
        if (today.isBefore(enlistDate)) {
            currentServiceDays = 0L; // 입대 전
        } else if (today.isAfter(dischargeDate)) {
            currentServiceDays = totalServiceDays; // 전역 후
        } else {
            currentServiceDays = ChronoUnit.DAYS.between(enlistDate, today) + 1; // 복무 중
        }
        
        // 4. 복무율 (백분율 계산 후 소수점 첫째 자리까지만 포맷팅)
        double serviceRate = ((double) currentServiceDays / totalServiceDays) * 100.0;
        serviceRate = Math.round(serviceRate * 10.0) / 10.0;
        
        // 5. 계산된 필드 셋팅
        dto.setTotalServiceDays(totalServiceDays);
        dto.setCurrentServiceDays(currentServiceDays);
        dto.setServiceRate(serviceRate);
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSavingsResponseDTO findSavingsStatus(Long userId) {
        Long currentTotalSavings = this.mapper.getCurrentTotalSavings(userId);
        List<DashboardSavingAccountDTO> accounts = this.mapper.getSavingAccountsByUserId(userId);
        
        // 기획 변경: 군적금 가입자만 이용 가능하므로, 계좌가 없으면 404 예외 처리 유도
        if (accounts == null || accounts.isEmpty()) {
            return null; 
        }
        
        LocalDate dischargeDate = this.mapper.getDischargeDateByUserId(userId);
        if (dischargeDate == null) {
            dischargeDate = LocalDate.now().plusMonths(24); // fallback
        }
        
        // 입대일 가져오기 (복무개월수 한도 계산용)
        org.scoula.dashboard.dto.DashboardBasicResponseDTO basicInfo = this.mapper.getBasicInfoByUserId(userId);
        LocalDate enlistDate = (basicInfo != null && basicInfo.getEnlistDate() != null) ? basicInfo.getEnlistDate() : LocalDate.now();
        int totalServiceMonths = (int) ChronoUnit.MONTHS.between(enlistDate.withDayOfMonth(1), dischargeDate.withDayOfMonth(1));
        if (totalServiceMonths <= 0) totalServiceMonths = 1;
        
        Long expectedMaturityTotal = 0L;
        
        // 군적금 기본 금리 및 지원금 조건 (현재 스펙 기준 가정)
        double annualInterestRate = 0.05; // 연이율 5%
        double governmentMatchingRate = 1.0; // 정부 매칭 기여금 100% (2024년 기준)
        
        if (accounts != null) {
            for(DashboardSavingAccountDTO account : accounts) {
                // 계좌별 동적 만기 개월수 계산 (계좌 생성일 ~ 전역일)
                LocalDate createdDate = account.getCreatedDate() != null 
                    ? account.getCreatedDate() 
                    : LocalDate.now();
                    
                int totalMaturityMonths = (int) ChronoUnit.MONTHS.between(
                        createdDate.withDayOfMonth(1), 
                        dischargeDate.withDayOfMonth(1)) + 1;
                if (totalMaturityMonths < 0) totalMaturityMonths = 0;
                if (totalMaturityMonths > 24) totalMaturityMonths = 24; // 법정 최대 가입기간(24개월) 제한
                
                // 1. 해당 계좌의 실제 납입 내역(saving_history) 가져오기
                List<DashboardSavingHistoryDTO> histories = 
                        this.mapper.getSavingHistoryByAccountId(account.getAccountId());
                
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
                
                // 매칭지원금 최대 한도 제한 (복무개월수 * 해당 계좌 월 납입액)
                double maxMatchingFundLimit = (double) totalServiceMonths * monthlySave;
                if (matchingFund > maxMatchingFundLimit) {
                    matchingFund = maxMatchingFundLimit;
                }
                
                // 6. 비과세 처리 적용(세금 0) 및 최종 예상 만기 수령액 합산
                expectedMaturityTotal += (long) (totalPrincipal + totalInterest + matchingFund);
            }
        }
        
        return new DashboardSavingsResponseDTO(currentTotalSavings, expectedMaturityTotal);
    }
}
