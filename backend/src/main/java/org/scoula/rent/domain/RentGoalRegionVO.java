package org.scoula.rent.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 목표별 희망 지역 : rent_goal_region 테이블 한 행을 담는 VO
 * REGION 모드에서 한 목표(goalId)에 희망 지역(regionCode)이 여러 개 붙는다 (1:N)
 * 감사컬럼 5개는 BaseVO 상속
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RentGoalRegionVO extends BaseVO {
    private Long regionId;     // 지역번호 (PK)
    private Long goalId;       // 목표번호 (FK rent_goal)
    private String regionCode; // 법정동코드 (FK region_code)
}
