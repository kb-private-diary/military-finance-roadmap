package org.scoula.social.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.social.mapper.SocialMapper;

@Log4j2
@Service
@RequiredArgsConstructor
public class SocialNotificationServiceImpl implements SocialNotificationService {
    private final SocialMapper mapper;
    private final SocialBadgeAwardService badgeAwardService;

    @Override
    public void sendNewBadgeNotifications() {
        final List<Long> userIds = this.mapper.findNotificationTargetUserIdList();
        int awardedCount = 0;

        for (Long userId : userIds) {
            try {
                awardedCount += this.badgeAwardService.awardEarnedBadges(userId);
            } catch (Exception exception) {
                log.error("신규 뱃지 알림 처리 실패 - userId: {}", userId, exception);
            }
        }

        log.info("신규 뱃지 알림 배치 완료 - 대상 {}명, 신규 지급 {}건",
                userIds.size(), awardedCount);
    }
}
