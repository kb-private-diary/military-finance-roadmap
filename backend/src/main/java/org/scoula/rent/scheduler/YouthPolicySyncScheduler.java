package org.scoula.rent.scheduler;

import org.scoula.rent.service.YouthPolicySyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 온통청년 청년 주거정책 정기 동기화 스케줄러 (연동명세 §5 - 1일 1회 새벽).
 * 매일 04:10 에 배치를 실행한다. (ProductSyncScheduler 와 시간대를 겹치지 않게 04:10 사용)
 * 외부 API 오류가 서버에 전파되지 않도록 예외를 삼킨다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YouthPolicySyncScheduler {

    private final YouthPolicySyncService youthPolicySyncService;

    @Scheduled(cron = "0 10 4 * * *")
    public void syncYouthPolicies() {
        try {
            youthPolicySyncService.syncYouthPolicies();
        } catch (Exception e) {
            log.error("온통청년 주거정책 정기 동기화 실패", e);
        }
    }
}
