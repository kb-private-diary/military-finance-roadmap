package org.scoula.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialStatsResponseDTO {
    private String name;
    private String rankName;
    private String typeName;
    private String unitName;
    private Long currentSavings;
    private Double savingsRate;
    private Double averageSavingsRate;
    private Integer savingsRank;
    private Integer comparisonMemberCount;
    private Integer higherSavingsCount;
    private Integer lowerSavingsCount;
}
