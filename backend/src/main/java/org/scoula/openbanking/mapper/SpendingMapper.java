package org.scoula.openbanking.mapper;

import java.util.List;

import org.scoula.openbanking.domain.SpendingVO;

/**
 * spending 테이블 접근 Mapper
 * 오픈뱅킹 거래내역에서 뽑은 지출을 적재하고, 가맹점명 카테고리 분류를 조회
 */
public interface SpendingMapper {

    /** 가맹점명 → 카테고리 조회 (merchant_category의 keyword 포함 매칭, 없으면 null) */
    String findCategoryByMerchant(String merchantName);

    /** 지출 여러 건 일괄 저장 (거래내역 동기화 결과) */
    int insertSpendings(List<SpendingVO> spendings);
}
