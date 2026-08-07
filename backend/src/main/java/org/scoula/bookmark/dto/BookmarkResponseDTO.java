package org.scoula.bookmark.dto;

import lombok.Builder;
import lombok.Getter;
import org.scoula.bookmark.domain.BookmarkVO;

@Getter
@Builder
public class BookmarkResponseDTO {

    private Long bookmarkId;
    private Integer categoryId;
    private Long goalId;

    public static BookmarkResponseDTO of(BookmarkVO bookmarkVO) {

        return BookmarkResponseDTO.builder()
                .bookmarkId(bookmarkVO.getBookmarkId())
                .categoryId(bookmarkVO.getCategoryId())
                .goalId(bookmarkVO.getGoalId())
                .build();
    }
}
