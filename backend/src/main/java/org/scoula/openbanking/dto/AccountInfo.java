package org.scoula.openbanking.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 계좌 목록 조회 응답 1건
 * 사용자가 연동 가능한 계좌 하나의 정보이며, 이 목록에서 사용자가 연동할 계좌를 선택
 */
@Data
public class AccountInfo {

    private String fintechUseNum;    // 핀테크이용번호 (계좌 고유 식별자, 선택·연동의 키)
    private String bankCodeStd;      // 은행표준코드 (예 "004" KB국민)
    private String bankName;         // 은행명 (표시용)
    private String accountType;      // 계좌구분: "SAVING"(적금류) / "CHECKING"(입출금)
    private String productName;      // 상품명 (예 "나라사랑 군적금", "KB Star 정기예금")
    private String accountNumMasked; // 마스킹 계좌번호 (예 "004-01-****111")
    private Long balance;            // 잔액 (적금=누적납입금 → 석윤 계산용 / 입출금=현재잔액), 연동 화면 표시용
    private String openDate;         // 개설일 "yyyy-MM-dd" (적금계좌만), 반드시 입대일 이후 - 신규회원은 Service가 입대일 기반 보정
    private String maturityDate;     // 만기일 "yyyy-MM-dd" (적금계좌만, 입출금은 null), 만기 수령액 계산은 석윤이 금리 붙여서
    private BigDecimal interestRate;  // 적금 기본금리(연 %), 적금계좌만 - "내 적금 금리" 화면 노출용 (멘토: 사용자 입장 필수). 입출금은 null
    private BigDecimal govMatchRate;  // 정부매칭 비율(%) - 군적금 특성(장병내일준비적금 등 100%), 해당 없으면 null
}
