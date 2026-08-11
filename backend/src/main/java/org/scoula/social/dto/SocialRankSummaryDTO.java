package org.scoula.social.dto;

import lombok.Data;

@Data
public class SocialRankSummaryDTO {
    private Integer rank;
    private Integer totalCount;
    private Integer higherCount;
    private Integer lowerCount;
}
