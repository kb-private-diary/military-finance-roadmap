package org.scoula.rent.mapper;

import org.scoula.rent.domain.RegionCodeVO;
import org.scoula.rent.domain.RentGoalVO;
import java.util.List;

public interface RentMapper {
    // 시/도 목록 (중복 제거)
    List<RegionCodeVO> findSidoList();

    // 특정 시/도의 시/군/구 목록
    List<RegionCodeVO> findSigunguListBySido(String sidoName);

    // 특정 시/군/구의 읍/면/동 목록
    List<RegionCodeVO> findUmdListBySigunguCode(String sigunguCode);

    // 자취 목표 등록 (INSERT 후 생성된 goalId 가 goal 객체에 채워짐)
    void insertGoal(RentGoalVO goal);
}