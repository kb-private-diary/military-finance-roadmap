package org.scoula.openbanking.domain;

import java.time.LocalDateTime;

import org.scoula.common.domain.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 지출 내역 (spending)
 * 오픈뱅킹 거래내역(출금)에서 적재한 지출 1건
 * 후회소비(regret) 도메인이 이 데이터를 조회해 "후회/만족" 태깅
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpendingVO extends BaseVO {

    private Long spendingId;     // spending_id    지출번호 (PK)
    private Long userId;         // user_id        회원고유번호
    private String merchantName; // merchant_name  가맹점명
    private String category;     // category       카테고리 (merchant_category로 자동분류)
    private Long amount;         // amount         지출금액
    private LocalDateTime spentAt; // spent_at       결제일시
}
