package org.scoula.push.client;

import java.security.Security;

import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

// 웹푸시 발송 실무(VAPID 서명·암호화·HTTP 발송)를 담당하는 외부 연동 클라이언트.
// Service는 이 클라이언트를 통해서만 발송하고, 라이브러리/BouncyCastle 관련 사항은 여기서만 다룬다.
@Component
public class WebPushClient {
    @Value("${push.vapid.public-key}")
    private String vapidPublicKey;

    @Value("${push.vapid.private-key}")
    private String vapidPrivateKey;

    @Value("${push.vapid.subject}")
    private String vapidSubject;

    private PushService pushService;

    // BouncyCastleProvider 등록 + PushService 생성은 첫 발송 시 1회만 하면 되므로 지연 초기화한다.
    private synchronized PushService getPushService() throws Exception {
        if (this.pushService == null) {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            this.pushService = new PushService(
                    this.vapidPublicKey, this.vapidPrivateKey, this.vapidSubject);
        }
        return this.pushService;
    }

    // 발송 후 HTTP 상태코드만 반환한다. 성공/만료 판단과 이력 기록은 호출하는 Service의 몫.
    public int send(String endpoint, String p256dh, String auth, String payload) throws Exception {
        // getPushService()가 BC provider 등록을 하므로, Notification 생성(공개키 파싱)보다 반드시 먼저 호출해야 한다.
        // (순서 바뀌면 NoSuchProviderException: no such provider: BC 발생 - 직접 겪은 버그)
        PushService pushService = this.getPushService();
        Notification notification = new Notification(endpoint, p256dh, auth, payload);
        HttpResponse response = pushService.send(notification, Encoding.AES128GCM);
        return response.getStatusLine().getStatusCode();
    }
}
