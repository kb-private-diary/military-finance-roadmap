package org.scoula.push.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.push.client.WebPushClient;
import org.scoula.push.domain.PushHistoryVO;
import org.scoula.push.domain.PushSubscriptionVO;
import org.scoula.push.dto.PushSubscriptionRequestDTO;
import org.scoula.push.mapper.PushHistoryMapper;
import org.scoula.push.mapper.PushSubscriptionMapper;

// 구독 등록/해지 + 발송을 처리. 실제 암호화·HTTP 발송은 WebPushClient에 위임하고,
// 여기서는 "누구에게 보낼지" 판단과 push_history 기록만 담당한다.
@Log4j2
@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String SYSTEM = "SYSTEM";

    private final PushSubscriptionMapper subscriptionMapper;
    private final PushHistoryMapper historyMapper;
    private final WebPushClient webPushClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    @Override
    public void subscribe(Long userId, String createdNm, PushSubscriptionRequestDTO request) {
        PushSubscriptionVO existing = this.subscriptionMapper.findByEndpoint(request.getEndpoint());

        if (existing == null) {
            // 처음 보는 endpoint - 신규 구독으로 저장
            PushSubscriptionVO vo = PushSubscriptionVO.builder()
                    .userId(userId)
                    .endpoint(request.getEndpoint())
                    .p256dh(request.getKeys().getP256dh())
                    .auth(request.getKeys().getAuth())
                    .build();
            vo.setCreatedNm(createdNm);
            this.subscriptionMapper.insert(vo);
            return;
        }

        // 같은 브라우저가 재구독(만료 후 재시도, 계정 변경 등)한 경우 - 새로 만들지 않고 기존 행을 갱신
        existing.setUserId(userId);
        existing.setP256dh(request.getKeys().getP256dh());
        existing.setAuth(request.getKeys().getAuth());
        existing.setModifiedNm(createdNm);
        this.subscriptionMapper.reactivate(existing);
    }

    @Transactional
    @Override
    public void unsubscribe(Long userId, String endpoint, String modifiedNm) {
        // findByEndpoint는 del_yn 상관없이 찾으므로(재구독용) 이미 해지된 구독은 여기서 따로 걸러야 한다.
        PushSubscriptionVO subscription = this.subscriptionMapper.findByEndpoint(endpoint);
        boolean notFound = subscription == null
                || !subscription.getUserId().equals(userId)
                || "Y".equals(subscription.getDelYn());
        if (notFound) {
            throw BusinessException.notFound("구독 정보를 찾을 수 없습니다.", "PUSH_001");
        }
        this.subscriptionMapper.deleteByEndpoint(endpoint, modifiedNm);
    }

    // 구독마다 개별 HTTP 발송(외부 I/O)이 일어나므로 DB 트랜잭션으로 묶지 않는다.
    // (각 mapper 호출은 자체 트랜잭션으로 개별 커밋됨 - 다른 조회 전용 메서드들과 동일)
    @Override
    public void send(Long userId, String title, String body) {
        List<PushSubscriptionVO> subscriptions = this.subscriptionMapper.findListByUserId(userId);
        for (PushSubscriptionVO subscription : subscriptions) {
            this.sendToSubscription(userId, subscription, title, body);
        }
    }

    private void sendToSubscription(
            Long userId, PushSubscriptionVO subscription, String title, String body) {
        String status = STATUS_FAILED;
        try {
            String payload = this.objectMapper.writeValueAsString(
                    Map.of("title", title, "body", body));
            int statusCode = this.webPushClient.send(
                    subscription.getEndpoint(), subscription.getP256dh(),
                    subscription.getAuth(), payload);

            if (statusCode == HttpStatus.SC_CREATED) {
                status = STATUS_SUCCESS;
            } else if (statusCode == HttpStatus.SC_NOT_FOUND || statusCode == HttpStatus.SC_GONE) {
                // 구독이 브라우저 쪽에서 만료/무효화됨 - 죽은 구독 자동 정리
                this.subscriptionMapper.deleteByEndpoint(subscription.getEndpoint(), SYSTEM);
                log.warn("만료된 구독 정리 - endpoint: {}, status: {}",
                        subscription.getEndpoint(), statusCode);
            } else {
                log.warn("웹푸시 발송 실패 - endpoint: {}, status: {}",
                        subscription.getEndpoint(), statusCode);
            }
        } catch (Exception e) {
            log.error("웹푸시 발송 중 오류 - endpoint: {}", subscription.getEndpoint(), e);
        }

        PushHistoryVO history = PushHistoryVO.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .status(status)
                .sentAt(LocalDateTime.now())
                .build();
        history.setCreatedNm(SYSTEM);
        this.historyMapper.insert(history);
    }
}
