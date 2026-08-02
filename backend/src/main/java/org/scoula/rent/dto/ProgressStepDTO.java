package org.scoula.rent.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 진행률 한 단계 상태 (목표 상세 체크리스트 표시용).
 */
@Data
@Builder
public class ProgressStepDTO {
    private String stepCode;   // 단계 코드 (SAVE_GOAL ...)
    private String label;      // 화면 라벨 (목표 저장 ...)
    private boolean completed; // 완료 여부
}
