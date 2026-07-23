package org.scoula.dashboard.service;

import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;

public interface DashboardService {
    DashboardSavingsResponseDTO findSavingsStatus(Long userId);
}
