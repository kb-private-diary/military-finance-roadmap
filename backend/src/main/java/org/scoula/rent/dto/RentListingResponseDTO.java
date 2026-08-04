package org.scoula.rent.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.RentListingVO;

/**
 * 매물 리스트 응답 DTO (Step2)
 * 매물 카드에 필요한 기본 정보 (관리비·총비용·뱃지는 신규 테이블 데이터 반영 후 확장 예정)
 */
@Data
@Builder
public class RentListingResponseDTO {
    private Long listingId;
    private String estateType;    // 매물종류 (OFFICETEL/VILLA/APARTMENT)
    private String umdName;       // 읍면동명
    private String buildingName;  // 건물명
    private Long deposit;         // 보증금
    private Long monthlyRent;     // 월세
    private BigDecimal areaSqm;   // 전용면적
    private Integer floor;        // 층
    private Integer builtYear;    // 건축년도
    private LocalDate dealDate;   // 계약일

    public static RentListingResponseDTO of(RentListingVO vo) {
        return RentListingResponseDTO.builder()
                .listingId(vo.getListingId())
                .estateType(vo.getEstateType())
                .umdName(vo.getUmdName())
                .buildingName(vo.getBuildingName())
                .deposit(vo.getDeposit())
                .monthlyRent(vo.getMonthlyRent())
                .areaSqm(vo.getAreaSqm())
                .floor(vo.getFloor())
                .builtYear(vo.getBuiltYear())
                .dealDate(vo.getDealDate())
                .build();
    }
}
