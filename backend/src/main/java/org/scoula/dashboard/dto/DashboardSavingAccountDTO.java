package org.scoula.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 추후 삭제 필요 openbanking 패키지에서 계좌 생성 구현 예정
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSavingAccountDTO {
    private Long accountId;
    private Long userId;
    private String bankCode;
    private Long monthlySave;
    private Integer monthlyCount;
    private Long currAmount;
    private String accountStatus;
    private LocalDate createdDate;
}
