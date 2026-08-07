package org.scoula.main.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainUpcomingScheduleDTO {

    // 일정 카테고리: TRAVEL 또는 JOB
    private String category;

    // 여행명 또는 자격증명
    private String title;

    // 여행 출발일 또는 자격증 필기시험 시작일
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    // 오늘을 기준으로 해당 일정까지 남은 일수
    private Long dDay;
}
