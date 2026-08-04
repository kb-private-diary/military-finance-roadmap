package org.scoula.dashboard.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

// DASH-API-06(등록)·07(수정) 공용 요청.
// category=REGULAR: name/acquiredDate/isUsed는 서버가 자동 결정하므로 안 보내도 된다
// (name="N차 정기휴가" 자동 생성, acquiredDate=오늘, isUsed=TRUE 고정).
// category≠REGULAR: name/acquiredDate/isUsed 전부 필수 — @Valid로 조건부 표현이 안 돼서
// Service에서 직접 검증한다.
@Data
public class DashboardVacationCreateRequestDTO {

    @NotBlank(message = "휴가 카테고리는 필수입니다")
    private String category;

    private String name;

    private LocalDate acquiredDate;

    @NotNull(message = "휴가일수는 필수입니다")
    @Positive(message = "휴가일수는 0보다 커야 합니다")
    private Integer days;

    private Boolean isUsed;
}
