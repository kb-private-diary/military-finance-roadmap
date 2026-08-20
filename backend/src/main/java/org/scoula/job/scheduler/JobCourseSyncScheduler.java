package org.scoula.job.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.job.service.JobCourseSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 매일 새벽 4시에 인강 정보를 가져와 DB에 반영한다.
@Log4j2
@Component
@RequiredArgsConstructor
public class JobCourseSyncScheduler {

    private final JobCourseSyncService jobCourseSyncService;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void syncCourses() {
        try {
            jobCourseSyncService.syncAllCourses();
        } catch (Exception e) {
            log.error("인강 정보 정기 동기화 실패", e);
        }
    }
}