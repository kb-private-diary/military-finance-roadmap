package org.scoula.dashboard.dto;

import java.time.LocalDate;

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
public class DashboardSavingHistoryDTO {
    private Long historyId;
    private Long accountId;
    private Integer payRound;
    private Long payAmount;
    private LocalDate createdDate;
}
