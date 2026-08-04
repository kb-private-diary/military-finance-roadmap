package org.scoula.rent.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 진행률 단계 수정 요청 DTO. (목표 상세 체크리스트에서 체크/해제)
 */
@Data
public class ProgressUpdateRequestDTO {

    @NotBlank(message = "단계 코드는 필수입니다")
    private String stepCode;

    @NotNull(message = "완료 여부는 필수입니다")
    private Boolean isCompleted;
}
