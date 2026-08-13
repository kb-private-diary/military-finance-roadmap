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
    private Boolean veteran;
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
    private Long totalContribution;
    private Long averageMonthlyContribution;
    private Double savingsCompletionRate;
    private Integer maturedAccountCount;
    private Long peerAverageTotalContribution;
    private Long peerAverageMonthlyContribution;
    private Double peerAverageCompletionRate;
}
