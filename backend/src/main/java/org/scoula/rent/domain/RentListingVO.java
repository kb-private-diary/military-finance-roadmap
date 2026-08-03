package org.scoula.rent.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 월세 매물 : rent_listing 테이블 한 행을 담는 VO
 * 국토교통부 전월세 실거래가 API 로 수집한 매물 정보 (배치 적재)
 * 감사컬럼 5개는 BaseVO 상속
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RentListingVO extends BaseVO {
    private Long listingId;        // 매물번호 (PK)
    private String estateType;     // 매물종류 APARTMENT / OFFICETEL / VILLA
    private String sigunguCode;    // 시군구코드 (LAWD_CD, 5자리)
    private String regionCode;     // 법정동코드 (10자리)
    private String umdName;        // 읍면동명
    private String jibun;          // 지번
    private String buildingName;   // 건물명
    private Integer builtYear;     // 건축년도
    private Integer floor;         // 층
    private BigDecimal areaSqm;    // 전용면적(㎡)
    private Long deposit;          // 보증금(원)
    private Long monthlyRent;      // 월세(원)
    private LocalDate dealDate;    // 계약일
    private BigDecimal latitude;   // 위도
    private BigDecimal longitude;  // 경도
    private LocalDate baseDate;    // 적재기준일
}
