package org.scoula.social.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialRankingResponseDTO {
    private String title;
    private List<SocialRankingItemDTO> rankings;
    private Integer myUnitRank;
}
