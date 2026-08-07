package org.scoula.car.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.scoula.car.domain.CarEvVO;
import org.scoula.car.domain.CarGoalVO;
import org.scoula.car.domain.CarInsuranceVO;
import org.scoula.car.domain.CarModelVO;
import org.scoula.car.domain.CarTaxPrepayVO;
import org.scoula.car.domain.CarTaxVO;
import org.scoula.car.dto.CarGoalResponseDTO;

public interface CarMapper {
    // 자동차 목표 신규 등록
    void createCarGoal(CarGoalVO carGoalVO);

    // 목표에 선택한 차량 모델 반영 (CAR-API-08)
    void updateSelectedModel(CarGoalVO carGoalVO);

    // 자동차 목표 단건 조회 (소유자 검증 포함)
    CarGoalVO selectCarGoalById(@Param("goalId") Long goalId, @Param("userId") Long userId);

    // 목표 확정(완료) 처리 — status를 CONFIRMED로 전환, 영향받은 행 수 반환
    int confirmGoal(@Param("goalId") Long goalId, @Param("userId") Long userId);

    // 신차 모델 단건 조회
    CarModelVO selectCarModelById(@Param("modelId") Long modelId);

    // 차종+운전경력구간 기준 보험료 조회
    CarInsuranceVO selectInsuranceByTypeAndBracket(
            @Param("carTypeCode") Integer carTypeCode,
            @Param("experienceBracket") String experienceBracket);

    // 차종 기준 취득세율/공채면제기준 조회
    CarTaxVO selectTaxByTypeCode(@Param("carTypeCode") Integer carTypeCode);

    // 연도 기준 최대 연납할인율 조회
    CarTaxPrepayVO selectBestPrepayDiscount(@Param("year") Integer year);

    // 유저의 자동차 목표 목록 조회 (CAR-API-03)
    List<CarGoalResponseDTO> selectCarGoalsByUserId(@Param("userId") Long userId);

    // 자동차 목표 상세 조회, 선택차량명 포함 (CAR-API-04, 소유자 검증 포함)
    CarGoalResponseDTO selectCarGoalDetailById(@Param("goalId") Long goalId, @Param("userId") Long userId);

    // 전체 차량 모델 목록 조회 (CAR-API-07, 목표 단계에서 차종을 특정하지 않으므로 전체 반환)
    List<CarModelVO> selectAllCarModels();

    // 지역 기준 전기차 보조금 조회 (CAR-API-12)
    CarEvVO selectEvSubsidyByRegion(@Param("region") String region);
}
