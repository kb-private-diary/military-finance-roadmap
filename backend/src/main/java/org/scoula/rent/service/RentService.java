package org.scoula.rent.service;

import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.SchoolSearchResponseDTO;
import org.scoula.rent.dto.ProgressUpdateRequestDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import java.util.List;

public interface RentService {
    List<RegionResponseDTO> findRegions(String sido, String sigunguCode);

    // 자취 목표 등록 → 생성된 goalId 반환
    Long createGoal(RentGoalCreateRequestDTO request, Long userId);

    // 학교 검색 (자동완성)
    List<SchoolSearchResponseDTO> findSchools(String keyword);

    // 진행률 단계 수정 (UPSERT) → 갱신된 진행률(%) 반환
    int updateProgress(Long goalId, ProgressUpdateRequestDTO request, Long userId);

    // 목표 상세 조회 (목표 정보 + 진행률 5단계)
    RentGoalDetailResponseDTO findGoal(Long goalId);
}
