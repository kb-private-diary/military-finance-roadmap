package org.scoula.car.mapper;

import org.scoula.car.domain.CarGoalVO;

public interface CarMapper {
    // 자동차 목표 신규 등록
    void createCarGoal(CarGoalVO carGoalVO);
}
