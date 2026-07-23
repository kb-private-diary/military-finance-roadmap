package org.scoula.member.controller;

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
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.member.service.MemberService;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class MemberController {
    final MemberService service;

    @GetMapping("/check-id/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkUserId(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(this.service.checkDuplicate(userId)));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Long>> createMember(@RequestBody MemberJoinRequestDTO member) {
        Long id = this.service.createMember(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(id));
    }
}
