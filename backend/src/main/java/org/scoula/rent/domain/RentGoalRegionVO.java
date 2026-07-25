package org.scoula.rent.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

// rent_goal_region 테이블 매핑 VO (목표별 희망 지역)
@Data
@EqualsAndHashCode(callSuper = true)
public class RentGoalRegionVO extends BaseVO {
    private Long regionId;
    private Long goalId;
    private String regionCode;  // 법정동코드
}
