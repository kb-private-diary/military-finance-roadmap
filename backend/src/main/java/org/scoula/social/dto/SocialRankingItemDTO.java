package org.scoula.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialRankingItemDTO {
    private Integer rank;
    private String label;
    private Double savingsRate;
    private Double metricValue;
    private Boolean me;
}
