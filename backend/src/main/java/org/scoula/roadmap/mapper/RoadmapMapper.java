package org.scoula.roadmap.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.roadmap.dto.RoadmapListResponseDTO;

import java.util.List;

public interface RoadmapMapper {
    List<RoadmapListResponseDTO> findRoadmapList(
            @Param("userId") Long userId,
            @Param("category") String category
    );
}
