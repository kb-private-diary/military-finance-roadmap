package org.scoula.saving.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

// saving_account 테이블 매핑 VO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SavingAccountVO extends BaseVO {
    private Long accountId;
    private Long userId;
    private String bankCode;
    private Long monthlySave;
    private Integer monthlyCount;
    private Long currAmount;
    private String accountStatus;
}
