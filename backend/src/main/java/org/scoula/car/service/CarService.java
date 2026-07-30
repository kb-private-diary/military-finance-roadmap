package org.scoula.car.service;

import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;

public interface CarService {
    // 자동차 목표 신규 등록, 생성된 goalId를 담은 응답 DTO 반환
    CarGoalCreateResponseDTO createCarGoal(CarGoalCreateRequestDTO requestDTO);
}
