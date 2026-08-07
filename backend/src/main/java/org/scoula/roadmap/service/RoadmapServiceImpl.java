package org.scoula.roadmap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.exception.BusinessException;
import org.scoula.roadmap.dto.RoadmapListResponseDTO;
import org.scoula.roadmap.mapper.RoadmapMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class RoadmapServiceImpl implements RoadmapService{

    private static final Set<String> VALID_CATEGORIES =
            Set.of("all", "travel", "job", "car", "rent");

    private final RoadmapMapper roadmapMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoadmapListResponseDTO> findRoadmapList(
            Long userId,
            String category) {

        String normalizedCategory =
                category == null ? "all" : category.trim().toLowerCase();

        if (!VALID_CATEGORIES.contains(normalizedCategory)) {
            throw BusinessException.badRequest(
                    "올바르지 않은 카테고리입니다.",
                    "ROADMAP_001"
            );
        }

        return this.roadmapMapper.findRoadmapList(userId, normalizedCategory);
    }
}
