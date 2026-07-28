package org.scoula.product.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.product.service.ProductService;

// 매일 새벽 3시에 FSS API에서 KB 예·적금 상품을 가져와 DB에 반영한다.
@Log4j2
@Component
@RequiredArgsConstructor
public class ProductSyncScheduler {

    private final ProductService productService;

    @Scheduled(cron = "0 0 3 * * *")
    public void syncSavingProducts() {
        try {
            productService.syncSavingProducts();
        } catch (Exception e) {
            log.error("KB 예·적금 상품 정기 동기화 실패", e);
        }
    }
}
