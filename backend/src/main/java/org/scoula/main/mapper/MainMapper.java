package org.scoula.main.mapper;

import java.util.List;

import org.scoula.main.dto.MainFavoriteRoadmapDTO;
import org.scoula.main.dto.MainUpcomingScheduleDTO;

public interface MainMapper {

    // 사용자가 관심 등록한 카테고리별 목표 목록 조회
    List<MainFavoriteRoadmapDTO> findFavoriteRoadmapList(Long userId);

    // 여행·진로 일정 중 오늘 이후 가장 가까운 일정 2개 조회
    List<MainUpcomingScheduleDTO> findUpcomingScheduleList(Long userId);
}