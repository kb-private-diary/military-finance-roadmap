package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobGoalCreateRequestDTO {

    // J01: 취업, J02: 공무원, J03: 편입
    @NotBlank(message = "목표 유형은 필수입니다")
    private String goalType;

    // 취업 / 공무원
    private Long categoryId;

    // 편입
    private Long univId;
    private Long majorId;

    // YYYY-MM
    @NotBlank(message = "목표 예상 시기는 필수입니다")
    private String expectedDate;
}
