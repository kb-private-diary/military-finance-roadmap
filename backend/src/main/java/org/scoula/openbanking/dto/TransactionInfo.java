package org.scoula.openbanking.dto;

import java.util.Date;

import lombok.Data;

/**
 * 거래내역 조회 응답 1건 (원천 데이터, 분류 전)
 * 입출금계좌에서 받아오며, Service가 출금(OUT)만 골라 카테고리를 매긴 뒤
 * SpendingVO로 변환해 spending 테이블에 적재
 */
@Data
public class TransactionInfo {

    private Date txDateTime;     // 거래일시
    private String merchantName; // 가맹점명 / 적요 (예 "배달의민족", "국군복지단 PX")
    private Long amount;         // 거래금액
    private String inoutType;    // 입출금구분: "OUT"(출금=지출) / "IN"(입금)
}
