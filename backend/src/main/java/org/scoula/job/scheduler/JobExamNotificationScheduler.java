package org.scoula.job.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.job.service.JobService;

@Log4j2
@Component
@RequiredArgsConstructor
public class JobExamNotificationScheduler {

    private final JobService jobService;

    // 매일 아침 선택한 자격증의 D-7/D-1/D-Day 시험 알림을 보낸다.
    @Scheduled(cron = "0 0 7 * * *")
    public void sendExamNotifications() {
        try {
            this.jobService.sendExamNotifications();
        } catch (Exception e) {
            log.error("자격증 시험 알림 배치 실패", e);
        }
    }
}
