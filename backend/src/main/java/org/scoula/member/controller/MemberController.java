package org.scoula.member.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.scoula.common.response.ApiResponse;
import org.scoula.member.dto.ChangePasswordRequestDTO;
import org.scoula.member.dto.FindIdRequestDTO;
import org.scoula.member.dto.FindIdResponseDTO;
import org.scoula.member.dto.FindPasswordRequestDTO;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDetailRequestDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.member.dto.MemberUpdateRequestDTO;
import org.scoula.member.dto.MilitaryTypeDTO;
import org.scoula.member.dto.MilitaryUnitDTO;
import org.scoula.member.dto.TermsDTO;
import org.scoula.member.dto.WithdrawRequestDTO;
import org.scoula.member.service.MemberService;
import org.scoula.security.account.domain.CustomUser;
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

    //회원가입 - 군종 목록 조회 (SCR-COM-05 군종 드롭다운용)
    @GetMapping("/military-types")
    public ResponseEntity<ApiResponse<List<MilitaryTypeDTO>>> findMilitaryTypeList() {
        return ResponseEntity.ok(ApiResponse.success(this.service.findMilitaryTypeList()));
    }

    //회원가입 - 군종별 부대 목록 조회 (SCR-COM-05 부대 드롭다운용)
    @GetMapping("/military-units")
    public ResponseEntity<ApiResponse<List<MilitaryUnitDTO>>> findMilitaryUnitListByTypeId(
            @RequestParam Integer typeId) {
        return ResponseEntity.ok(
                ApiResponse.success(this.service.findMilitaryUnitListByTypeId(typeId)));
    }

    //이름+전화번호로 아이디 찾기 (마스킹된 아이디 반환)
    @PostMapping("/find-id")
    public ResponseEntity<ApiResponse<FindIdResponseDTO>> findUserId(@RequestBody FindIdRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(this.service.findUserId(request)));
    }

    //본인확인(아이디+이름+전화번호) 후 비밀번호 재설정
    @PostMapping("/find-pw")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody FindPasswordRequestDTO request) {
        this.service.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    //마이페이지 - 내 정보 조회 (SCR-MYP-01)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberDTO>> findMyInfo(@AuthenticationPrincipal CustomUser customUser) {
        return ResponseEntity.ok(ApiResponse.success(this.service.findMember(customUser.getUsername())));
    }

    //마이페이지 - 회원정보 수정 (SCR-MYP-02)
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyInfo(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody MemberUpdateRequestDTO request) {
        this.service.updateMember(customUser.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    //마이페이지 - 비밀번호 변경 (SCR-MYP-03, 로그인 상태에서 현재 비밀번호 확인 후 변경)
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody ChangePasswordRequestDTO request) {
        this.service.changePassword(customUser.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    //마이페이지 - 회원 탈퇴 (SCR-MYP-04, 비밀번호 확인 후 소프트 삭제)
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody WithdrawRequestDTO request) {
        this.service.withdraw(customUser.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success());
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
