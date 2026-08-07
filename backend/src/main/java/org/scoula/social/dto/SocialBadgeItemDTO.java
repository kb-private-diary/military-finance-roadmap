package org.scoula.social.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SocialBadgeItemDTO {
    private Integer badgeId;
    private String badgeName;
    private Boolean achieved;
    private LocalDateTime achievedDate;
}
