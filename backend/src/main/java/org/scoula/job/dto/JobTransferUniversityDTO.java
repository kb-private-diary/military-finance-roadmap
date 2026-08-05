package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.job.domain.JobTransferUniversityVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobTransferUniversityDTO {

    private Long univId;
    private String univName;

    public static JobTransferUniversityDTO of(JobTransferUniversityVO vo) {
        JobTransferUniversityDTO dto = new JobTransferUniversityDTO();
        dto.setUnivId(vo.getUnivId());
        dto.setUnivName(vo.getUnivName());
        return dto;
    }
}
