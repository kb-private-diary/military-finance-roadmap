package org.scoula.rent.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

/**
 * 자취 목표 등록 요청 DTO. 화면(step1)에서 보내는 값만 담는다.
 * (goalId·status·residenceMonths·감사컬럼 등은 서버가 채우므로 여기 없음)
 *
 * <p>검증 역할 분담:
 * - @Valid(여기): "항상 필수"인 값 (selectionMode / monthlyBudget / residencePreset)
 * - Service: 모드별 조건 (SCHOOL이면 schoolId 필수 등 XOR 규칙)
 */
@Data
public class RentGoalCreateRequestDTO {

    @NotBlank(message = "위치 모드(SCHOOL/REGION)는 필수입니다")
    private String selectionMode;

    private Long schoolId;            // SCHOOL 모드일 때 (조건 검증은 Service)
    private Integer commuteRadiusKm;  // SCHOOL 모드일 때
    private List<String> regionCodes; // REGION 모드일 때

    @NotNull(message = "월예산은 필수입니다")
    @Positive(message = "월예산은 0보다 커야 합니다")
    private Long monthlyBudget;

    @NotBlank(message = "거주기간은 필수입니다")
    private String residencePreset;
}
