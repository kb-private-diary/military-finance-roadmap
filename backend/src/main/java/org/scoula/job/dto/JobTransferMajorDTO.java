package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.job.domain.JobTransferMajorVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobTransferMajorDTO {

    private Long majorId;
    private Long univId;
    private String majorCode;
    private String majorName;
    private Integer admissionYear;

    public static JobTransferMajorDTO of(JobTransferMajorVO vo) {
        return JobTransferMajorDTO.builder()
                .majorId(vo.getMajorId())
                .univId(vo.getUnivId())
                .majorCode(vo.getMajorCode())
                .majorName(vo.getMajorName())
                .admissionYear(vo.getAdmissionYear())
                .build();
    }
}
