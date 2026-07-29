package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.job.domain.JobPlanVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPlanItemDTO {
    private String itemType;
    private String itemName;
    private Long amount;

    public static JobPlanItemDTO of(JobPlanVO jobPlanVO) {
        return JobPlanItemDTO.builder()
                .itemType(jobPlanVO.getItemType())
                .itemName(jobPlanVO.getItemName())
                .amount(jobPlanVO.getAmount())
                .build();
    }
}
