package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExamNotificationDTO {

    private Long userId;

    private String qualName;

    private LocalDate writtenExamDate;

    private LocalDate practicalExamDate;
}
