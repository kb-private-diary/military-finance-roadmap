package org.scoula.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainFavoriteRoadmapDTO {

    // 카테고리별 목표 ID
    private Long goalId;

    // 1: 여행, 2: 진로, 3: 자동차, 4: 자취
    private Integer categoryId;

    // 관심 목표명
    private String title;

    // 목표별 예상 비용
    // 홈 프론트에서 관심 목표의 전체 사용 예정 금액 계산에 사용
    private Long amount;
}