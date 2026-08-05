package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobGoalCreateRequestDTO {
    private Long userId;

    // J01: 취업, J02: 공무원, J03: 편입
    private String goalType;

    // 취업 / 공무원
    private Long categoryId;

    // 편입
    private Long univId;
    private Long majorId;

    // YYYY-MM
    private String expectedDate;
}
