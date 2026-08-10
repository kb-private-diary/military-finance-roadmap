package org.scoula.bookmark.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.bookmark.domain.BookmarkVO;
import org.scoula.bookmark.dto.BookmarkCreateDTO;
import org.scoula.bookmark.dto.BookmarkDeleteDTO;
import org.scoula.bookmark.dto.BookmarkResponseDTO;
import org.scoula.bookmark.mapper.BookmarkMapper;
import org.scoula.common.exception.BusinessException;

@Service
@RequiredArgsConstructor
@Log4j2
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkMapper bookmarkMapper;

    // 관심 로드맵 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> findBookmarkList(Long userId) {
        return this.bookmarkMapper.findBookmarkListByUserId(userId)
                .stream()
                .map(BookmarkResponseDTO::of)
                .toList();
    }

    // 관심 로드맵 등록
    @Override
    @Transactional
    public Long createBookmark(BookmarkCreateDTO createDTO) {

        BookmarkVO existingBookmark =
                this.bookmarkMapper.findBookmarkByUserIdAndGoal(createDTO);

        if (existingBookmark != null) {
            throw BusinessException.conflict(
                    "이미 관심 등록한 로드맵입니다",
                    "BOOK_001"
            );
        }

        if (existingBookmark != null) {
            throw BusinessException.conflict(
                    "해당 카테고리에 이미 관심 등록한 로드맵이 있습니다",
                    "BOOK_001"
            );
        }

        BookmarkVO bookmarkVO = new BookmarkVO();
        bookmarkVO.setUserId(createDTO.getUserId());
        bookmarkVO.setCategoryId(createDTO.getCategoryId());
        bookmarkVO.setGoalId(createDTO.getGoalId());
        bookmarkVO.setCreatedNm(createDTO.getCreatedNm());

        this.bookmarkMapper.insertBookmark(bookmarkVO);

        return bookmarkVO.getBookmarkId();
    }

    // 관심 로드맵 삭제
    @Override
    @Transactional
    public void deleteBookmark(BookmarkDeleteDTO deleteDTO) {

        int updatedCount =
                this.bookmarkMapper.deleteBookmark(deleteDTO);

        if (updatedCount == 0) {
            throw BusinessException.notFound(
                    "관심 등록한 로드맵을 찾을 수 없습니다",
                    "BOOK_002"
            );
        }
    }
}