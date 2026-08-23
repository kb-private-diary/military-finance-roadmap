package org.scoula.openbanking.domain;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 적금 계좌 (saving_account)
 * 오픈뱅킹 연동 시 적금계좌 정보를 저장 (저장 담당: 오픈뱅킹 / 조회·만기금 계산: 석윤 saving)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SavingAccountVO extends BaseVO {
    private Long accountId;        // account_id     계좌ID (PK)
    private Long userId;           // user_id        회원ID
    private String bankCode;       // bank_code      은행코드 (3자리)
    private Long monthlySave;      // monthly_save   현재 한달 납입금
    private Integer monthlyCount;  // monthly_count  납입개월수
    private Long currAmount;       // curr_amount    누적납입금
    private String accountStatus;  // account_status 적금 상태 (ACTIVE 등)
    private String productType;    // product_type   적금 종류 (MILITARY 군적금 / GENERAL 일반적금) - 만기금 계산은 MILITARY만
    private LocalDate openDate;    // open_date       계좌 개설일 (실제 은행 개설일, created_date 감사컬럼과 별개)
}
