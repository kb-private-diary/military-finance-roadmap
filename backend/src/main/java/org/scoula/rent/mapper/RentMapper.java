package org.scoula.rent.mapper;

import java.util.List;

import org.scoula.rent.domain.RegionCodeVO;
import org.scoula.rent.domain.RentGoalRegionVO;
import org.scoula.rent.domain.RentGoalVO;

public interface RentMapper {

    // ---------- 지역 조회 ----------
    List<RegionCodeVO> findSidoList();
    List<RegionCodeVO> findSigunguListBySido(String sidoName);
    List<RegionCodeVO> findUmdListBySigunguCode(String sigunguCode);

    // ---------- 월세 목표 CRUD ----------
    void insertRentGoal(RentGoalVO rentGoalVO);
    RentGoalVO findRentGoalById(Long goalId);
    RentGoalVO findCurrentRentGoalByUserId(Long userId);
    RentGoalVO findDraftRentGoalByUserId(Long userId);
    void softDeleteRentGoal(RentGoalVO rentGoalVO);

    // ---------- 희망 지역 ----------
    void insertGoalRegion(RentGoalRegionVO regionVO);
    void softDeleteRegionsByGoalId(RentGoalRegionVO regionVO);
}
