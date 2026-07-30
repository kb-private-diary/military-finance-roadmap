package org.scoula.travel.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

import org.scoula.travel.domain.TravelGoalVO;

/**
 * 작성 중인 여행 목표를 이어쓰기 화면에 전달하는 응답 DTO.
 */
@Getter
@Builder
public class TravelGoalDraftResponseDTO {

    private final Long goalId;
    private final String title;
    private final String departure;
    private final String destination;
    private final String style;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate endDate;
    private final Long totalBudget;

    public static TravelGoalDraftResponseDTO of(final TravelGoalVO goal) {
        return TravelGoalDraftResponseDTO.builder()
                .goalId(goal.getGoalId())
                .title(goal.getTitle())
                .departure(goal.getDeparture())
                .destination(goal.getDestination())
                .style(goal.getStyle())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .totalBudget(goal.getTotalBudget())
                .build();
    }
}
