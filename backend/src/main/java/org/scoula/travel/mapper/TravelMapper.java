package org.scoula.travel.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.travel.domain.CityCostVO;

import java.util.List;

/**
 여행 도메인 MyBatis 매퍼
 */
public interface TravelMapper {

    List<CityCostVO> getCityCostList(@Param("country") String country);

    CityCostVO getCityCost(@Param("cityCostId") Long cityCostId);
}