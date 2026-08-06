package org.scoula.rent.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.RentListingVO;

/**
 * 매물 리스트 응답 DTO (Step2/3)
 * 매물 카드 기본 정보 + 뱃지(시세 상대평가·신선도)
 */
@Data
@Builder(toBuilder = true)
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

    // --- 뱃지 (지역·종류 평균 대비 계산이 필요해 avgRent 넘길 때만 채워짐) ---
    private String priceLevel;    // 시세 상대평가 CHEAP / AVERAGE / EXPENSIVE
    private String freshness;     // 실거래 신선도 FRESH_1M / FRESH_3M / OLD_6M

    /** 뱃지 없는 기본 매핑 */
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

    /** 뱃지 포함 매핑 (avgRent = 같은 지역·종류 평균 월세) */
    public static RentListingResponseDTO of(RentListingVO vo, Double avgRent) {
        return of(vo).toBuilder()
                .priceLevel(priceLevel(vo.getMonthlyRent(), avgRent))
                .freshness(freshness(vo.getDealDate()))
                .build();
    }

    /** 시세 상대평가: 평균의 90% 이하 저렴 / 110% 초과 비쌈 / 그 사이 평균 (명세 4.3.2) */
    private static String priceLevel(Long monthlyRent, Double avgRent) {
        if (monthlyRent == null || avgRent == null || avgRent <= 0) {
            return null;
        }
        double ratio = monthlyRent / avgRent;
        if (ratio <= 0.9) return "CHEAP";
        if (ratio > 1.1) return "EXPENSIVE";
        return "AVERAGE";
    }

    /** 실거래 신선도: 계약일 경과 개월 1 이하 / 3 이하 / 초과 (명세 4.3.2) */
    private static String freshness(LocalDate dealDate) {
        if (dealDate == null) {
            return null;
        }
        long months = ChronoUnit.MONTHS.between(dealDate, LocalDate.now());
        if (months <= 1) return "FRESH_1M";
        if (months <= 3) return "FRESH_3M";
        return "OLD_6M";
    }
}
