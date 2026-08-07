package org.scoula.social.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.scoula.common.response.ApiResponse;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.social.dto.SocialBadgeItemDTO;
import org.scoula.social.dto.SocialDistributionItemDTO;
import org.scoula.social.dto.SocialRankingResponseDTO;
import org.scoula.social.dto.SocialStatsResponseDTO;
import org.scoula.social.service.SocialService;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {
    private final SocialService service;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SocialStatsResponseDTO>> findStats(
            @AuthenticationPrincipal final CustomUser customUser,
            @RequestParam(defaultValue = "ALL") final String scope) {
        Long userId = customUser.getMember().getId();
        return ResponseEntity.ok(ApiResponse.success(this.service.findStats(userId, scope)));
    }

    @GetMapping("/distributions")
    public ResponseEntity<ApiResponse<List<SocialDistributionItemDTO>>> findDistributionList(
            @AuthenticationPrincipal final CustomUser customUser,
            @RequestParam(defaultValue = "ALL") final String scope) {
        Long userId = customUser.getMember().getId();
        return ResponseEntity.ok(
                ApiResponse.success(this.service.findDistributionList(userId, scope)));
    }

    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<SocialRankingResponseDTO>> findRanking(
            @AuthenticationPrincipal final CustomUser customUser,
            @RequestParam(defaultValue = "ALL") final String scope) {
        Long userId = customUser.getMember().getId();
        return ResponseEntity.ok(ApiResponse.success(this.service.findRanking(userId, scope)));
    }

    @GetMapping("/badges")
    public ResponseEntity<ApiResponse<List<SocialBadgeItemDTO>>> findBadgeList(
            @AuthenticationPrincipal final CustomUser customUser) {
        Long userId = customUser.getMember().getId();
        return ResponseEntity.ok(ApiResponse.success(this.service.findBadgeList(userId)));
    }
}
