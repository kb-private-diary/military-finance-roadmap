package org.scoula.social.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialBadgeAwardCriteriaDTO {
    private Long userId;
    private Integer badgeId;
}
