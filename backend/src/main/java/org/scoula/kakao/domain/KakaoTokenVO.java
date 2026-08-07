package org.scoula.kakao.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 카카오 토큰 (kakao_token)
 * 카카오톡 "나에게 보내기" 발송용 회원별 토큰 (회원당 1건, refresh_token으로 access 갱신)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KakaoTokenVO extends BaseVO {
    private Long tokenId;            // token_id      토큰번호 (PK)
    private Long userId;            // user_id       회원ID (UNIQUE, 회원당 1건)
    private String accessToken;    // access_token  액세스토큰 (6시간 만료)
    private String refreshToken;   // refresh_token 리프레시토큰 (갱신용, 2개월)
    private LocalDateTime expiresAt; // expires_at   액세스토큰 만료일시
}
