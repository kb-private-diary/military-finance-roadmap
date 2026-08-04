package org.scoula.rent.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.RentListingVO;

/**
 * 매물 상세 응답 DTO (Step3)
 * 매물 기본 정보 + 세부 정보 (계약일·건축년도·층)
 * (관리비·총비용·재정체크·뱃지·좌표는 관리비 데이터/저장 기능 붙으면 확장 예정)
 */
@Data
@Builder
public class RentListingDetailResponseDTO {
    private Long listingId;
    private String estateType;    // 매물종류
    private String sigunguCode;   // 시군구코드
    private String umdName;       // 읍면동명
    private String buildingName;  // 건물명
    private Long deposit;         // 보증금
    private Long monthlyRent;     // 월세
    private BigDecimal areaSqm;   // 전용면적
    private Integer floor;        // 층
    private Integer builtYear;    // 건축년도
    private LocalDate dealDate;   // 계약일

    public static RentListingDetailResponseDTO of(RentListingVO vo) {
        return RentListingDetailResponseDTO.builder()
                .listingId(vo.getListingId())
                .estateType(vo.getEstateType())
                .sigunguCode(vo.getSigunguCode())
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
