package org.scoula.openbanking.domain;

import java.util.Date;

import org.scoula.common.domain.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 오픈뱅킹 연동 정보 (openbanking_link)
 * 회원이 오픈뱅킹 인증을 마치면 발급받은 토큰과 연동 계좌 정보를 저장
 * 회원당 N건 (은행별 계좌마다 1건)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpenBankingLinkVO extends BaseVO {

    private Long linkId;             // link_id           연동번호 (PK)
    private Long userId;             // user_id           회원고유번호
    private String accessToken;      // access_token      액세스토큰
    private String refreshToken;     // refresh_token     리프레시토큰
    private String fintechUseNum;    // fintech_use_num   핀테크이용번호 (계좌당 고유, 중복 연동 방지)
    private String bankCode;         // bank_code         은행코드
    private Long accountId;          // account_id        연동된 군적금 계좌 (적금만, 입출금은 null)
    private String accountNumMasked; // account_num_masked 마스킹 계좌번호 (표시용)
    private Date expiresAt;          // expires_at        토큰만료일시
}
