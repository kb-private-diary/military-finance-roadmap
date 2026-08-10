package org.scoula.roadmap.service;

import org.scoula.roadmap.dto.RoadmapListResponseDTO;

import java.util.List;

public interface RoadmapService {
    // 나의 로드맵 목록 조회
    List<RoadmapListResponseDTO> findRoadmapList(
            Long userId,
            String category
    );

    // 로드맵 목표 삭제
    void deleteRoadmapGoal(
            Long userId,
            Long categoryId,
            Long goalId,
            String modifiedNm
    );
}

