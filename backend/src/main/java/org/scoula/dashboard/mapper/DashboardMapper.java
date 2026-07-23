package org.scoula.dashboard.mapper;

import java.time.LocalDate;
import java.util.List;

// import org.scoula.openbanking.dto.SavingAccoutDTO;  openbanking 패키지 에 DTO 파일로 교체 필요
// import org.scoula.openbanking.dto.SavingHistoryDTO; openbanking 패키지 에 DTO 파일로 교체 필요
import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardSavingAccountDTO;
import org.scoula.dashboard.dto.DashboardSavingHistoryDTO;

public interface DashboardMapper {
    // 복무 기본 정보 조회 (DASH-API-01)
    DashboardBasicResponseDTO findBasicInfoByUserId(Long userId);

    // 현재 누적 납입액 조회 (saving_history 테이블 합산)
    Long findCurrentTotalSavings(Long userId);
    
    // 특정 유저의 전역일 조회
    LocalDate findDischargeDateByUserId(Long userId);
    
    // 계좌 정보 조회 (예상 만기 수령액 계산용)
    // openbanking 패키지에 맞게 변경 예: List<SavingAccountVO>, List<SavingHistoryVO>
    List<DashboardSavingAccountDTO> findSavingAccountListByUserId(Long userId);
    
    // 계좌의 납입 내역 조회 (정확한 이자 계산용)
    List<DashboardSavingHistoryDTO> findSavingHistoryListByAccountId(Long accountId);
}
