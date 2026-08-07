package org.scoula.bookmark.service;

import org.scoula.bookmark.dto.BookmarkCreateDTO;
import org.scoula.bookmark.dto.BookmarkDeleteDTO;
import org.scoula.bookmark.dto.BookmarkResponseDTO;

import java.util.List;

public interface BookmarkService {

    // 관심 로드맵 목록 조회
    List<BookmarkResponseDTO> findBookmarkList(Long userId);

    // 관심 로드맵 등록
    Long createBookmark(BookmarkCreateDTO createDTO);

    // 관심 로드맵 삭제
    void deleteBookmark(BookmarkDeleteDTO deleteDTO);
}