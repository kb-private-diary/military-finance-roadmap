package org.scoula.rent.service;

import org.scoula.rent.dto.HousingProductRecommendResponseDTO;

public interface HousingProductService {

    /**
     * 매물 조건 기반 주거 금융상품 추천 (그룹핑 응답)
     * @param listingId 대상 매물 번호
     * @param userId    사용자 번호 (복무기간 안내용, 임시 @RequestParam)
     * @param months    거주(임대차) 개월수 (부산 머물자리론 12개월 미만 제외 판정용)
     */
    HousingProductRecommendResponseDTO recommend(Long listingId, Long userId, int months);
}
