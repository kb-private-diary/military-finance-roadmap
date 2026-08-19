package org.scoula.dashboard.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

// DASH-API-08(사용내역 등록) 요청.
@Data
public class DashboardVacationUsageCreateRequestDTO {

    @NotNull(message = "사용일은 필수입니다")
    private LocalDate usedDate;

    @NotNull(message = "사용일수는 필수입니다")
    @Positive(message = "사용일수는 0보다 커야 합니다")
    private Integer days;
}
