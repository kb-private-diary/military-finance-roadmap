package org.scoula.rent.service;

import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.SchoolSearchResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.RentListingResponseDTO;
import org.scoula.rent.dto.RentListingDetailResponseDTO;
import org.scoula.rent.dto.RentCostResponseDTO;
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
}
