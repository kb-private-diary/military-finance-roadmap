package org.scoula.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 카카오 토큰 발급·갱신 응답 (oauth/token)
 * snake_case 응답을 @JsonProperty 로 매핑
 */
@Data
public class KakaoTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken; // 갱신 시엔 없을 수 있음 (기존 것 유지)

    @JsonProperty("expires_in")
    private Integer expiresIn;   // access_token 남은 초

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("scope")
    private String scope;
}
