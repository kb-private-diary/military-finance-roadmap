package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPlanCreateResponseDTO {
    private Long goalId;
    private List<JobQualificationDTO> qualifications;
    private List<JobCourseDTO> courses;
    private List<JobTrainingDTO> trainings;
}
