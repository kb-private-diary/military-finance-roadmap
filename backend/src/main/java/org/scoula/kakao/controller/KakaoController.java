package org.scoula.kakao.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.response.ApiResponse;
import org.scoula.kakao.service.KakaoMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 카카오톡 "나에게 보내기" 연동 API
 * 본인 계정 인증(콜백) + 테스트 발송 (후회소비 알림 PoC)
 */
@RestController
@RequestMapping("/api/kakao")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoMessageService service;

    // GET /api/kakao/callback?code=xxx&state=1 → 인가코드로 토큰 발급·저장
    // (인가 URL의 state 에 userId 를 실어 보냄, JWT 연동 후 SecurityContext 로 교체)
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<Void>> callback(
            @RequestParam String code,
            @RequestParam(defaultValue = "1") Long state) {
        service.exchangeToken(state, code);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // POST /api/kakao/send-test?userId=1&text=메시지 → 나에게 보내기 (발송 확인용)
    @PostMapping("/send-test")
    public ResponseEntity<ApiResponse<Void>> sendTest(
            @RequestParam Long userId,
            @RequestParam String text) {
        service.sendToMe(userId, text);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
