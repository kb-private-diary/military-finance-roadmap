package org.scoula.travel.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 여행 자금 준비 안내에 필요한 로그인 사용자의 재정 정보.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelUserFinanceDTO {

    private LocalDate dischargeDate;
    private Long monthlySalary;
    private Long monthlySaving;
}
