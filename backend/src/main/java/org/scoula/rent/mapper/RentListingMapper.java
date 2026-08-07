package org.scoula.rent.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.rent.domain.RentListingVO;
import java.util.List;

/**
 * 월세 매물(rent_listing) 매퍼 - 배치 적재 + 조회
 * (RentMapper 와 분리 - 매물은 배치·추천으로 규모가 커서 별도 관리)
 */
public interface RentListingMapper {

    // 매물 저장 (배치/초기 적재)
    void insertListing(RentListingVO listing);

    // REGION 모드 후보: 희망 지역(법정동코드)에 속하고 월세가 예산(느슨한 상한) 이하인 매물 (월세 오름차순)
    //   실질월부담(월세+관리비+보증금환산) 필터·정렬·상위 30개 컷은 RentServiceImpl.findListings 에서 처리
    List<RentListingVO> findListingsByRegions(@Param("regionCodes") List<String> regionCodes,
                                              @Param("maxMonthly") Long maxMonthly);

    // SCHOOL 모드 후보: 학교 좌표 기준 반경 내 + 월세 예산(느슨한 상한) 이하인 매물 (가까운 순)
    //   실질월부담 필터·정렬·상위 30개 컷은 RentServiceImpl.findListings 에서 처리
    List<RentListingVO> findListingsBySchool(@Param("schoolId") Long schoolId,
                                             @Param("radiusKm") Integer radiusKm,
                                             @Param("maxMonthly") Long maxMonthly);

    // 매물 단건 조회 (Step3 상세)
    RentListingVO findById(Long listingId);
}
