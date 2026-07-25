package org.scoula.openbanking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 계좌 연동 요청 (OAuth 콜백으로 받은 인증코드)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenbankingLinkRequestDTO {
    private String authCode;
}
