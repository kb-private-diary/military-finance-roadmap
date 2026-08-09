package org.scoula.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarModelSelectRequestDTO {
    private Long modelId;
    private Integer selectedYear; // 중고 선택 시 연식, 신차는 null
    private Integer selectedMileageKm; // 중고 선택 시 키로수, 신차는 null
}
