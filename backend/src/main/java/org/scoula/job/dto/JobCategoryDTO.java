package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.job.domain.JobCategoryVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCategoryDTO {
    private Long categoryId;
    private Long parentId;
    private String goalType;
    private String categoryName;
    private Integer categoryLevel;

    public static JobCategoryDTO of(JobCategoryVO vo) {
        return JobCategoryDTO.builder()
                .categoryId(vo.getCategoryId())
                .parentId(vo.getParentId())
                .goalType(vo.getGoalType())
                .categoryName(vo.getCategoryName())
                .categoryLevel(vo.getCategoryLevel())
                .build();
    }
}
