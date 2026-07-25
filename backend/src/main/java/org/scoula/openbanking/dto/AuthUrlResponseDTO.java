package org.scoula.openbanking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 오픈뱅킹 OAuth 인증 URL 응답
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUrlResponseDTO {
    private String authUrl;
}
