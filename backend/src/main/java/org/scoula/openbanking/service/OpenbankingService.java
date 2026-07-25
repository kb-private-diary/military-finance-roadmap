package org.scoula.openbanking.service;

import org.scoula.openbanking.dto.OpenbankingLinkRequestDTO;
import org.scoula.openbanking.dto.OpenbankingLinkResponseDTO;

public interface OpenbankingService {

    // OAuth 인증 URL 발급
    String getAuthUrl(Long userId);

    // 인증코드로 계좌 연동 (토큰 발급 + openbanking_link 저장)
    OpenbankingLinkResponseDTO linkAccount(Long userId, OpenbankingLinkRequestDTO request);
}
