package org.scoula.car.service;

import java.util.List;

import org.scoula.car.dto.CarAcquisitionTaxResponseDTO;
import org.scoula.car.dto.CarBudgetStatusResponseDTO;
import org.scoula.car.dto.CarEvSubsidyResponseDTO;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.car.dto.CarGoalResponseDTO;
import org.scoula.car.dto.CarMaintenanceCostResponseDTO;
import org.scoula.car.dto.CarModelSelectRequestDTO;
import org.scoula.car.dto.CarRecommendationResponseDTO;
import org.scoula.car.dto.CarUsedPriceResponseDTO;

public interface CarService {
    // 자동차 목표 신규 등록, 생성된 goalId를 담은 응답 DTO 반환
    CarGoalCreateResponseDTO createCarGoal(Long userId, CarGoalCreateRequestDTO requestDTO);

    // CAR-API-03: 유저의 자동차 목표 목록 조회
    List<CarGoalResponseDTO> findCarGoals(Long userId);

    // CAR-API-04: 자동차 목표 상세 조회
    CarGoalResponseDTO findCarGoalDetail(Long goalId, Long userId);

    // CAR-API-07: 예산/차종 기반 차량 추천 목록 조회
    List<CarRecommendationResponseDTO> recommendCars(Long goalId, Long userId);

    // CAR-API-16: 중고차 목표 한정 — 연식/키로수 직접 선택 기반 차량 추천 목록 조회
    List<CarRecommendationResponseDTO> recommendCarsByFilter(
            Long goalId, Long userId, Integer year, Integer mileageKm);

    // CAR-API-08: 추천 목록 중 차량 모델 선택 반영
    CarGoalResponseDTO selectCarModel(Long goalId, Long userId, CarModelSelectRequestDTO requestDTO);

    // CAR-API-09: 오피넷 유가 연동 기반 연간 유지비(연료비+보험료) 계산
    CarMaintenanceCostResponseDTO calculateMaintenanceCost(Long goalId, Long userId);

    // CAR-API-10: 취득세 및 공채매입 여부 계산
    CarAcquisitionTaxResponseDTO calculateAcquisitionTax(Long goalId, Long userId);

    // CAR-API-13: 연차별 정률감가 기반 중고차 시세 추정
    CarUsedPriceResponseDTO calculateUsedPrice(Long goalId, Long userId);

    // CAR-API-12: 지역 기준 전기차 보조금 계산
    CarEvSubsidyResponseDTO calculateEvSubsidy(Long goalId, Long userId);

    // CAR-API-14: 선택 차량 구매비용이 기준 예산(수동입력 또는 군적금 만기예상액)을 넘는지 확인
    CarBudgetStatusResponseDTO checkBudgetStatus(Long goalId, Long userId);

    // CAR-API-15: 자동차 목표 확정(완료) — status를 CONFIRMED로 전환한다.
    // 로드맵(관심 목록)에서 자동차 목표가 노출되는 조건이 이 상태다.
    void confirmGoal(Long goalId, Long userId);
}
