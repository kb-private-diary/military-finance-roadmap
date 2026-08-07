package org.scoula.rent.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.RentGoalVO;

/**
 * 자취 목표 상세 응답 DTO
 * DRAFT면 목표 기본 정보만, Step5(CONFIRMED + 매물 확정)면 확정매물·편의시설·정밀시뮬레이션까지
 */
@Data
@Builder(toBuilder = true)
public class RentGoalDetailResponseDTO {
    private Long goalId;
    private String title;
    private String selectionMode;
    private Long schoolId;
    private Integer commuteRadiusKm;
    private Long monthlyBudget;
    private String residencePreset;
    private Integer residenceMonths;
    private String status;

    // --- Step5 저장 후 상세 (매물 확정 시에만 채워짐, DRAFT면 null) ---
    private ListingSummaryDTO listing;                 // 확정 매물 (지번·좌표)
    private List<NearbyFacilityDTO> nearbyFacilities;  // 주변 편의시설 (카카오 로컬)
    private PrecisionSimulationDTO precisionSimulation; // 진짜 정밀 시뮬레이션

    public static RentGoalDetailResponseDTO of(RentGoalVO goal) {
        return RentGoalDetailResponseDTO.builder()
                .goalId(goal.getGoalId())
                .title(goal.getTitle())
                .selectionMode(goal.getSelectionMode())
                .schoolId(goal.getSchoolId())
                .commuteRadiusKm(goal.getCommuteRadiusKm())
                .monthlyBudget(goal.getMonthlyBudget())
                .residencePreset(goal.getResidencePreset())
                .residenceMonths(goal.getResidenceMonths())
                .status(goal.getStatus())
                .build();
    }
}
