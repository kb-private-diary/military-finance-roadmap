package org.scoula.social.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import org.scoula.push.service.PushNotificationService;
import org.scoula.social.dto.SocialBadgeAwardCriteriaDTO;
import org.scoula.social.mapper.SocialMapper;

@Service
@RequiredArgsConstructor
public class SocialBadgeAwardService {
    private static final String CATEGORY_BADGE = "BADGE";
    private static final String SOCIAL_URL = "/social";

    private final SocialMapper mapper;
    private final PushNotificationService pushNotificationService;

    @Transactional
    public synchronized int awardEarnedBadges(final Long userId) {
        final List<Integer> earnedBadgeIds =
                this.mapper.findEarnedBadgeIdList(userId);
        int awardedCount = 0;

        for (Integer badgeId : earnedBadgeIds) {
            final int inserted = this.mapper.insertBadge(
                    new SocialBadgeAwardCriteriaDTO(userId, badgeId));
            if (inserted == 0) {
                continue;
            }

            final String badgeName = this.mapper.getBadgeName(badgeId);
            this.pushNotificationService.send(
                    userId,
                    "새로운 뱃지를 획득했어요!",
                    "'" + badgeName + "' 뱃지를 획득했습니다.",
                    CATEGORY_BADGE,
                    SOCIAL_URL);
            awardedCount++;
        }
        return awardedCount;
    }
}
