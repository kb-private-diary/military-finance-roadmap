package org.scoula.openbanking.controller;

import java.util.List;

import org.scoula.common.response.ApiResponse;
import org.scoula.openbanking.dto.AccountInfo;
import org.scoula.openbanking.dto.AuthUrlResponse;
import org.scoula.openbanking.dto.LinkRequest;
import org.scoula.openbanking.service.OpenBankingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

/**
 * 오픈뱅킹 연동 API
 * 온보딩(연동 여부 확인 → 인증 URL 발급 → 계좌 연동)과 연동 해제를 제공
 */
@RestController
@RequestMapping("/api/openbanking")
@RequiredArgsConstructor
public class OpenBankingController {

    private final OpenBankingService service;

    // GET /api/openbanking/status → 연동 여부 (온보딩 분기용, true면 서비스 이용 가능)
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> status(@RequestParam Long userId) { // TODO: JWT 연동
        return ResponseEntity.ok(ApiResponse.success(service.hasLink(userId)));
    }

    // GET /api/openbanking/auth-url → 인증 URL 발급 (프론트가 이 URL로 리다이렉트)
    @GetMapping("/auth-url")
    public ResponseEntity<ApiResponse<AuthUrlResponse>> authUrl(@RequestParam Long userId) { // TODO: JWT 연동
        return ResponseEntity.ok(ApiResponse.success(service.getAuthUrl(userId)));
    }

    // POST /api/openbanking/link → 선택한 계좌 연동 (연동된 계좌 목록 반환)
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<List<AccountInfo>>> link(
            @RequestParam Long userId, // TODO: JWT 연동
            @RequestBody LinkRequest request) {

        List<AccountInfo> linked = service.linkAccounts(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(linked));
    }

    // DELETE /api/openbanking/link → 연동 전체 해제
    @DeleteMapping("/link")
    public ResponseEntity<ApiResponse<Void>> unlink(@RequestParam Long userId) { // TODO: JWT 연동
        service.unlink(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // POST /api/openbanking/sync → 거래내역을 지출로 동기화 (적재된 건수 반환)
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Integer>> syncTransactions(@RequestParam Long userId) { // TODO: JWT 연동
        int count = service.syncTransactions(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
