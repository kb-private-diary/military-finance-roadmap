package org.scoula.travel.domain;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

// travel_goal 테이블 매핑 VO
// places, benefits는 DB상 JSON 타입이지만 MyBatis에 JSON용 타입핸들러가 없음.
// 대신 MySQL JDBC 드라이버가 JSON 컬럼을 String으로 돌려줌. 그래서 VO에서 String으로 다룸.
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TravelGoalVO extends BaseVO {

    private Long goalId;
    private Long userId;
    private String title;
    private String departure;
    private String destination;
    private Boolean isDomestic;
    private String style;
    private Date startDate;
    private Date endDate;
    private Long totalBudget;
    private String places;
    private String benefits;
    private Long packageId;
    private String status;
}
