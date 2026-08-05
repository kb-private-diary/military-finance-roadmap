package org.scoula.regret.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.regret.domain.SpendingReviewVO;
import org.scoula.regret.dto.SpendingResponseDTO;
import org.scoula.regret.dto.RegretStatsResponseDTO;
import org.scoula.regret.dto.CategoryAmountDTO;
import java.util.List;

public interface SpendingReviewMapper {
    // 지출 목록 조회 (회고 태깅 포함, spending LEFT JOIN spending_review)
    List<SpendingResponseDTO> findSpendingsWithReview(Long userId);

    // 특정 지출의 기존 회고 조회 (재태깅 시 insert/update 분기용)
    SpendingReviewVO findReviewBySpendingId(Long spendingId);

    // 회고 신규 저장
    void insertReview(SpendingReviewVO review);

    // 회고 구분 변경 (재태깅)
    void updateReviewType(@Param("spendingId") Long spendingId,
                          @Param("reviewType") String reviewType,
                          @Param("modifiedNm") String modifiedNm);

    // 월별 후회소비 통계 (만족/후회 금액·건수 집계)
    RegretStatsResponseDTO findMonthlyStats(@Param("userId") Long userId,
                                            @Param("yearMonth") String yearMonth);

    // 월별 카테고리별 후회 금액 (후회 많은 카테고리 순)
    List<CategoryAmountDTO> findCategoryRegrets(@Param("userId") Long userId,
                                                @Param("yearMonth") String yearMonth);
}
