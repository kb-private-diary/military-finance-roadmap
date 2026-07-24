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
    private String goalType;
    private Long jobCodeId;
    private String expectedDate;
    // 준비 항목 복수 선택값 (P01/P02/P03), job_interested_type에 각 한 줄씩 저장
    private List<String> itemTypes;
}
