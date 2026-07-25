package org.scoula.rent.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 월세 목표 등록 요청. userId 는 @RequestParam 으로 받는다(JWT 연동 전 임시).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentGoalCreateRequestDTO {

    private String title;
    private String tradeType;      // 필수
    private String estateType;
    private Long maxDeposit;       // 필수, > 0
    private Long maxMonthly;       // 필수, > 0
    private String roomCount;
    private Long expectedFee;
    private String residenceTerm;  // 필수
    private Long currentAsset;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;
}
