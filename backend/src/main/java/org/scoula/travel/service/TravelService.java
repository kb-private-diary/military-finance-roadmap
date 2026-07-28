package org.scoula.travel.service;

import java.util.List;

import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.dto.TravelCostCreateRequestDTO;
import org.scoula.travel.dto.TravelCostResponseDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;
import org.scoula.travel.dto.TravelPlaceResponseDTO;

// 여행 로드맵 서비스
public interface TravelService {

    // 도시 물가 목록 조회. country가 null이면 전체 조회.
    List<CityCostResponseDTO> findCityCosts(String country);

    // 여행 목표 등록
    Long createGoal(TravelGoalCreateRequestDTO request);

    // 예상 경비 산출 후 저장. 이미 결과가 있으면 소프트 삭제 후 재적재.
    Long createCost(Long goalId, TravelCostCreateRequestDTO request);

    // 산출된 예상 경비 조회
    TravelCostResponseDTO findCost(Long goalId);

    // 저장된 목표의 도착지를 기준으로 관광지 또는 맛집을 검색한다.
    List<TravelPlaceResponseDTO> searchPlaces(
            final Long goalId,
            final String category);

}
