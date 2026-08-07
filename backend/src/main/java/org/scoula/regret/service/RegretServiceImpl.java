package org.scoula.regret.service;

import lombok.RequiredArgsConstructor;
import org.scoula.common.exception.BusinessException;
import org.scoula.regret.domain.ReviewType;
import org.scoula.regret.domain.SpendingReviewVO;
import org.scoula.regret.dto.SpendingResponseDTO;
import org.scoula.regret.dto.SpendingReviewRequestDTO;
import org.scoula.regret.dto.RegretStatsResponseDTO;
import org.scoula.regret.dto.RegretSpendingSummaryDTO;
import org.scoula.regret.mapper.SpendingReviewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegretServiceImpl implements RegretService {

    private final SpendingReviewMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SpendingResponseDTO> findSpendings(Long userId) {
        return this.mapper.findSpendingsWithReview(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public RegretStatsResponseDTO getMonthlyStats(Long userId, String yearMonth) {
        RegretStatsResponseDTO stats = this.mapper.findMonthlyStats(userId, yearMonth);
        // 카테고리별 후회 순위는 별도 쿼리로 조회해 세팅
        stats.setCategoryRegrets(this.mapper.findCategoryRegrets(userId, yearMonth));

        // 수입 대비 후회소비 비율 - income(오픈뱅킹 저장)을 조회해 계산
        Long monthlyIncome = this.mapper.findMonthlyIncomeByUserId(userId, yearMonth);
        stats.setMonthlyIncome(monthlyIncome);
        long regret = stats.getRegretAmount() != null ? stats.getRegretAmount() : 0L;
        // 수입이 있어야 비율 산출 (없으면 0), 소수 첫째 자리까지
        stats.setRegretRatio(monthlyIncome != null && monthlyIncome > 0
                ? Math.round(regret * 1000.0 / monthlyIncome) / 10.0
                : 0.0);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public RegretSpendingSummaryDTO getSpendingSummary(Long userId, int months) {
        // 개월수는 1 이상 (0 나눗셈 방지)
        int safeMonths = Math.max(months, 1);
        return this.mapper.findSpendingSummary(userId, safeMonths);
    }

    @Override
    @Transactional
    public void tagReview(SpendingReviewRequestDTO request, Long userId) {
        // 1) 회고구분 유효성 검증 (SATISFIED/REGRET 만 허용)
        validateReviewType(request.getReviewType());

        String actor = "user:" + userId; // TODO: JWT 연동 후 교체

        // 2) 기존 회고 있으면 UPDATE, 없으면 INSERT (지출당 회고 1건 유지)
        SpendingReviewVO existing = this.mapper.findReviewBySpendingId(request.getSpendingId());
        if (existing != null) {
            this.mapper.updateReviewType(request.getSpendingId(), request.getReviewType(), actor);
        } else {
            SpendingReviewVO review = new SpendingReviewVO();
            review.setSpendingId(request.getSpendingId());
            review.setReviewType(request.getReviewType());
            review.setCreatedNm(actor);
            this.mapper.insertReview(review);
        }
    }

    /** 회고구분 검증 (enum에 없는 값이면 REGRET_001) */
    private void validateReviewType(String reviewType) {
        try {
            ReviewType.valueOf(reviewType);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw BusinessException.badRequest("회고 구분이 올바르지 않습니다.", "REGRET_001");
        }
    }
}
