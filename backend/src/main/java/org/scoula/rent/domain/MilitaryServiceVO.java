package org.scoula.rent.domain;

import java.time.LocalDate;

import lombok.Data;

/**
 * 사용자 복무기간 조회 결과 (user 테이블의 enlist_date·discharge_date 만 담는 경량 VO)
 * 주거 금융상품 추천 시 병역 혜택(연령 상한 연장) 안내에 사용한다
 * user 테이블엔 생년월일이 없어 나이 자체는 계산할 수 없다 - 복무기간만 다룬다
 */
@Data
public class MilitaryServiceVO {
    private LocalDate enlistDate;    // 입대일
    private LocalDate dischargeDate; // 전역예정일
}
