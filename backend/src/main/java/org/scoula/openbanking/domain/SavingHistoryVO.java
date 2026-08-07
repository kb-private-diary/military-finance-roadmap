package org.scoula.openbanking.domain;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 적금 납입 내역 (saving_history)
 * 오픈뱅킹 적금 납입 거래를 회차별로 저장 (석윤 saving 파트가 조회해 만기금 계산)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SavingHistoryVO extends BaseVO {
    private Long historyId;    // history_id  히스토리ID (PK)
    private Long accountId;    // account_id  계좌ID (FK)
    private Integer payRound;  // pay_round   납입회차
    private Long payAmount;    // pay_amount  해당회차 납입금
    private LocalDate paidDate; // paid_date  실제 납입일 (created_date 감사컬럼과 별개, 만기금 firstPayDate 산출용)
}
