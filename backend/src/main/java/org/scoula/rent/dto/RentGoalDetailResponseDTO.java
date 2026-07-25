package org.scoula.rent.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.rent.domain.RentGoalVO;

// 월세 목표 상세 응답 (진행중 조회 · 상세 조회 공용)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentGoalDetailResponseDTO {

    private Long goalId;
    private String title;
    private String tradeType;
    private String estateType;
    private Long maxDeposit;
    private Long maxMonthly;
    private String roomCount;
    private Long expectedFee;
    private String residenceTerm;
    private Long currentAsset;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    private String status;

    public static RentGoalDetailResponseDTO of(RentGoalVO vo) {
        return RentGoalDetailResponseDTO.builder()
                .goalId(vo.getGoalId())
                .title(vo.getTitle())
                .tradeType(vo.getTradeType())
                .estateType(vo.getEstateType())
                .maxDeposit(vo.getMaxDeposit())
                .maxMonthly(vo.getMaxMonthly())
                .roomCount(vo.getRoomCount())
                .expectedFee(vo.getExpectedFee())
                .residenceTerm(vo.getResidenceTerm())
                .currentAsset(vo.getCurrentAsset())
                .targetDate(vo.getTargetDate())
                .status(vo.getStatus())
                .build();
    }
}
