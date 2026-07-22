package org.scoula.job.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCodeDTO {
    private Long jobCodeId;
    private String goalType;
    private String codeName;
    private String infoUrl;
}
