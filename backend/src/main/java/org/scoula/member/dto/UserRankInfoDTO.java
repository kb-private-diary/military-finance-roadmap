package org.scoula.member.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserRankInfoDTO {
    private Long id;
    private LocalDate enlistDate;
    private Integer rankId;
}
