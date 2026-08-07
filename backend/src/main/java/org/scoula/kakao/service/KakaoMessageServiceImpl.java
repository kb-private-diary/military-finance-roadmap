package org.scoula.kakao.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.scoula.common.exception.BusinessException;
import org.scoula.kakao.domain.KakaoTokenVO;
import org.scoula.kakao.dto.KakaoTokenResponse;
import org.scoula.kakao.mapper.KakaoTokenMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoMessageServiceImpl implements KakaoMessageService {

    private final KakaoTokenMapper tokenMapper;
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 카카오 토큰 발급·갱신 / 나에게 보내기 엔드포인트
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String SEND_URL = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

    @Value("${kakao.rest-api-key}")
    private String restApiKey;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;
    // Client Secret (카카오 로그인 secret 활성화 시 토큰 요청에 필수)
    @Value("${kakao.client-secret:}")
    private String clientSecret;

    @Override
    @Transactional
    public void exchangeToken(Long userId, String code) {
        // 인가코드 → 토큰 (grant_type=authorization_code)
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", restApiKey);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);
        addClientSecret(form);

        KakaoTokenResponse res = requestToken(form);
        if (res == null || res.getAccessToken() == null) {
            throw BusinessException.badRequest("카카오 토큰 발급에 실패했습니다.", "KAKAO_001");
        }

        KakaoTokenVO token = new KakaoTokenVO();
        token.setUserId(userId);
        token.setAccessToken(res.getAccessToken());
        token.setRefreshToken(res.getRefreshToken());
        token.setExpiresAt(LocalDateTime.now().plusSeconds(res.getExpiresIn() != null ? res.getExpiresIn() : 0));
        token.setCreatedNm("user:" + userId);
        tokenMapper.upsertToken(token);
    }

    @Override
    @Transactional
    public void sendToMe(Long userId, String text) {
        KakaoTokenVO token = tokenMapper.findByUserId(userId);
        if (token == null) {
            throw BusinessException.notFound("카카오 연동이 필요합니다.", "KAKAO_002");
        }
        String accessToken = ensureValidAccessToken(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("template_object", buildTextTemplate(text));

        try {
            rest.postForObject(SEND_URL, new HttpEntity<>(form, headers), String.class);
        } catch (RestClientResponseException e) {
            // 카카오 발송 에러 본문 그대로 노출 (예 insufficient scopes, 토큰 만료)
            throw BusinessException.badRequest(
                    "카카오 메시지 발송 실패: " + e.getResponseBodyAsString(), "KAKAO_006");
        }
    }

    /** access 만료(1분 여유) 시 refresh_token 으로 갱신, 유효하면 그대로 반환 */
    private String ensureValidAccessToken(KakaoTokenVO token) {
        boolean valid = token.getExpiresAt() != null
                && token.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(1));
        if (valid) {
            return token.getAccessToken();
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", restApiKey);
        form.add("refresh_token", token.getRefreshToken());
        addClientSecret(form);

        KakaoTokenResponse res = requestToken(form);
        if (res == null || res.getAccessToken() == null) {
            throw BusinessException.badRequest("카카오 토큰 갱신에 실패했습니다.", "KAKAO_003");
        }

        token.setAccessToken(res.getAccessToken());
        token.setExpiresAt(LocalDateTime.now().plusSeconds(res.getExpiresIn() != null ? res.getExpiresIn() : 0));
        // refresh_token 은 만료가 가까울 때만 새로 내려옴 - 오면 갱신
        if (res.getRefreshToken() != null) {
            token.setRefreshToken(res.getRefreshToken());
        }
        tokenMapper.upsertToken(token);
        return res.getAccessToken();
    }

    /** Client Secret 설정 시 토큰 요청 폼에 추가 (카카오 로그인 secret 활성화 대응) */
    private void addClientSecret(MultiValueMap<String, String> form) {
        if (clientSecret != null && !clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }
    }

    /** oauth/token 공통 호출 (application/x-www-form-urlencoded) */
    private KakaoTokenResponse requestToken(MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        try {
            return rest.postForObject(TOKEN_URL, new HttpEntity<>(form, headers), KakaoTokenResponse.class);
        } catch (RestClientResponseException e) {
            // 카카오 에러: 상태코드 + 주입값(client_id/redirect) + 헤더 + 본문을 노출해 원인 파악
            String keyPreview = restApiKey == null ? "null"
                    : (restApiKey.isEmpty() ? "EMPTY" : restApiKey.substring(0, Math.min(6, restApiKey.length())) + "...");
            throw BusinessException.badRequest(
                    "카카오 토큰 발급 실패: [status=" + e.getRawStatusCode() + "]"
                            + " client_id=" + keyPreview
                            + " redirect=" + redirectUri
                            + " www-auth=" + (e.getResponseHeaders() != null ? e.getResponseHeaders().getFirst("WWW-Authenticate") : "")
                            + " body=" + e.getResponseBodyAsString(), "KAKAO_005");
        }
    }

    /** 텍스트 템플릿 JSON 생성 ({"object_type":"text","text":...,"link":{...}}) */
    private String buildTextTemplate(String text) {
        Map<String, Object> template = Map.of(
                "object_type", "text",
                "text", text,
                "link", Map.of("web_url", "http://localhost:8080", "mobile_web_url", "http://localhost:8080")
        );
        try {
            return objectMapper.writeValueAsString(template);
        } catch (Exception e) {
            throw BusinessException.badRequest("카카오 메시지 생성에 실패했습니다.", "KAKAO_004");
        }
    }
}
