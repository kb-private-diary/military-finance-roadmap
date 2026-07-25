package org.scoula.openbanking.client;

import java.time.LocalDateTime;
import java.util.UUID;

import org.scoula.openbanking.domain.OpenbankingLinkVO;
import org.springframework.stereotype.Component;

// 오픈뱅킹 Mock 클라이언트 — 실제 호출 없이 가짜 토큰·계좌를 만들어 반환한다.
@Component
public class MockOpenBankingClient implements OpenBankingClient {

    private static final String AUTH_BASE = "https://testbed.openbanking.or.kr/oauth/2.0/authorize";

    @Override
    public String buildAuthUrl(Long userId) {
        return AUTH_BASE
                + "?response_type=code"
                + "&client_id=MOCK_CLIENT_ID"
                + "&redirect_uri=http://localhost:8080/api/openbanking/callback"
                + "&scope=login inquiry"
                + "&state=" + userId;
    }

    @Override
    public OpenbankingLinkVO linkAccount(String authCode) {
        OpenbankingLinkVO vo = new OpenbankingLinkVO();
        vo.setAccessToken("mock-access-" + UUID.randomUUID());
        vo.setRefreshToken("mock-refresh-" + UUID.randomUUID());
        vo.setFintechUseNum("199" + System.currentTimeMillis());  // 계좌당 고유(mock)
        vo.setBankCode("004");                                      // KB국민
        vo.setAccountNumMasked("1234-**-5678");
        vo.setExpiresAt(LocalDateTime.now().plusDays(90));
        return vo;
    }
}
