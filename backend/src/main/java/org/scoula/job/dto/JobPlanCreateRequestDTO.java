package org.scoula.job.dto;

import lombok.Data;

import java.util.List;

@Data
public class JobPlanCreateRequestDTO {
    // JOB-API-04에서 추천받은 항목 중 사용자가 선택한 prep_crit_id 목록
    private List<Long> prepCritIds;
}
