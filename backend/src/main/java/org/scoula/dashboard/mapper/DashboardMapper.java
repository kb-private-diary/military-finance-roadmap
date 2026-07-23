package org.scoula.dashboard.mapper;

// import org.scoula.openbanking.dto.SavingAccoutDTO;  openbanking 패키지 에 DTO 파일로 교체 필요
// import org.scoula.openbanking.dto.SavingHistoryDTO; openbanking 패키지 에 DTO 파일로 교체 필요
import org.scoula.dashboard.dto.DashboardSavingAccountDTO;
import org.scoula.dashboard.dto.DashboardSavingHistoryDTO;

import java.util.Date;
import java.util.List;

public interface DashboardMapper {
    // 현재 누적 납입액 조회 (saving_history 테이블 합산)
    Long getCurrentTotalSavings(Long userId);
    
    // 회원의 전역일 조회 (만기 수령액 계산용)
    Date getDischargeDateByUserId(Long userId);
    
    // 계좌 정보 조회 (예상 만기 수령액 계산용)
    // openbanking 패키지에 맞게 변경 예: List<SavingAccountVO>, List<SavingHistoryVO>
    List<DashboardSavingAccountDTO> getSavingAccountsByUserId(Long userId);
    
    // 계좌의 납입 내역 조회 (정확한 이자 계산용)
    List<DashboardSavingHistoryDTO> getSavingHistoryByAccountId(Long accountId);
}
