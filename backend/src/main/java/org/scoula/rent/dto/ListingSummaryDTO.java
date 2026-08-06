package org.scoula.rent.dto;

import java.math.BigDecimal;

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
                .build();
    }
}
