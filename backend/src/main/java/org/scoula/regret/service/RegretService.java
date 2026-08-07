package org.scoula.regret.service;

import org.scoula.regret.dto.SpendingResponseDTO;
import org.scoula.regret.dto.SpendingReviewRequestDTO;
import org.scoula.regret.dto.RegretStatsResponseDTO;
import org.scoula.regret.dto.RegretSpendingSummaryDTO;
import java.util.List;

public interface RegretService {
    // 지출 목록 조회 (회고 태깅 상태 포함)
    List<SpendingResponseDTO> findSpendings(Long userId);

    // 지출 회고 태깅 (만족/후회, 재태깅 시 갱신)
    void tagReview(SpendingReviewRequestDTO request, Long userId);

    // 월별 후회소비 통계 조회
    RegretStatsResponseDTO getMonthlyStats(Long userId, String yearMonth);

    // 최근 N개월 월평균 지출·후회소비 요약 (자취 Step5가 조회)
    RegretSpendingSummaryDTO getSpendingSummary(Long userId, int months);
}
