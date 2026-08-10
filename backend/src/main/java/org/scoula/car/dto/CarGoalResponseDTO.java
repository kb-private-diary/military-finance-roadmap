package org.scoula.car.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarGoalResponseDTO {
    private Long goalId;
    private Long budget;
    private Integer carTypeCode;
    private Boolean isNew;
    private Integer experienceYears;
    private LocalDate targetDate;
    private String region;
    private Long selectedModelId;
    private String selectedModelName;
    private Integer selectedYear;
    private Integer selectedMileageKm;
    private String status;
}
