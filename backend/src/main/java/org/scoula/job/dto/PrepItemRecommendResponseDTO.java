package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrepItemRecommendResponseDTO {
    private Long goalId;
    // key: item_type(P01/P02/P03), value: 해당 유형의 준비항목 목록
    private Map<String, List<PrepItemDTO>> items;
}
