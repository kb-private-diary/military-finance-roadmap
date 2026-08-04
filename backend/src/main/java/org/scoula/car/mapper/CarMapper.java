package org.scoula.car.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.car.domain.CarGoalVO;
import org.scoula.car.domain.CarInsuranceVO;
import org.scoula.car.domain.CarModelVO;
import org.scoula.car.domain.CarTaxVO;

public interface CarMapper {
    // 자동차 목표 신규 등록
    void createCarGoal(CarGoalVO carGoalVO);

    // 자동차 목표 단건 조회
    CarGoalVO selectCarGoalById(@Param("goalId") Long goalId);

    // 신차 모델 단건 조회
    CarModelVO selectCarModelById(@Param("modelId") Long modelId);

    // 차종+운전경력구간 기준 보험료 조회
    CarInsuranceVO selectInsuranceByTypeAndBracket(
            @Param("carTypeCode") Integer carTypeCode,
            @Param("experienceBracket") String experienceBracket);

    // 차종 기준 취득세율/공채면제기준 조회
    CarTaxVO selectTaxByTypeCode(@Param("carTypeCode") Integer carTypeCode);
}
