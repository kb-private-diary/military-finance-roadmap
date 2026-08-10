package org.scoula.roadmap.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.roadmap.dto.RoadmapListResponseDTO;

import java.util.List;

public interface RoadmapMapper {
    List<RoadmapListResponseDTO> findRoadmapList(
            @Param("userId") Long userId,
            @Param("category") String category
    );

    // 로드맵 목표 soft delete
    int deleteRoadmapGoal(
            @Param("categoryId") Long categoryId,
            @Param("goalId") Long goalId,
            @Param("userId") Long userId,
            @Param("modifiedNm") String modifiedNm
    );

    // 삭제한 로드맵의 관심등록 soft delete
    int deleteBookmarkByGoal(
            @Param("categoryId") Long categoryId,
            @Param("goalId") Long goalId,
            @Param("userId") Long userId,
            @Param("modifiedNm") String modifiedNm
    );
}
