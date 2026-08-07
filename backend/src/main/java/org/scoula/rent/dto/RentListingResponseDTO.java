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

    // --- 전월세전환 실질월부담 (findListings 에서 계산해 채워짐, 프론트 카드 표시용) ---
    private Long maintenanceFee;   // 월 예상 관리비 (면적앵커 보간 단가 × 전용면적 × 시도계수, 없으면 0)
    private Long depositConverted; // 보증금환산액 = 보증금 × 전월세전환율 ÷ 12 (원/월)
    private Long effectiveMonthly; // 실질 월부담 = 월세 + 관리비 + 보증금환산 (반전세 공정 비교 기준)

    // --- Step2 카드 모드별 뱃지 (findListings 에서 채움, 프론트 계약) ---
    private String selectionMode; // 위치 모드 SCHOOL / REGION (프론트 뱃지 분기용)
    private String commuteText;   // [학교 모드만] 통학시간 "도보 N분" / "버스 N분" (학교↔매물), 좌표 없으면 null
    private String transitText;   // [지역 모드만] 대중교통 "OO역 도보 N분" / "버스 이용 지역"
    private String affordLevel;   // [공통] 재정진단 코드 ENOUGH / TIGHT / OVER (6개월 거주 기준)
    private String affordText;    // [공통] 재정진단 라벨 딱 맞아요 / 빠듯해요 / 예산 초과

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

    /**
     * 뱃지 + 전월세전환 실질월부담 포함 매핑 (Step2 매물 리스트용)
     * 관리비·보증금환산·실질월부담은 findListings 에서 이미 계산한 값을 그대로 담는다.
     */
    public static RentListingResponseDTO of(RentListingVO vo, Double avgRent,
                                            long maintenanceFee, long depositConverted, long effectiveMonthly) {
        return of(vo, avgRent).toBuilder()
                .maintenanceFee(maintenanceFee)
                .depositConverted(depositConverted)
                .effectiveMonthly(effectiveMonthly)
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
