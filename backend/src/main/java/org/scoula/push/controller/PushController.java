package org.scoula.push.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.response.ApiResponse;
import org.scoula.push.dto.PushHistoryDTO;
import org.scoula.push.dto.PushSubscriptionRequestDTO;
import org.scoula.push.service.PushNotificationService;
import org.scoula.security.account.domain.CustomUser;

@Log4j2
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {
    private final PushNotificationService service;

    // 알림 켜기 - 브라우저 구독 정보 등록
    @PostMapping("/subscriptions")
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @AuthenticationPrincipal CustomUser customUser,
            @Valid @RequestBody PushSubscriptionRequestDTO request
    ) {
        Long userId = customUser.getMember().getId();
        this.service.subscribe(userId, customUser.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    // 알림 끄기 - 구독 해지 (소프트 삭제)
    @DeleteMapping("/subscriptions")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam String endpoint
    ) {
        Long userId = customUser.getMember().getId();
        this.service.unsubscribe(userId, endpoint, customUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 내 알림 이력 조회 (최신순, 실제 발송 성공한 것만)
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PushHistoryDTO>>> findHistoryList(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        Long userId = customUser.getMember().getId();
        return ResponseEntity.ok(ApiResponse.success(this.service.findHistoryList(userId)));
    }
}
