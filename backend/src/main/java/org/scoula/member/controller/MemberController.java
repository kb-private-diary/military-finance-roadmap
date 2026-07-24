package org.scoula.member.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.scoula.common.response.ApiResponse;
import org.scoula.member.dto.MemberJoinDetailRequestDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.member.dto.TermsDTO;
import org.scoula.member.service.MemberService;
import org.scoula.security.account.dto.AuthResultDTO;
import org.scoula.security.account.dto.RefreshRequestDTO;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class MemberController {
    private final MemberService service;

    @GetMapping("/check-id/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkUserId(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(this.service.checkDuplicate(userId)));
    }

    //기본정보 검증만 하고 계정 생성x
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Void>> checkJoinBasic(@RequestBody MemberJoinRequestDTO basic) {
        this.service.checkJoinBasic(basic);
        return ResponseEntity.ok(ApiResponse.success());
    }

    //1단계 정보와 상세정보를 합쳐 계정을 생성
    @PostMapping("/join/detail")
    public ResponseEntity<ApiResponse<Long>> createMember(@RequestBody MemberJoinDetailRequestDTO member) {
        Long id = this.service.createMember(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(id));
    }

    @GetMapping("/terms")
    public ResponseEntity<ApiResponse<List<TermsDTO>>> findTerms() {
        return ResponseEntity.ok(ApiResponse.success(this.service.findTerms()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResultDTO>> refresh(@RequestBody RefreshRequestDTO request) {
        AuthResultDTO result = this.service.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success());
    }
}
