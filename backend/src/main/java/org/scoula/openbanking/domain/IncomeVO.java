package org.scoula.openbanking.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 수입 (income)
 * 급여 등 입금 내역을 저장 (저장 담당: 오픈뱅킹 / 조회: 진로·여행·자동차·후회소비 등 공용)
 * spending(지출)과 대칭 구조 - 수입원(source)·입금일시(receivedAt)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IncomeVO extends BaseVO {
    private Long incomeId;             // income_id    수입번호 (PK)
    private Long userId;              // user_id      회원ID
    private String source;           // source       수입원 (예 "국군재정관리단"=급여)
    private String category;         // category     수입종류 (SALARY 등)
    private Long amount;             // amount       수입금액
    private LocalDateTime receivedAt; // received_at  입금일시 (업무일, created_date 감사컬럼과 분리)
}
