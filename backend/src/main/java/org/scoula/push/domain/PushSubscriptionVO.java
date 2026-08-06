package org.scoula.push.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

// push_subscription 테이블 매핑. 브라우저 하나(=구독 하나)당 발송에 필요한 주소·키를 담는다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PushSubscriptionVO extends BaseVO {
    private Long subscriptionId;
    private Long userId;
    private String endpoint;   // 브라우저 푸시 서비스 발송 주소
    private String p256dh;     // 암호화 공개키
    private String auth;       // 암호화 인증 시크릿
}
