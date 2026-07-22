package org.scoula.job.domain;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class JobCodeVO {
    private Long jobCodeId;
    private String goalType;
    private String codeName;
    private String infoUrl;
}
