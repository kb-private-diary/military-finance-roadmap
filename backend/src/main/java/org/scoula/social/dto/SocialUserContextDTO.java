package org.scoula.social.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class SocialUserContextDTO {
    private Long userId;
    private String name;
    private Integer rankId;
    private String rankName;
    private Integer typeId;
    private String typeName;
    private String unitName;
    private String unitCode;
    private LocalDate dischargeDate;
    private Long currentSavings;
    private Double savingsRate;
}
