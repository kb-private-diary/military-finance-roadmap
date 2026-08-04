package org.scoula.openbanking.client;

import java.util.List;

import org.scoula.openbanking.dto.AccountInfo;
import org.scoula.openbanking.dto.BalanceInfo;
import org.scoula.openbanking.dto.TokenResponse;
import org.scoula.openbanking.dto.TransactionInfo;

/**
 * 오픈뱅킹 API 를 추상화한 인터페이스
 * 실제 오픈뱅킹은 금융결제원 기관 자격(핀테크·전자금융업자)이 있어야 연동 가능한데, 현재 권한이 없어 Mock으로 개발 (영구 Mock)
 * 다만 인터페이스로 계약을 실제 스펙과 맞춤 추후, 자격을 갖추면 Real 구현 가능({@code @Profile("prod")})만 붙여 전환 가능한 구조
 */
public interface OpenBankingClient {

    /** 인증코드(code)로 액세스토큰 발급 */
    TokenResponse getToken(String code);

    /** 리프레시토큰으로 액세스토큰 갱신 */
    TokenResponse refreshToken(String refreshToken);

    /** 사용자 계좌 목록 조회 (적금·입출금 전부 - 이 중 사용자가 선택) */
    List<AccountInfo> getAccounts(String accessToken, String userSeqNo);

    /** 특정 계좌 잔액 조회 (적금계좌 연동 시 사용) */
    BalanceInfo getBalance(String accessToken, String fintechUseNum);

    /**
     * 특정 계좌 거래내역 조회 (입출금계좌 대상)
     * @param fromDate 조회 시작일 (yyyyMMdd)
     * @param toDate   조회 종료일 (yyyyMMdd)
     */
    List<TransactionInfo> getTransactions(String accessToken, String fintechUseNum,
                                          String fromDate, String toDate);

    /** 토큰 폐기 (연동 해제 시) */
    void revokeToken(String accessToken);
}
