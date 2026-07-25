package org.scoula.openbanking.client;

import org.scoula.openbanking.domain.OpenbankingLinkVO;

// 오픈뱅킹 외부 API 클라이언트.
// 실연동 불가(금결원: 기관만 가능)라 Mock 구현으로 대체한다.
// (운영 전환 시 @Profile 로 Real 구현체 교체)
public interface OpenBankingClient {

    // OAuth 인증 URL 생성
    String buildAuthUrl(Long userId);

    // 인증코드 → 토큰 발급 + 대표 계좌 조회 결과를 VO(미저장)로 반환
    OpenbankingLinkVO linkAccount(String authCode);
}
