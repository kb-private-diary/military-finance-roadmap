package org.scoula.travel.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.travel.service.TravelNotificationService;

@Log4j2
@Component
@RequiredArgsConstructor
public class TravelNotificationScheduler {
    private final TravelNotificationService service;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendUpcomingTravelNotifications() {
        try {
            this.service.sendUpcomingTravelNotifications();
        } catch (Exception exception) {
            log.error("여행 출발 알림 배치 실패", exception);
        }
    }
}
