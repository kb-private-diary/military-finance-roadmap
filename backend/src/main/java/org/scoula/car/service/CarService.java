package org.scoula.car.service;

import java.util.List;

import org.scoula.car.dto.CarAcquisitionTaxResponseDTO;
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
    CarGoalCreateResponseDTO createCarGoal(CarGoalCreateRequestDTO requestDTO);

    // CAR-API-03: 유저의 자동차 목표 목록 조회
    List<CarGoalResponseDTO> findCarGoals(Long userId);

    // CAR-API-04: 자동차 목표 상세 조회
    CarGoalResponseDTO findCarGoalDetail(Long goalId);

    // CAR-API-07: 예산/차종 기반 차량 추천 목록 조회
    List<CarRecommendationResponseDTO> recommendCars(Long goalId);

    // CAR-API-08: 추천 목록 중 차량 모델 선택 반영
    CarGoalResponseDTO selectCarModel(Long goalId, CarModelSelectRequestDTO requestDTO);

    // CAR-API-09: 오피넷 유가 연동 기반 연간 유지비(연료비+보험료) 계산
    CarMaintenanceCostResponseDTO calculateMaintenanceCost(Long goalId);

    // CAR-API-10: 취득세 및 공채매입 여부 계산
    CarAcquisitionTaxResponseDTO calculateAcquisitionTax(Long goalId);

    // CAR-API-13: 연차별 정률감가 기반 중고차 시세 추정
    CarUsedPriceResponseDTO calculateUsedPrice(Long goalId);

    // CAR-API-12: 지역 기준 전기차 보조금 계산
    CarEvSubsidyResponseDTO calculateEvSubsidy(Long goalId);
}
