package org.scoula.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSavingsResponseDTO {
    // 현재까지의 총 적금 납입액 (saving_history 기준)
    private Long currentTotalSavings;
    
    // 예상되는 만기 수령액 총합 (2개 은행 각각의 이자 등을 포함하여 계산된 값)
    private Long expectedMaturityTotal;
}
