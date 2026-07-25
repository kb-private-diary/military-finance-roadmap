package org.scoula.rent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 월세 목표 등록 응답 — 생성된 goalId 반환
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentGoalCreateResponseDTO {
    private Long goalId;
}
