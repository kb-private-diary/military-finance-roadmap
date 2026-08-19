package org.scoula.social.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.social.service.SocialNotificationService;

@Log4j2
@Component
@RequiredArgsConstructor
public class SocialBadgeNotificationScheduler {
    private final SocialNotificationService service;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendNewBadgeNotifications() {
        try {
            this.service.sendNewBadgeNotifications();
        } catch (Exception exception) {
            log.error("신규 뱃지 알림 배치 실패", exception);
        }
    }
}
