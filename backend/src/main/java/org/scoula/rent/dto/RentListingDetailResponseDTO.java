package org.scoula.rent.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.RentListingVO;

/**
 * 매물 상세 응답 DTO (Step3)
 * 프론트 계약(RentListingDetailPage.vue)에 맞춘 중첩 구조.
 *   - listing        : 매물 상세 (프론트가 직접 읽는 필드명으로 매핑 - dongName·buildYear·maintenanceFee 등)
 *   - propertyBadges : 신선도 뱃지(dealDate 기준). 지역평균 대비 시세뱃지는 단건 조회라 계산 불가로 생략
 * 총비용·재정체크는 프론트가 자체 계산하므로 cost/affordability 필드는 두지 않는다.
 */
@Data
@Builder
public class RentListingDetailResponseDTO {

    private Listing listing;              // 매물 상세 (중첩)
    private List<String> propertyBadges;  // 신선도 등 한글 뱃지

    /** 프론트가 직접 읽는 매물 상세 (필드명은 프론트 계약과 정확히 일치시킴) */
    @Data
    @Builder
    public static class Listing {
        private Long listingId;
        private String buildingName;      // 건물명
        private String dongName;          // 읍면동명 (백엔드 umdName)
        private Integer floor;            // 층
        private BigDecimal areaSqm;       // 전용면적(㎡)
        private LocalDate dealDate;       // 계약일 (yyyy-MM-dd)
        private Integer buildYear;        // 건축년도 (백엔드 builtYear)
        private Long deposit;             // 보증금(원)
        private Long monthlyRent;         // 월세(원)
        private Long maintenanceFee;      // 예상 관리비(원) - UtilityService 계산값
        private BigDecimal latitude;      // 위도 (지도용)
        private BigDecimal longitude;     // 경도 (지도용)
    }

    /**
     * @param vo             매물 VO
     * @param maintenanceFee 서비스에서 UtilityService 로 계산한 예상 관리비(regionCode·areaSqm 없으면 0)
     */
    public static RentListingDetailResponseDTO of(RentListingVO vo, long maintenanceFee) {
        Listing listing = Listing.builder()
                .listingId(vo.getListingId())
                .buildingName(vo.getBuildingName())
                .dongName(vo.getUmdName())
                .floor(vo.getFloor())
                .areaSqm(vo.getAreaSqm())
                .dealDate(vo.getDealDate())
                .buildYear(vo.getBuiltYear())
                .deposit(vo.getDeposit())
                .monthlyRent(vo.getMonthlyRent())
                .maintenanceFee(maintenanceFee)
                .latitude(vo.getLatitude())
                .longitude(vo.getLongitude())
                .build();

        return RentListingDetailResponseDTO.builder()
                .listing(listing)
                .propertyBadges(buildFreshnessBadges(vo.getDealDate()))
                .build();
    }

    /**
     * 신선도 뱃지 (계약일이 얼마나 최근인지)
     *   ~1개월  : "1개월 전 실거래"
     *   ~3개월  : "3개월 전 실거래"
     *   그 이상 : "6개월+ 전 실거래"
     */
    private static List<String> buildFreshnessBadges(LocalDate dealDate) {
        List<String> badges = new ArrayList<>();
        if (dealDate != null) {
            long monthsAgo = ChronoUnit.MONTHS.between(dealDate, LocalDate.now());
            if (monthsAgo <= 1) {
                badges.add("1개월 전 실거래");
            } else if (monthsAgo <= 3) {
                badges.add("3개월 전 실거래");
            } else {
                badges.add("6개월+ 전 실거래");
            }
        }
        return badges;
    }
}
