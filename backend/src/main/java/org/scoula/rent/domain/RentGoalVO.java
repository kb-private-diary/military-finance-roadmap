package org.scoula.rent.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 자취(월세) 목표 : rent_goal 테이블 한 행을 담는 VO
 * 감사컬럼 5개(createdDate/createdNm/modifiedDate/modifiedNm/delYn)는 BaseVO 상속
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RentGoalVO extends BaseVO {
    private Long goalId;             // 목표번호 (PK)
    private Long userId;             // 회원번호 (FK user)
    private String title;            // 목표이름 (선택)
    private String selectionMode;    // 위치모드 SCHOOL / REGION
    private Long schoolId;           // 학교 ID (SCHOOL 모드일 때, FK school)
    private Integer commuteRadiusKm; // 통학 반경 km (SCHOOL 모드일 때)
    private Long monthlyBudget;      // 월예산 (월세 + 관리비 합)
    private String residencePreset;  // 거주기간 프리셋 SEMESTER / YEAR / GRADUATE
    private Integer residenceMonths; // 거주 개월수 6 / 12 / 24 (프리셋 매핑값)
    private String status;           // 진행상태 DRAFT / CONFIRMED / ARCHIVED
    private Long confirmedListingId; // 확정 매물번호 (Step5 저장 시 선택 매물, DRAFT면 null)
}
