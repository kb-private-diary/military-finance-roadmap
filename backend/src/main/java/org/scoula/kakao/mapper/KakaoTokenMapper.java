package org.scoula.kakao.mapper;

import org.scoula.kakao.domain.KakaoTokenVO;

/**
 * 카카오 토큰 매퍼 (kakao_token)
 * 회원당 1건 (user_id UNIQUE) - 발급 시 UPSERT, 발송 시 조회
 */
public interface KakaoTokenMapper {
    // 회원의 카카오 토큰 조회 (없으면 null)
    KakaoTokenVO findByUserId(Long userId);

    // 토큰 저장/갱신 (user_id UNIQUE 충돌 시 갱신)
    void upsertToken(KakaoTokenVO token);
}
