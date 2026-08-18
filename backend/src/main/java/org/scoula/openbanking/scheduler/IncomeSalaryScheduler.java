package org.scoula.openbanking.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.openbanking.service.OpenBankingService;

/**
 * 월급(급여) 자동 적재 배치.
 * <p>실제 군 봉급처럼 매월 10일이 지나면 그 달 급여가 income 에 1건 들어온다.
 * 매일 00:20 에 도는데(계급 진급 배치 00:10 뒤라 진급한 호봉이 반영됨),
 * 서비스단 로직이 <b>멱등</b>이라 그 달 급여가 이미 있으면 아무 것도 하지 않는다.
 * 즉 평소엔 무동작이다가, 10일이 지나는 순간 그 달치가 딱 1건 적재된다.
 * (매일 재시도라 특정일에 서버가 죽어 있어도 다음 날 채워진다)</p>
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class IncomeSalaryScheduler {

    private final OpenBankingService openBankingService;

    @Scheduled(cron = "0 20 0 * * *")
    public void payMonthlySalary() {
        try {
            openBankingService.runMonthlySalaryBatch();
        } catch (Exception e) {
            log.error("월급 배치 실패", e);
        }
    }
}
