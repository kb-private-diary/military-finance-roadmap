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
    private Long userId;
    private Long budget;
    private Integer carTypeCode;
    private Boolean isNew;
    private LocalDate targetDate;
    private String region;
}
