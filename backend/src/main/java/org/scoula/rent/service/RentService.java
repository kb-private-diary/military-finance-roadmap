package org.scoula.rent.service;

import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import java.util.List;

public interface RentService {
    List<RegionResponseDTO> findRegions(String sido, String sigunguCode);

    // 자취 목표 등록 → 생성된 goalId 반환
    Long createGoal(RentGoalCreateRequestDTO request, Long userId);
}
