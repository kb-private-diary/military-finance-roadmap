package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobGoalCreateRequestDTO {
    private Long userId;
    private String goalType;
    private Long jobCodeId;
    private String expectedDate;
}
