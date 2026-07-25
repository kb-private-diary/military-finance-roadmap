package org.scoula.rent.service;

import java.util.List;

import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.RentGoalCreateResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.RentGoalRegionCreateRequestDTO;

public interface RentService {

    // 지역 3단계 조회
    List<RegionResponseDTO> findRegions(String sido, String sigunguCode);

    // 월세 목표 CRUD
    RentGoalCreateResponseDTO createRentGoal(Long userId, RentGoalCreateRequestDTO request);
    RentGoalDetailResponseDTO findCurrentRentGoal(Long userId);
    RentGoalDetailResponseDTO findRentGoal(Long goalId, Long userId);
    void deleteRentGoal(Long goalId, Long userId);

    // 희망 지역 등록 (최대 5개, 재등록 시 대체)
    void addGoalRegions(Long goalId, Long userId, RentGoalRegionCreateRequestDTO request);
}
