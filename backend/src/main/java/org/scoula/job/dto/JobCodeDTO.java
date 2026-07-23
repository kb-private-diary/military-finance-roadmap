package org.scoula.job.dto;

import lombok.*;
import org.scoula.job.domain.JobCodeVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCodeDTO {
    private Long jobCodeId;
    private String goalType;
    private String codeName;
    private String infoUrl;

    public static JobCodeDTO of(JobCodeVO vo) {
        return JobCodeDTO.builder()
                .jobCodeId(vo.getJobCodeId())
                .goalType(vo.getGoalType())
                .codeName(vo.getCodeName())
                .infoUrl(vo.getInfoUrl())
                .build();
    }
}
