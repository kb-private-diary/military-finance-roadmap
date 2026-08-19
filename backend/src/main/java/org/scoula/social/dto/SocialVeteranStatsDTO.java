package org.scoula.social.dto;

import lombok.Data;

@Data
public class SocialVeteranStatsDTO {
    private Long totalContribution;
    private Long averageMonthlyContribution;
    private Double savingsCompletionRate;
    private Integer maturedAccountCount;
    private Long peerAverageTotalContribution;
    private Long peerAverageMonthlyContribution;
    private Double peerAverageCompletionRate;
    private Integer rank;
    private Integer totalCount;
    private Integer higherCount;
    private Integer lowerCount;
}
