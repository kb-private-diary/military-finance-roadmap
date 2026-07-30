package org.scoula.rent.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 자취 준비 진행률(체크리스트) 한 단계. rent_progress 테이블 한 행을 담는 VO.
 * 한 목표(goalId)당 단계(stepCode)별로 1행씩 (UNIQUE goal_id + step_code).
 * 감사컬럼 5개는 BaseVO 상속.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RentProgressVO extends BaseVO {
    private Long progressId;             // 진행단계번호 (PK)
    private Long goalId;                 // 목표번호 (FK rent_goal)
    private String stepCode;             // 단계코드 SAVE_GOAL / CHECK_PRODUCT / LOAN_INQUIRY / POLICY_APPLY / MOVING_BOOK
    private String isCompleted;          // 완료여부 Y / N
    private LocalDateTime completedDate; // 완료일시
}
