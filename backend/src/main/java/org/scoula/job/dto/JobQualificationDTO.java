package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.job.domain.JobQualificationVO;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobQualificationDTO {

    private Long qualId;
    private String qualType;
    private String qualName;
    private String qualSummary;
    private String organizationName;
    private Long writtenFee;
    private Long practicalFee;
    private Long militaryFee;
    private Long selectedCost;
    private String detailUrl;

    private Integer examYear;
    private String examRound;

    private LocalDate writtenRegStartDate;
    private LocalDate writtenRegEndDate;
    private LocalDate writtenExamStartDate;
    private LocalDate writtenExamEndDate;
    private LocalDate writtenResultDate;

    private LocalDate practicalRegStartDate;
    private LocalDate practicalRegEndDate;
    private LocalDate practicalExamStartDate;
    private LocalDate practicalExamEndDate;
    private LocalDate practicalResultDate;

    public static JobQualificationDTO of(JobQualificationVO vo) {
        return JobQualificationDTO.builder()
                .qualId(vo.getQualId())
                .qualType(vo.getQualType())
                .qualName(vo.getQualName())
                .qualSummary(vo.getQualSummary())
                .organizationName(vo.getOrganizationName())
                .writtenFee(vo.getWrittenFee())
                .practicalFee(vo.getPracticalFee())
                .militaryFee(vo.getMilitaryFee())
                .selectedCost(vo.getSelectedCost())
                .detailUrl(vo.getDetailUrl())

                .examYear(vo.getExamYear())
                .examRound(vo.getExamRound())

                .writtenRegStartDate(vo.getWrittenRegStartDate())
                .writtenRegEndDate(vo.getWrittenRegEndDate())
                .writtenExamStartDate(vo.getWrittenExamStartDate())
                .writtenExamEndDate(vo.getWrittenExamEndDate())
                .writtenResultDate(vo.getWrittenResultDate())

                .practicalRegStartDate(vo.getPracticalRegStartDate())
                .practicalRegEndDate(vo.getPracticalRegEndDate())
                .practicalExamStartDate(vo.getPracticalExamStartDate())
                .practicalExamEndDate(vo.getPracticalExamEndDate())
                .practicalResultDate(vo.getPracticalResultDate())

                .build();
    }
}
