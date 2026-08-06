package org.scoula.job.dto;

import lombok.Data;

import java.util.List;

@Data
public class JobPlanCreateRequestDTO {
    // 사용자가 선택한 자격증·어학 ID 목록
    private List<Long> qualIds;

    // 사용자가 선택한 인강 ID 목록
    private List<Long> courseIds;
}
