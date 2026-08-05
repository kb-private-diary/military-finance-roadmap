package org.scoula.saving.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 군적금 계좌 등록 요청. 오픈뱅킹 연동 시 연동된 적금계좌 정보로도 이 DTO를 채워 등록한다.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingAccountCreateRequestDTO {

    @NotBlank(message = "은행코드는 필수입니다")
    private String bankCode;

    @NotNull(message = "월 납입액은 필수입니다")
    @PositiveOrZero(message = "월 납입액은 0 이상이어야 합니다")
    private Long monthlySave;

    @NotNull(message = "누적납입금은 필수입니다")
    @PositiveOrZero(message = "누적납입금은 0 이상이어야 합니다")
    private Long currAmount;
}
