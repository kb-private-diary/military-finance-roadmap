package org.scoula.regret.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 지출 회고 (spending_review)
 * 지출(spending) 1건에 대한 만족/후회 태깅을 담는 VO
 * 감사컬럼 5개는 BaseVO 상속
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpendingReviewVO extends BaseVO {
    private Long reviewId;            // review_id     회고번호 (PK)
    private Long spendingId;          // spending_id   지출번호 (FK, 지출당 1건)
    private String reviewType;        // review_type   회고구분 (SATISFIED/REGRET)
    private LocalDateTime reviewedAt; // reviewed_at   태깅일시
}
