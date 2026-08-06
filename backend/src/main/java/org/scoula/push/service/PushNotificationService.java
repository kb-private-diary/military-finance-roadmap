package org.scoula.push.service;

import java.util.List;

import org.scoula.push.dto.PushHistoryDTO;
import org.scoula.push.dto.PushSubscriptionRequestDTO;

// 웹푸시 공통 인프라 - 다른 도메인은 send()만 주입받아 쓰면 됨.
// PushService가 아닌 이유: 라이브러리 nl.martijndwars.webpush.PushService와 이름 충돌 방지
public interface PushNotificationService {
    void subscribe(Long userId, String createdNm, PushSubscriptionRequestDTO request);

    void unsubscribe(Long userId, String endpoint, String modifiedNm);

    // 다른 도메인이 알림 보낼 때 쓰는 진입점. 트리거 조건은 호출하는 쪽에서 판단
    void send(Long userId, String title, String body);

    // 내 알림 이력 조회 (최신순)
    List<PushHistoryDTO> findHistoryList(Long userId);
}
