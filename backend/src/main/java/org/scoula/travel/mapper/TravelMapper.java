package org.scoula.travel.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import org.scoula.travel.domain.CityCostVO;
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
}
