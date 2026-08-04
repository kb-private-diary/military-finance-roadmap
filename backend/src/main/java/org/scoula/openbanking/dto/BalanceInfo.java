package org.scoula.openbanking.dto;

import lombok.Data;

/**
 * 잔액 조회 응답
 * 적금계좌 연동 시 잔액(누적납입금)을 받아 석윤 파트 saving_account 생성에 넘김
 */
@Data
public class BalanceInfo {

    private String fintechUseNum; // 핀테크이용번호 (어느 계좌인지)
    private Long balanceAmt;      // 잔액 (원)
}
