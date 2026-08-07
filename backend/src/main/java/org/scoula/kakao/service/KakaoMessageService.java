package org.scoula.kakao.service;

/**
 * 카카오톡 "나에게 보내기" 서비스
 * 본인 계정 토큰으로 메시지 발송 (사업자·전체회원 로그인 불필요, PoC/알림용)
 */
public interface KakaoMessageService {
    // 인가코드 → 토큰 발급 후 저장 (본인 카카오 로그인 1회)
    void exchangeToken(Long userId, String code);

    // 나에게 보내기 (토큰 만료 시 refresh 로 자동 갱신)
    void sendToMe(Long userId, String text);
}
