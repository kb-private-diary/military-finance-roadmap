package org.scoula.rent.service;

import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.SchoolSearchResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.RentListingResponseDTO;
import org.scoula.rent.dto.RentListingDetailResponseDTO;
import org.scoula.rent.dto.RentCostResponseDTO;
import org.scoula.rent.dto.RentAffordabilityResponseDTO;
import java.util.List;

public interface RentService {
    List<RegionResponseDTO> findRegions(String sido, String sigunguCode);

    // 자취 목표 등록 → 생성된 goalId 반환
    Long createGoal(RentGoalCreateRequestDTO request, Long userId);

    // 학교 검색 (자동완성)
    List<SchoolSearchResponseDTO> findSchools(String keyword);

    // 목표 상세 조회 (목표 정보)
    RentGoalDetailResponseDTO findGoal(Long goalId);

    // 조건 매칭 매물 리스트 (Step2)
    List<RentListingResponseDTO> findListings(Long goalId);

    // 매물 상세 (Step3)
    RentListingDetailResponseDTO findListingDetail(Long listingId);

    // 총 필요자금 계산 (보증금 + 월세×거주개월)
    RentCostResponseDTO calculateCost(Long listingId, int months);

    // 진행중(DRAFT) 목표 조회 (없으면 null)
    RentGoalDetailResponseDTO findCurrentGoal(Long userId);

    // 목표 삭제 (soft delete)
    void deleteGoal(Long goalId);

    // 부족분 계산 (Step4) - 총 필요자금 vs 만기금 → 부족분·감당도 판정
    RentAffordabilityResponseDTO findAffordability(Long listingId, Long userId, int months);

    // 로드맵 저장 (Step4 저장 버튼) - rent_goal 상태 DRAFT → CONFIRMED 확정
    void confirmGoal(Long goalId, Long userId, Integer months);
}
