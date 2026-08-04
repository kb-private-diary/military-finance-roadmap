package org.scoula.dashboard.service;

import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationDetailResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationListResponseDTO;

public interface DashboardService {
    // DASH-API-01: 복무 기본 정보 조회
    DashboardBasicResponseDTO findBasicInfo(Long userId);

    // DASH-API-03: 적금 현황 조회
    DashboardSavingsResponseDTO findSavingsStatus(Long userId);

    // DASH-API-04: 휴가 목록(요약 + 카드 목록) 조회
    DashboardVacationListResponseDTO findVacations(Long userId);

    // DASH-API-05: 휴가 상세 조회
    DashboardVacationDetailResponseDTO findVacationDetail(Long userId, Long vacationId);
}
