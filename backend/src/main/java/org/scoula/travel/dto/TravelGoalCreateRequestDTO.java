package org.scoula.travel.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private Long totalBudget;
}
