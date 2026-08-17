package org.scoula.dashboard.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.dashboard.service.DashboardService;

// 매일 아침 전역 D-30/D-1인 유저에게 웹푸시를 보낸다.
@Log4j2
@Component
@RequiredArgsConstructor
public class DDayNotificationScheduler {

    private final DashboardService dashboardService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDDayNotifications() {
        try {
            this.dashboardService.sendDDayNotifications();
        } catch (Exception e) {
            log.error("D-Day 알림 배치 실패", e);
        }
    }
}
