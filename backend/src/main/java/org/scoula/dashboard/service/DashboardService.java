package org.scoula.dashboard.service;

import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationCreateRequestDTO;
import org.scoula.dashboard.dto.DashboardVacationDetailResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationListResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationUsageCreateRequestDTO;

public interface DashboardService {
    // DASH-API-01: 복무 기본 정보 조회
    DashboardBasicResponseDTO findBasicInfo(Long userId);

    // DASH-API-03: 적금 현황 조회
    DashboardSavingsResponseDTO findSavingsStatus(Long userId);

    // DASH-API-04: 휴가 목록(요약 + 카드 목록) 조회
    DashboardVacationListResponseDTO findVacations(Long userId);

    // DASH-API-05: 휴가 상세 조회
    DashboardVacationDetailResponseDTO findVacationDetail(Long userId, Long vacationId);

    // DASH-API-06: 휴가 부여 등록 (REGULAR 제외, 생성된 vacationId 반환)
    Long createVacation(Long userId, String createdNm, DashboardVacationCreateRequestDTO request);

    // DASH-API-07: 휴가 부여 수정 (REGULAR 제외)
    void updateVacation(
            Long userId, Long vacationId, String modifiedNm,
            DashboardVacationCreateRequestDTO request);

    // DASH-API-08: 휴가 부여 삭제 (REGULAR 제외, 소프트 삭제)
    void deleteVacation(Long userId, Long vacationId, String modifiedNm);

    // DASH-API-09: 휴가 사용내역 등록 (생성된 historyId 반환)
    Long createVacationUsage(
            Long userId, String createdNm, Long vacationId,
            DashboardVacationUsageCreateRequestDTO request);

    // DASH-API-10: 휴가 사용내역 삭제 (소프트 삭제)
    void deleteVacationUsage(Long userId, Long historyId, String modifiedNm);
}
