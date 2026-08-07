package org.scoula.bookmark.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class BookmarkCreateRequestDTO {

    @NotNull
    @Positive
    private Integer categoryId;

    @NotNull
    @Positive
    private Long goalId;
}
