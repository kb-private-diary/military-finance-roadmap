package org.scoula.travel.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TravelNotificationTargetDTO {
    private Long goalId;
    private Long userId;
    private String destination;
    private Boolean domestic;
    private LocalDate startDate;
}
