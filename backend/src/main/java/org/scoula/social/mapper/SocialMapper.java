package org.scoula.social.mapper;

import java.util.List;

import org.scoula.social.dto.SocialBadgeAwardCriteriaDTO;
import org.scoula.social.dto.SocialBadgeItemDTO;
import org.scoula.social.dto.SocialDistributionItemDTO;
import org.scoula.social.dto.SocialRankSummaryDTO;
import org.scoula.social.dto.SocialRankingItemDTO;
import org.scoula.social.dto.SocialScopeCriteriaDTO;
import org.scoula.social.dto.SocialUserContextDTO;
import org.scoula.social.dto.SocialVeteranStatsDTO;

public interface SocialMapper {
    SocialUserContextDTO findUserContext(Long userId);

    Double findAverageSavingsRate(SocialScopeCriteriaDTO criteria);

    SocialRankSummaryDTO findSavingsRank(SocialScopeCriteriaDTO criteria);

    SocialVeteranStatsDTO findVeteranStats(SocialScopeCriteriaDTO criteria);

    // 로드맵 카테고리별로 플랜을 저장한 회원 수. 저장 기준은 CONFIRMED·ARCHIVED 이며
    // 한 회원이 같은 카테고리에 여러 건을 저장해도 1명으로 센다.
    List<SocialDistributionItemDTO> findDistributionList(SocialScopeCriteriaDTO criteria);

    // 부대별 평균 저축률 TOP 3. scope·계급과 무관하게 항상 전체 부대가 대상
    List<SocialRankingItemDTO> findUnitRankingList(SocialScopeCriteriaDTO criteria);

    Integer findUnitRank(SocialScopeCriteriaDTO criteria);

    List<SocialRankingItemDTO> findVeteranUnitRankingList(
            SocialScopeCriteriaDTO criteria);

    Integer findVeteranUnitRank(SocialScopeCriteriaDTO criteria);

    List<SocialBadgeItemDTO> findBadgeListByUserId(Long userId);

    List<Integer> findEarnedBadgeIdList(Long userId);

    int insertBadge(SocialBadgeAwardCriteriaDTO criteria);
}
