package org.scoula.saving.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.response.ApiResponse;
import org.scoula.saving.dto.SavingAccountCreateRequestDTO;
import org.scoula.saving.service.SavingAccountService;
import org.scoula.security.account.domain.CustomUser;

@RestController
@RequestMapping("/api/saving")
@RequiredArgsConstructor
@Log4j2
public class SavingAccountController {

    private final SavingAccountService service;

    // 군적금 계좌 등록 (오픈뱅킹 연동 시 openbanking 서비스가 내부적으로도 호출)
    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<Long>> createAccount(
            @AuthenticationPrincipal CustomUser customUser,
            @Valid @RequestBody SavingAccountCreateRequestDTO request
    ) {
        Long userId = customUser.getMember().getId();
        log.info("Creating saving account for userId: {}", userId);
        Long accountId = this.service.createAccount(userId, customUser.getUsername(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(accountId));
    }
}
