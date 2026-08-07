package org.scoula.roadmap.service;

import org.scoula.roadmap.dto.RoadmapListResponseDTO;

import java.util.List;

public interface RoadmapService {
    List<RoadmapListResponseDTO> findRoadmapList(
            Long userId,
            String category
    );
}
