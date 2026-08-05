package org.scoula.regret.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 지출 목록 응답 DTO (후회소비 화면)
 * 지출 정보 + 회고 태깅 상태 (reviewType null이면 아직 미태깅)
 */
@Data
public class SpendingResponseDTO {
    private Long spendingId;       // 지출번호
    private String merchantName;   // 가맹점명
    private String category;       // 카테고리 (merchant_category 자동분류)
    private Long amount;           // 지출금액
    private LocalDateTime spentAt; // 결제일시
    private String reviewType;     // 회고구분 (SATISFIED/REGRET/null)
}
