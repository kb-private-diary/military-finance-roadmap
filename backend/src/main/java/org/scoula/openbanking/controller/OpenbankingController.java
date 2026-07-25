package org.scoula.openbanking.controller;

import lombok.RequiredArgsConstructor;

import org.scoula.common.response.ApiResponse;
import org.scoula.openbanking.dto.AuthUrlResponseDTO;
import org.scoula.openbanking.dto.OpenbankingLinkRequestDTO;
import org.scoula.openbanking.dto.OpenbankingLinkResponseDTO;
import org.scoula.openbanking.service.OpenbankingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/openbanking")
@RequiredArgsConstructor
public class OpenbankingController {

    private final OpenbankingService service;

    // OAuth 인증 URL 발급 — userId 는 임시 @RequestParam
    @GetMapping("/auth-url")
    public ResponseEntity<ApiResponse<AuthUrlResponseDTO>> getAuthUrl(@RequestParam Long userId) {
        String url = service.getAuthUrl(userId);
        return ResponseEntity.ok(ApiResponse.success(new AuthUrlResponseDTO(url)));
    }

    // 계좌 연동 (인증코드 → 토큰 발급 + 저장)
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<OpenbankingLinkResponseDTO>> linkAccount(
            @RequestParam Long userId,
            @RequestBody OpenbankingLinkRequestDTO request) {

        OpenbankingLinkResponseDTO result = service.linkAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }
}
