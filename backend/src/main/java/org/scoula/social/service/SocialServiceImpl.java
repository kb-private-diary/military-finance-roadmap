package org.scoula.social.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import org.scoula.common.exception.BusinessException;
import org.scoula.social.dto.SocialBadgeItemDTO;
import org.scoula.social.dto.SocialDistributionItemDTO;
import org.scoula.social.dto.SocialRankSummaryDTO;
import org.scoula.social.dto.SocialRankingResponseDTO;
import org.scoula.social.dto.SocialScopeCriteriaDTO;
import org.scoula.social.dto.SocialStatsResponseDTO;
import org.scoula.social.dto.SocialUserContextDTO;
import org.scoula.social.dto.SocialVeteranStatsDTO;
import org.scoula.social.mapper.SocialMapper;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {
    private static final String SCOPE_ALL = "ALL";
    private static final String SCOPE_TYPE = "TYPE";
    private static final String SCOPE_UNIT = "UNIT";

    // 하단 랭킹은 탭과 무관하게 항상 전체 부대 평균을 보여준다
    private static final String RANKING_TITLE = "전체 평균 저축률 TOP 3";
    private static final String VETERAN_RANKING_TITLE =
            "전역자 부대 평균 완주율 TOP 3";

    private final SocialMapper mapper;
    private final SocialBadgeAwardService badgeAwardService;

    @Override
    @Transactional(readOnly = true)
    public SocialStatsResponseDTO findStats(final Long userId, final String scope) {
        final SocialUserContextDTO context = this.findUserContext(userId);
        final SocialScopeCriteriaDTO criteria = this.createCriteria(context, scope);
        if (this.isVeteran(context)) {
            return this.findVeteranStats(context, criteria);
        }
        final SocialRankSummaryDTO rankSummary =
                this.mapper.findSavingsRank(criteria);

        return SocialStatsResponseDTO.builder()
                .veteran(false)
                .name(context.getName())
                .rankName(context.getRankName())
                .typeName(context.getTypeName())
                .unitName(context.getUnitName())
                .currentSavings(context.getCurrentSavings())
                .savingsRate(context.getSavingsRate())
                .averageSavingsRate(this.mapper.findAverageSavingsRate(criteria))
                .savingsRank(rankSummary != null ? rankSummary.getRank() : 1)
                .comparisonMemberCount(rankSummary != null ? rankSummary.getTotalCount() : 1)
                .higherSavingsCount(rankSummary != null ? rankSummary.getHigherCount() : 0)
                .lowerSavingsCount(rankSummary != null ? rankSummary.getLowerCount() : 0)
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
        final SocialUserContextDTO context = this.findUserContext(userId);
        final SocialScopeCriteriaDTO criteria = this.createCriteria(context, scope);
        if (this.isVeteran(context)) {
            return SocialRankingResponseDTO.builder()
                    .title(VETERAN_RANKING_TITLE)
                    .rankings(this.mapper.findVeteranUnitRankingList(criteria))
                    .myUnitRank(this.mapper.findVeteranUnitRank(criteria))
                    .build();
        }
        final Integer unitRank = this.mapper.findUnitRank(criteria);

        return SocialRankingResponseDTO.builder()
                .title(RANKING_TITLE)
                .rankings(this.mapper.findUnitRankingList(criteria))
                .myUnitRank(unitRank)
                .build();
    }

    private SocialStatsResponseDTO findVeteranStats(
            final SocialUserContextDTO context,
            final SocialScopeCriteriaDTO criteria) {
        final SocialVeteranStatsDTO mappedStats =
                this.mapper.findVeteranStats(criteria);
        final SocialVeteranStatsDTO veteranStats = mappedStats != null
                ? mappedStats
                : new SocialVeteranStatsDTO();

        return SocialStatsResponseDTO.builder()
                .veteran(true)
                .name(context.getName())
                .rankName(context.getRankName())
                .typeName(context.getTypeName())
                .unitName(context.getUnitName())
                .currentSavings(this.valueOrZero(veteranStats.getTotalContribution()))
                .savingsRank(this.valueOrDefault(veteranStats.getRank(), 1))
                .comparisonMemberCount(
                        this.valueOrDefault(veteranStats.getTotalCount(), 1))
                .higherSavingsCount(
                        this.valueOrDefault(veteranStats.getHigherCount(), 0))
                .lowerSavingsCount(
                        this.valueOrDefault(veteranStats.getLowerCount(), 0))
                .totalContribution(
                        this.valueOrZero(veteranStats.getTotalContribution()))
                .averageMonthlyContribution(this.valueOrZero(
                        veteranStats.getAverageMonthlyContribution()))
                .savingsCompletionRate(this.valueOrZero(
                        veteranStats.getSavingsCompletionRate()))
                .maturedAccountCount(this.valueOrDefault(
                        veteranStats.getMaturedAccountCount(), 0))
                .peerAverageTotalContribution(this.valueOrZero(
                        veteranStats.getPeerAverageTotalContribution()))
                .peerAverageMonthlyContribution(this.valueOrZero(
                        veteranStats.getPeerAverageMonthlyContribution()))
                .peerAverageCompletionRate(this.valueOrZero(
                        veteranStats.getPeerAverageCompletionRate()))
                .build();
    }

    // 조회 시점에 획득 조건을 만족한 뱃지를 지급한 뒤 전체 목록을 반환.
    @Override
    @Transactional
    public List<SocialBadgeItemDTO> findBadgeList(final Long userId) {
        this.findUserContext(userId);
        this.badgeAwardService.awardEarnedBadges(userId);
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
                .veteran(this.isVeteran(context))
                .build();
    }

    private boolean isVeteran(final SocialUserContextDTO context) {
        return context.getDischargeDate() != null
                && context.getDischargeDate().isBefore(LocalDate.now());
    }

    private Long valueOrZero(final Long value) {
        return value != null ? value : 0L;
    }

    private Double valueOrZero(final Double value) {
        return value != null ? value : 0.0;
    }

    private Integer valueOrDefault(final Integer value, final int fallback) {
        return value != null ? value : fallback;
    }
}
