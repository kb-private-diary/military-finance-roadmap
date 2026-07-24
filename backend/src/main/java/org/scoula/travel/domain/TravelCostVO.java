package org.scoula.travel.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

// travel_cost 테이블 매핑 VO (예상 경비 산출 결과)
// remainingBudget 은 예산 초과 시 음수가 될 수 있다.
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TravelCostVO extends BaseVO {

    private Long costId;
    private Long goalId;
    private Long flightCost;
    private Long hotelCost;
    private Long livingCost;
    private Long totalCost;
    private Long remainingBudget;
}
