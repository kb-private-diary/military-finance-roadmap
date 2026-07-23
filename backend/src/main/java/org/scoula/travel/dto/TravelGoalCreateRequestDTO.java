package org.scoula.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 여행 목표 등록 요청 DTO
// Jackson 2.9.4에 LocalDate를 직접 바인딩 하지 못해 날짜는 문자열로 받는다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelGoalCreateRequestDTO {

    private String title;
    private String departure;
    private String destination;
    private Boolean isDomestic;
    private String style;
    private String startDate;
    private String endDate;
    private Long totalBudget;
}
