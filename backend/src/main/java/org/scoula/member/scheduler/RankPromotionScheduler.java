package org.scoula.member.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.member.service.RankPromotionService;

// 진급은 항상 매달 1일에만 일어나지만, 그 시각에 서버가 죽어있을 경우를 대비해 매일 재계산한다(멱등 - 1일이 아니면 결과가 그대로).
@Log4j2
@Component
@RequiredArgsConstructor
public class RankPromotionScheduler {

    private final RankPromotionService rankPromotionService;

    @Scheduled(cron = "0 10 0 * * *")
    public void promoteRanks() {
        try {
            this.rankPromotionService.promoteRanks();
        } catch (Exception e) {
            log.error("계급 진급 배치 실패", e);
        }
    }
}
