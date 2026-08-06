package org.scoula.car.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarGoalCreateRequestDTO {
    private Long budget;
    private Boolean isNew;
    private Integer experienceYears;
    private LocalDate targetDate;
    private String region;
}
