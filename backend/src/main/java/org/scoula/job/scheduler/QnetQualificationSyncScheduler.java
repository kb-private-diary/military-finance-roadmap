package org.scoula.job.scheduler;

import org.scoula.job.service.QnetQualificationSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// 매일 새벽 4시 20분에 Q-Net API에서 국가자격증 시험정보를 가져와 DB에 반영한다.
@Log4j2
@Component
@RequiredArgsConstructor
public class QnetQualificationSyncScheduler {

    private final QnetQualificationSyncService qnetQualificationSyncService;

    @Scheduled(cron = "0 20 4 * * *")
    public void syncQualifications() {
        try {
            qnetQualificationSyncService.syncQualifications();
        } catch (Exception e) {
            log.error("Q-Net 자격증 정보 정기 동기화 실패", e);
        }
    }
}