package org.scoula.social.service;

import java.util.List;

import org.scoula.social.dto.SocialBadgeItemDTO;
import org.scoula.social.dto.SocialDistributionItemDTO;
import org.scoula.social.dto.SocialRankingResponseDTO;
import org.scoula.social.dto.SocialStatsResponseDTO;

public interface SocialService {
    SocialStatsResponseDTO findStats(Long userId, String scope);

    List<SocialDistributionItemDTO> findDistributionList(Long userId, String scope);

    SocialRankingResponseDTO findRanking(Long userId, String scope);

    List<SocialBadgeItemDTO> findBadgeList(Long userId);
}
