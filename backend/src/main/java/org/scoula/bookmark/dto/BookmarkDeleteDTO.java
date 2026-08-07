package org.scoula.bookmark.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkDeleteDTO {

    private Long bookmarkId;
    private Long userId;
    private String modifiedNm;
}
