package org.scoula.rent.dto;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.RentGoalVO;

/**
 * 자취 목표 상세 응답 DTO - 목표 정보
 * (매물·대출 스냅샷은 매물/대출 저장 기능 추가 후 확장 예정)
 */
@Data
@Builder
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
