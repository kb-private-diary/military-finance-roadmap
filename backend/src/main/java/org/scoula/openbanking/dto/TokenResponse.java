package org.scoula.openbanking.dto;

import lombok.Data;

/**
 * 오픈뱅킹 토큰 발급/갱신 응답
 * 인증코드(code) 또는 리프레시토큰으로 액세스토큰을 받아온 결과
 */
@Data
public class TokenResponse {

    private String accessToken;  // 액세스토큰 (API 호출 시 사용)
    private String refreshToken; // 리프레시토큰 (만료 시 갱신용)
    private String userSeqNo;    // 사용자일련번호 (오픈뱅킹이 발급하는 회원 식별자)
    private Long expiresIn;      // 만료까지 남은 초 (예 7776000 = 90일)
    private String scope;        // 허용된 권한 범위 (예 "login inquiry")
    private String tokenType;    // 토큰 종류 (보통 "Bearer")
}
