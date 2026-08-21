package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobGoalDetailResponseDTO {

    private Long goalId;
    private String goalType;

    private Long categoryId;
    private String categoryName;

    private Long univId;
    private String univName;

    private Long majorId;
    private String majorName;

    private String expectedDate;
    private String status;

    private List<JobQualificationDTO> qualifications;
    private List<JobCourseDTO> courses;
    private List<JobTrainingDTO> trainings;

    private List<JobProductDTO> policies;
    private List<JobProductDTO> financialProducts;

    // 최근 3개월 월평균 후회소비(원) - 준비비용 활용 분석 카드에서 사용
    private Long avgRegretSpending;
}
