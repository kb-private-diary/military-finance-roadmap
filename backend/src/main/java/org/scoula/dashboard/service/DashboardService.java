package org.scoula.dashboard.service;

import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;

public interface DashboardService {
    // DASH-API-01: 복무 기본 정보 조회
    DashboardBasicResponseDTO findBasicInfo(Long userId);

    // DASH-API-03: 적금 현황 조회
    DashboardSavingsResponseDTO findSavingsStatus(Long userId);
}
