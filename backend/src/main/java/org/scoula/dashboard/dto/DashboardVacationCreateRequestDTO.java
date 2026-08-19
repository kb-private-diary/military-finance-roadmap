package org.scoula.dashboard.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

// DASH-API-05(등록)·06(수정) 공용 요청. REGULAR는 가입 시 서버가 자동 부여하므로
// 이 API로는 생성·수정할 수 없다(Service에서 차단).
@Data
public class DashboardVacationCreateRequestDTO {

    @NotBlank(message = "휴가 카테고리는 필수입니다")
    private String category;

    @NotBlank(message = "휴가 이름은 필수입니다")
    private String name;

    @NotNull(message = "획득일은 필수입니다")
    private LocalDate acquiredDate;

    @NotNull(message = "휴가일수는 필수입니다")
    @Positive(message = "휴가일수는 0보다 커야 합니다")
    private Integer days;
}
