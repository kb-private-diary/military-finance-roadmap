package org.scoula.bookmark.mapper;

import org.scoula.bookmark.domain.BookmarkVO;
import org.scoula.bookmark.dto.BookmarkCreateDTO;
import org.scoula.bookmark.dto.BookmarkDeleteDTO;

import java.util.List;

public interface BookmarkMapper {

    // 로그인 사용자의 관심 로드맵 목록 조회
    List<BookmarkVO> findBookmarkListByUserId(Long userId);

    // 같은 카테고리에 이미 관심 등록한 로드맵이 있는지 조회
    BookmarkVO findBookmarkByUserIdAndCategoryId(
            BookmarkCreateDTO createDTO
    );

    // 관심 로드맵 등록
    void insertBookmark(BookmarkVO bookmarkVO);

    // 관심 로드맵 소프트 삭제
    int deleteBookmark(BookmarkDeleteDTO deleteDTO);
}