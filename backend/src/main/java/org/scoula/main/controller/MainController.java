package org.scoula.main.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.response.ApiResponse;
import org.scoula.main.dto.MainSummaryResponseDTO;
import org.scoula.main.service.MainService;
import org.scoula.security.account.domain.CustomUser;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
@Log4j2
public class MainController {

    private final MainService service;

    // 메인 요약 조회
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MainSummaryResponseDTO>> findSummary(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        Long userId = customUser.getMember().getId();
        log.info("Fetching main summary for userId: {}", userId);

        MainSummaryResponseDTO dto = this.service.findSummary(userId);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
