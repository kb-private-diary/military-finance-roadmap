package org.scoula.travel.service;

import java.util.List;

import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;

// 여행 로드맵 서비스
public interface TravelService {

    // 도시 물가 목록 조회. country가 null이면 전체 조회.
    List<CityCostResponseDTO> findCityCosts(String country);

    // 여행 목표 등록
    Long createGoal(TravelGoalCreateRequestDTO request);
}
