package org.scoula.bookmark.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.bookmark.dto.BookmarkCreateDTO;
import org.scoula.bookmark.dto.BookmarkCreateRequestDTO;
import org.scoula.bookmark.dto.BookmarkDeleteDTO;
import org.scoula.bookmark.dto.BookmarkResponseDTO;
import org.scoula.bookmark.service.BookmarkService;
import org.scoula.common.response.ApiResponse;
import org.scoula.security.account.domain.CustomUser;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
@Api(tags = "관심 로드맵")
public class BookmarkController {

    private final BookmarkService service;

    // 관심 로드맵 목록 조회
    @GetMapping
    @ApiOperation(
            value = "내 목표 관심항목 조회",
            notes = "로그인 사용자가 관심 등록한 로드맵 목록을 조회한다."
    )
    public ResponseEntity<ApiResponse<List<BookmarkResponseDTO>>> findBookmarkList(
            @AuthenticationPrincipal final CustomUser customUser) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        this.service.findBookmarkList(
                                customUser.getMember().getId()
                        )
                )
        );
    }

    // 관심 로드맵 등록
    @PostMapping
    @ApiOperation(
            value = "관심 로드맵 등록",
            notes = "로드맵 목표를 관심항목으로 등록하고 생성된 bookmarkId를 반환한다."
    )
    public ResponseEntity<ApiResponse<Long>> createBookmark(
            @AuthenticationPrincipal final CustomUser customUser,
            @Valid @RequestBody final BookmarkCreateRequestDTO requestDTO) {

        BookmarkCreateDTO createDTO = BookmarkCreateDTO.builder()
                .userId(customUser.getMember().getId())
                .categoryId(requestDTO.getCategoryId())
                .goalId(requestDTO.getGoalId())
                .createdNm(customUser.getUsername())
                .build();

        Long bookmarkId = this.service.createBookmark(createDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(bookmarkId));
    }

    // 관심 로드맵 삭제
    @DeleteMapping("/{bookmarkId}")
    @ApiOperation(
            value = "관심 로드맵 삭제",
            notes = "관심 등록한 로드맵을 soft delete 처리한다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            @AuthenticationPrincipal final CustomUser customUser,
            @PathVariable final Long bookmarkId) {

        BookmarkDeleteDTO deleteDTO = BookmarkDeleteDTO.builder()
                .bookmarkId(bookmarkId)
                .userId(customUser.getMember().getId())
                .modifiedNm(customUser.getUsername())
                .build();

        this.service.deleteBookmark(deleteDTO);

        return ResponseEntity.ok(ApiResponse.success());
    }
}