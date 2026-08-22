package org.scoula.dashboard.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class DashboardDDayUserDTO {
    private Long userId;
    private LocalDate dischargeDate;
}
