package org.scoula.openbanking.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

// openbanking_link 테이블 매핑 VO (계좌 연동 정보)
@Data
@EqualsAndHashCode(callSuper = true)
public class OpenbankingLinkVO extends BaseVO {

    private Long linkId;
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private String fintechUseNum;      // 핀테크이용번호 (계좌당 고유)
    private String bankCode;
    private Long accountId;            // 적금계좌만 채움 (입출금은 NULL)
    private String accountNumMasked;
    private LocalDateTime expiresAt;
}
