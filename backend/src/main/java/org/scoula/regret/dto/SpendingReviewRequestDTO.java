package org.scoula.regret.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 지출 회고 태깅 요청 DTO
 * 화면에서 지출 하나를 만족/후회로 태깅할 때 보내는 값
 */
@Data
public class SpendingReviewRequestDTO {

    @NotNull(message = "지출을 선택해주세요.")
    private Long spendingId;      // 태깅할 지출번호

    @NotBlank(message = "만족/후회를 선택해주세요.")
    private String reviewType;    // SATISFIED / REGRET (Service에서 enum 검증)
}
