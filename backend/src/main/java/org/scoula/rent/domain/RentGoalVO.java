package org.scoula.rent.domain;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

// rent_goal 테이블 매핑 VO (월세 목표). 감사컬럼 5개는 BaseVO 상속.
@Data
@EqualsAndHashCode(callSuper = true)
public class RentGoalVO extends BaseVO {

    private Long goalId;
    private Long userId;
    private String title;
    private String tradeType;      // 거래유형 (MONTHLY 등)
    private String estateType;     // 매물종류 (OFFICETEL, VILLA 등)
    private Long maxDeposit;       // 보증금 한도
    private Long maxMonthly;       // 월세 한도
    private String roomCount;      // 방개수 조건 (ONE 등)
    private Long expectedFee;      // 예상 관리비
    private String residenceTerm;  // 거주기간 (Y1, Y2)
    private Long currentAsset;     // 현재자산
    private LocalDate targetDate;  // 입주 목표 시기
    private String status;         // 진행상태 (DRAFT/CONFIRMED/ARCHIVED)
}
