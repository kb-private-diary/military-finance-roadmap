package org.scoula.rent.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.scoula.rent.domain.RentListingVO;

import lombok.Builder;
import lombok.Data;

/**
 * Step5 저장 후 상세의 확정 매물 요약 (지번·좌표는 저장 후에만 노출)
 */
@Data
@Builder
public class ListingSummaryDTO {
    private Long listingId;
    private String buildingName;
    private String jibunAddress; // 읍면동 + 지번 (예: 장전동 123-45)
    private Integer floor;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal areaSqm;   // 전용면적(㎡)
    private Integer buildYear;    // 건축년도 (VO builtYear → 프론트 buildYear)
    private LocalDate dealDate;   // 계약일
    private Long deposit;         // 보증금(원)
    private Long monthlyRent;     // 월세(원)

    public static ListingSummaryDTO of(RentListingVO vo) {
        String umd = vo.getUmdName() == null ? "" : vo.getUmdName();
        String jibun = vo.getJibun() == null ? "" : vo.getJibun();
        return ListingSummaryDTO.builder()
                .listingId(vo.getListingId())
                .buildingName(vo.getBuildingName())
                .jibunAddress((umd + " " + jibun).trim())
                .floor(vo.getFloor())
                .latitude(vo.getLatitude())
                .longitude(vo.getLongitude())
                .areaSqm(vo.getAreaSqm())
                .buildYear(vo.getBuiltYear())
                .dealDate(vo.getDealDate())
                .deposit(vo.getDeposit())
                .monthlyRent(vo.getMonthlyRent())
                .build();
    }
}
