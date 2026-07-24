package org.scoula.travel.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 여행 목표 등록 요청 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelGoalCreateRequestDTO {

    private String title;
    private String departure;
    private String destination;
    private String style;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalBudget;
}
