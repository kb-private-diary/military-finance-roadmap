package org.scoula.social.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import org.scoula.common.exception.BusinessException;
import org.scoula.social.dto.SocialBadgeAwardCriteriaDTO;
import org.scoula.social.dto.SocialBadgeItemDTO;
import org.scoula.social.dto.SocialDistributionItemDTO;
import org.scoula.social.dto.SocialRankSummaryDTO;
import org.scoula.social.dto.SocialRankingResponseDTO;
import org.scoula.social.dto.SocialScopeCriteriaDTO;
import org.scoula.social.dto.SocialStatsResponseDTO;
import org.scoula.social.dto.SocialUserContextDTO;
import org.scoula.social.mapper.SocialMapper;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {
    private static final String SCOPE_ALL = "ALL";
    private static final String SCOPE_TYPE = "TYPE";
    private static final String SCOPE_UNIT = "UNIT";

    // 하단 랭킹은 탭과 무관하게 항상 전체 부대 평균을 보여준다
    private static final String RANKING_TITLE = "전체 평균 저축률 TOP 3";

    private final SocialMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public SocialStatsResponseDTO findStats(final Long userId, final String scope) {
        SocialUserContextDTO context = this.findUserContext(userId);
        SocialScopeCriteriaDTO criteria = this.createCriteria(context, scope);
        SocialRankSummaryDTO rankSummary = this.mapper.findSavingsRank(criteria);

        return SocialStatsResponseDTO.builder()
                .name(context.getName())
                .rankName(context.getRankName())
                .typeName(context.getTypeName())
                .unitName(context.getUnitName())
                .currentSavings(context.getCurrentSavings())
                .savingsRate(context.getSavingsRate())
                .averageSavingsRate(this.mapper.findAverageSavingsRate(criteria))
                .savingsRank(rankSummary != null ? rankSummary.getRank() : 1)
                .comparisonMemberCount(rankSummary != null ? rankSummary.getTotalCount() : 1)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocialDistributionItemDTO> findDistributionList(
            final Long userId, final String scope) {
        SocialScopeCriteriaDTO criteria = this.createCriteria(
                this.findUserContext(userId), scope);
        List<SocialDistributionItemDTO> items = this.mapper.findDistributionList(criteria);
        long total = items.stream()
                .mapToLong(item -> item.getInterestCount() != null
                        ? item.getInterestCount() : 0L)
                .sum();

        for (SocialDistributionItemDTO item : items) {
            double percentage = total == 0L
                    ? 0.0
                    : Math.round((item.getInterestCount() * 1000.0) / total) / 10.0;
            item.setPercentage(percentage);
        }
        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public SocialRankingResponseDTO findRanking(final Long userId, final String scope) {
        // scope 는 결과를 가르지 않지만, 잘못된 값을 걸러낸다.
        SocialScopeCriteriaDTO criteria = this.createCriteria(
                this.findUserContext(userId), scope);

        return new SocialRankingResponseDTO(
                RANKING_TITLE, this.mapper.findUnitRankingList(criteria));
    }

    // 조회 시점에 획득 조건을 만족한 뱃지를 지급한 뒤 전체 목록을 반환.
    @Override
    @Transactional
    public List<SocialBadgeItemDTO> findBadgeList(final Long userId) {
        this.findUserContext(userId);
        for (Integer badgeId : this.mapper.findEarnedBadgeIdList(userId)) {
            this.mapper.insertBadge(new SocialBadgeAwardCriteriaDTO(userId, badgeId));
        }
        return this.mapper.findBadgeListByUserId(userId);
    }

    private SocialUserContextDTO findUserContext(final Long userId) {
        SocialUserContextDTO context = this.mapper.findUserContext(userId);
        if (context == null) {
            throw BusinessException.notFound(
                    "사용자 정보를 찾을 수 없습니다.", "SOCIAL_001");
        }
        return context;
    }

    private SocialScopeCriteriaDTO createCriteria(
            final SocialUserContextDTO context, final String requestedScope) {
        String scope = requestedScope == null
                ? SCOPE_ALL
                : requestedScope.toUpperCase(Locale.ROOT);
        if (!SCOPE_ALL.equals(scope)
                && !SCOPE_TYPE.equals(scope)
                && !SCOPE_UNIT.equals(scope)) {
            throw BusinessException.badRequest(
                    "지원하지 않는 비교 범위입니다.", "SOCIAL_002");
        }

        return SocialScopeCriteriaDTO.builder()
                .userId(context.getUserId())
                .scope(scope)
                .typeId(context.getTypeId())
                .unitCode(context.getUnitCode())
                .rankId(context.getRankId())
                .build();
    }
}
