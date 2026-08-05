package org.scoula.regret.dto;

import lombok.Data;

/**
 * 카테고리별 금액 집계 (후회 통계용)
 * 어느 카테고리에서 후회 소비가 많은지 순위 표시에 사용
 */
@Data
public class CategoryAmountDTO {
    private String category;  // 카테고리
    private Long amount;      // 합계 금액
    private Integer count;    // 건수
}
