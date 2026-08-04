package org.scoula.openbanking.dto;

import lombok.Data;

/**
 * 오픈뱅킹 인증 URL 발급 응답
 * 프론트는 이 authUrl로 사용자를 리다이렉트해 계좌 인증을 받음
 */
@Data
public class AuthUrlResponse {

    private String authUrl; // 오픈뱅킹 인증 페이지 URL (client-id·callback-url·scope·state 조합)
}
