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
public class PrepItemRecommendResponseDTO {
    private Long goalId;

    private String goalType;

    // 자격증/어학 추천
    private List<JobQualificationDTO> qualifications;

    // 인강 추천
    private List<JobCourseDTO> courses;
}
