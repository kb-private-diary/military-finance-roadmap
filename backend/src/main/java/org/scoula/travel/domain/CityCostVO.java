package org.scoula.travel.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

/**
 * city_cost 테이블 매핑 VO
 * 나라/도시별 여행 스타일별 1일 물가
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CityCostVO extends BaseVO {

    private Long cityCostId;
    private String country;
    private String city;
    private Long savingCost;
    private Long commonCost;
    private Long premiumCost;

    /**
     * 여행 스타일에 해당하는 1일 물가를 반환
     * @param style saving / common / premium
     * @return 스타일별 1일 물가
     */
    public Long costByStyle(String style) {
        if (style == null) {
            return this.commonCost;
        }
        switch (style.toLowerCase()) {
            case "saving":
                return this.savingCost;
            case "premium":
                return this.premiumCost;
            case "common":
            default:
                return this.commonCost;
        }
    }
}