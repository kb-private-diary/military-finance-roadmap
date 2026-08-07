package org.scoula.bookmark.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkCreateDTO {

    private Long userId;
    private Integer categoryId;
    private Long goalId;
    private String createdNm;
}
