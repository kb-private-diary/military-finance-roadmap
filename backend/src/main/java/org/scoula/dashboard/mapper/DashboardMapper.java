package org.scoula.dashboard.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;

// import org.scoula.openbanking.dto.SavingAccoutDTO;  openbanking 패키지 에 DTO 파일로 교체 필요
// import org.scoula.openbanking.dto.SavingHistoryDTO; openbanking 패키지 에 DTO 파일로 교체 필요
import org.scoula.dashboard.domain.VacationHistoryVO;
import org.scoula.dashboard.domain.VacationVO;
import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardDDayUserDTO;
import org.scoula.dashboard.dto.DashboardSavingAccountDTO;
import org.scoula.dashboard.dto.DashboardSavingHistoryDTO;

public interface DashboardMapper {
    // 복무 기본 정보 조회 (DASH-API-01)
    DashboardBasicResponseDTO findBasicInfoByUserId(Long userId);

    // D-Day 푸시 알림 배치 대상(활성 유저 + 전역일) 조회
    List<DashboardDDayUserDTO> findActiveUsersWithDischargeDate();

    // 현재 누적 납입액 조회 (saving_history 테이블 합산)
    Long findCurrentTotalSavings(Long userId);

    // 특정 유저의 전역일 조회
    LocalDate findDischargeDateByUserId(Long userId);

    // 계좌 정보 조회 (예상 만기 수령액 계산용)
    // openbanking 패키지에 맞게 변경 예: List<SavingAccountVO>, List<SavingHistoryVO>
    List<DashboardSavingAccountDTO> findSavingAccountListByUserId(Long userId);

    // 계좌의 납입 내역 조회 (정확한 이자 계산용)
    List<DashboardSavingHistoryDTO> findSavingHistoryListByAccountId(Long accountId);

    // 유저의 휴가(부여) 전체 목록 조회 (DASH-API-03). 잔여일수 계산은 서비스에서 사용내역 합계로 처리
    List<VacationVO> findVacationListByUserId(Long userId);

    // 휴가(부여) 단건 조회 (DASH-API-04). userId로 소유권까지 같이 검증
    VacationVO findVacationById(@Param("vacationId") Long vacationId, @Param("userId") Long userId);

    // 휴가(부여) 등록 (DASH-API-05). insert 후 vo.vacationId에 생성된 id가 채워진다
    void insertVacation(VacationVO vo);

    // 휴가(부여) 수정 (DASH-API-06). vacationId 기준 UPDATE
    void updateVacation(VacationVO vo);

    // 휴가(부여) 삭제 (DASH-API-07). 소프트 삭제(del_yn='Y')
    void deleteVacation(
            @Param("vacationId") Long vacationId, @Param("modifiedNm") String modifiedNm);

    // 특정 부여(vacationId)의 사용내역 전체 조회 (DASH-API-04 상세용)
    List<VacationHistoryVO> findVacationHistoryListByVacationId(Long vacationId);

    // 사용내역 단건 조회 (DASH-API-09 삭제용). vacation과 join해 userId 소유권까지 같이 검증
    VacationHistoryVO findVacationHistoryById(
            @Param("historyId") Long historyId, @Param("userId") Long userId);

    // 사용내역 등록 (DASH-API-08). insert 후 vo.historyId에 생성된 id가 채워진다
    void insertVacationHistory(VacationHistoryVO vo);

    // 사용내역 삭제 (DASH-API-09). 소프트 삭제(del_yn='Y')
    void deleteVacationHistory(
            @Param("historyId") Long historyId, @Param("modifiedNm") String modifiedNm);
}
