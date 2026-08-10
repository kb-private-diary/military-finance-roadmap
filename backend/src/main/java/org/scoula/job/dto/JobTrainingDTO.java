package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.job.domain.JobTrainingVO;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobTrainingDTO {

    private String externalCode;       // 고용24 훈련과정 ID
    private Integer trainingRound;     // 훈련회차
    private String institutionId;      // 훈련기관 ID

    private String trainingName;       // 훈련과정명
    private String institutionName;    // 훈련기관명

    private Long trainingCost;         // 전체 훈련비
    private Long selfPayment;          // 일반훈련생 기준 본인부담액

    private String address;            // 훈련지역
    private LocalDate startDate;       // 훈련시작일
    private LocalDate endDate;         // 훈련종료일

    private String detailUrl;           // 고용24 상세 URL
    private String trainingType;        // 훈련유형 (K-디지털 트레이닝 등)

    public static JobTrainingDTO of(JobTrainingVO vo) {
        return JobTrainingDTO.builder()
                .externalCode(vo.getExternalCode())
                .trainingRound(vo.getTrainingRound())
                .institutionId(vo.getInstitutionId())
                .trainingName(vo.getTrainingName())
                .institutionName(vo.getInstitutionName())
                .trainingCost(vo.getTrainingCost())
                .selfPayment(vo.getSelectedCost())
                .address(vo.getAddress())
                .startDate(vo.getStartDate())
                .endDate(vo.getEndDate())
                .detailUrl(vo.getDetailUrl())
                .trainingType(vo.getTrainingType())
                .build();
    }
}
