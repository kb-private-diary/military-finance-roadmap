package org.scoula.travel.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import org.scoula.travel.domain.CityCostVO;
import org.scoula.travel.domain.TravelCostVO;
import org.scoula.travel.domain.TravelGoalVO;

public interface TravelMapper {

    List<CityCostVO> getCityCostList(@Param("country") String country);

    // 도시명으로 물가 조회. 도착지 검증과 경비 산출에 사용.
    CityCostVO getCityCostByCity(@Param("city") String city);

    // 여행 목표 등록
    int insertGoal(TravelGoalVO vo);

    // 회원의 플랜 작성 상태를 확인한다. 플랜 중복 생성 방지용.
    int countGoalByStatus(@Param("userId") Long userId,
                          @Param("status") String status);

    // 여행 목표 단건 조회. 경비 산출의 입력값을 읽는다.
    TravelGoalVO getGoal(@Param("goalId") Long goalId);

    int insertCost(TravelCostVO vo);

    TravelCostVO getCostByGoalId(@Param("goalId") Long goalId);

    // 기존 경비 산출 결과를 소프트 삭제한다. 중복 호출 시 사용.
    int deleteCostByGoalId(@Param("goalId") Long goalId,
                           @Param("modifiedNm") String modifiedNm);

}
