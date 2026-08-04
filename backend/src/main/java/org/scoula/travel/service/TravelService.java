package org.scoula.travel.service;

import java.util.List;

import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.dto.TravelCostResponseDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;
import org.scoula.travel.dto.TravelGoalDraftResponseDTO;
import org.scoula.travel.dto.TravelPlaceResponseDTO;
import org.scoula.travel.dto.TravelPlaceSelectionDTO;
import org.scoula.travel.dto.TravelPlacesUpdateRequestDTO;
import org.scoula.travel.dto.TravelPackageResponseDTO;
import org.scoula.travel.dto.TravelPackageUpdateRequestDTO;

// 여행 로드맵 서비스
public interface TravelService {

    // 도시 물가 목록 조회. country가 null이면 전체 조회.
    List<CityCostResponseDTO> findCityCosts(String country);

    // 여행 목표 등록
    Long createGoal(TravelGoalCreateRequestDTO request);

    TravelGoalDraftResponseDTO findCurrentDraft();

    void updateGoal(
            final Long goalId,
            final TravelGoalCreateRequestDTO request);

    // 예상 경비 산출 후 저장. 이미 결과가 있으면 현재 값을 갱신.
    Long createCost(final Long goalId);

    // 산출된 예상 경비 조회
    TravelCostResponseDTO findCost(Long goalId);

    // 저장된 목표의 도착지를 기준으로 관광지 또는 맛집을 검색한다.
    List<TravelPlaceResponseDTO> searchPlaces(
            final Long goalId,
            final String category);

    void updatePlaces(
            final Long goalId,
            final TravelPlacesUpdateRequestDTO request);

    List<TravelPlaceSelectionDTO> getSelectedPlaces(final Long goalId);

    List<TravelPackageResponseDTO> findPackages(final Long goalId);

    void updatePackage(
            final Long goalId,
            final TravelPackageUpdateRequestDTO request);

}
