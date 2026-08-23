package org.scoula.openbanking.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
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

    /** 동일 거래(회원+일시+가맹점+금액)가 이미 적재됐는지 - 재동기화 중복 방지 (기존 점호 기록 보존) */
    boolean existsSpending(@Param("userId") Long userId,
                           @Param("spentAt") LocalDateTime spentAt,
                           @Param("merchantName") String merchantName,
                           @Param("amount") Long amount);
}
