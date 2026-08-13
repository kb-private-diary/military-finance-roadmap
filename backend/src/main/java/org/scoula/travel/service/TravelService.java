package org.scoula.travel.service;

import java.util.List;

import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.dto.TravelCostResponseDTO;
import org.scoula.travel.dto.TravelCostStyleUpdateRequestDTO;
import org.scoula.travel.dto.TravelGoalCreateRequestDTO;
import org.scoula.travel.dto.TravelGoalDraftResponseDTO;
import org.scoula.travel.dto.TravelGoalDetailResponseDTO;
import org.scoula.travel.dto.TravelPlaceResponseDTO;
import org.scoula.travel.dto.TravelPlaceSelectionDTO;
import org.scoula.travel.dto.TravelPlacesUpdateRequestDTO;
import org.scoula.travel.dto.TravelPackageResponseDTO;
import org.scoula.travel.dto.TravelPackageUpdateRequestDTO;
import org.scoula.travel.dto.TravelProductRecommendationResponseDTO;

/**
 * 여행 로드맵 서비스.
 *
 * <p>로그인 사용자는 컨트롤러가 {@code @AuthenticationPrincipal} 로 꺼내 넘긴다.
 * 목표 소유권을 따지는 메서드는 {@code userId} 를, 감사 컬럼(created_nm·modified_nm)을
 * 남기는 메서드는 {@code userName}(로그인 아이디)을 받는다.
 * 둘 다 필요 없는 조회는 파라미터를 두지 않는다.</p>
 */
public interface TravelService {

    // 도시 물가 목록 조회. country가 null이면 전체 조회.
    List<CityCostResponseDTO> findCityCosts(String country);

    // 여행 목표 등록
    Long createGoal(
            final Long userId,
            final String userName,
            final TravelGoalCreateRequestDTO request);

    TravelGoalDraftResponseDTO findCurrentDraft(final Long userId);

    TravelGoalDetailResponseDTO getGoalDetail(
            final Long userId,
            final Long goalId);

    void updateGoal(
            final Long userId,
            final Long goalId,
            final String userName,
            final TravelGoalCreateRequestDTO request);

    // 예상 경비 산출 후 저장. 이미 결과가 있으면 현재 값을 갱신.
    Long createCost(final Long goalId, final String userName);

    // 산출된 예상 경비 조회
    TravelCostResponseDTO findCost(Long goalId);

    void updateCostStyle(
            final Long userId,
            final Long goalId,
            final String userName,
            final TravelCostStyleUpdateRequestDTO request);

    // 저장된 목표의 도착지를 기준으로 관광지 또는 맛집을 검색한다.
    List<TravelPlaceResponseDTO> searchPlaces(
            final Long goalId,
            final String category);

    void updatePlaces(
            final Long goalId,
            final String userName,
            final TravelPlacesUpdateRequestDTO request);

    List<TravelPlaceSelectionDTO> getSelectedPlaces(final Long goalId);

    List<TravelPackageResponseDTO> findPackages(
            final Long goalId,
            final String userName);

    void updatePackage(
            final Long userId,
            final Long goalId,
            final String userName,
            final TravelPackageUpdateRequestDTO request);

    TravelProductRecommendationResponseDTO findProducts(
            final Long userId,
            final Long goalId);

    void confirmGoal(
            final Long userId,
            final Long goalId,
            final String userName);

}
