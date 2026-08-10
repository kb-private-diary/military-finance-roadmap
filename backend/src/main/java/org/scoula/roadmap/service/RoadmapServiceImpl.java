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

    private static final Set<Long> VALID_CATEGORY_IDS =
            Set.of(1L, 2L, 3L, 4L);

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

    // 로드맵 목표 삭제
    @Override
    @Transactional
    public void deleteRoadmapGoal(
            Long userId,
            Long categoryId,
            Long goalId,
            String modifiedNm) {

        // 지원하지 않는 카테고리 방지
        if (categoryId == null || !VALID_CATEGORY_IDS.contains(categoryId)) {
            throw BusinessException.badRequest(
                    "올바르지 않은 로드맵 카테고리입니다.",
                    "ROADMAP_002"
            );
        }

        // 실제 로드맵 목표 soft delete
        int updatedCount = this.roadmapMapper.deleteRoadmapGoal(
                categoryId,
                goalId,
                userId,
                modifiedNm
        );

        // 본인의 존재하는 로드맵이 아닌 경우
        if (updatedCount == 0) {
            throw BusinessException.notFound(
                    "삭제할 로드맵을 찾을 수 없습니다.",
                    "ROADMAP_003"
            );
        }

        // 해당 로드맵에 관심등록이 되어 있었다면 함께 soft delete
        this.roadmapMapper.deleteBookmarkByGoal(
                categoryId,
                goalId,
                userId,
                modifiedNm
        );

        log.info(
                "로드맵 삭제 완료: userId={}, categoryId={}, goalId={}",
                userId,
                categoryId,
                goalId
        );
    }
}
