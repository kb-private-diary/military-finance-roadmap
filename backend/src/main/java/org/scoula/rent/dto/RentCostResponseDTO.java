package org.scoula.rent.dto;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.RentListingVO;

/**
 * 총 필요자금 응답 DTO (매물 하나 기준)
 * 보증금(반환됨) vs 월주거비(소멸)를 분리 표기
 * (예상관리비·현재자산 충당 여부는 관리비 데이터·석윤 만기금 API 붙으면 확장 예정)
 */
@Data
@Builder
public class RentCostResponseDTO {
    private Long listingId;      // 매물번호
    private Long deposit;        // 보증금 (계약 종료 시 반환)
    private Long monthlyRent;          // 월세
    private Long monthlyManagementFee; // 월 예상 관리비·공과금 (면적당요금 × 전용면적, 데이터 없으면 0)
    private Integer months;            // 거주 개월수
    private Long livingCost;           // 월주거비 합계 = (월세 + 관리비) × 개월 (소멸성)
    private Long totalRequired;        // 총 필요자금 = 보증금 + 월주거비

    public static RentCostResponseDTO of(RentListingVO vo, int months, long monthlyManagementFee,
                                         long livingCost, long totalRequired) {
        return RentCostResponseDTO.builder()
                .listingId(vo.getListingId())
                .deposit(vo.getDeposit())
                .monthlyRent(vo.getMonthlyRent())
                .monthlyManagementFee(monthlyManagementFee)
                .months(months)
                .livingCost(livingCost)
                .totalRequired(totalRequired)
                .build();
    }
}
