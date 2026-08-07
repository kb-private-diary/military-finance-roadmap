package org.scoula.bookmark.domain;

import lombok.Data;
import org.scoula.common.domain.BaseVO;

@Data
public class BookmarkVO extends BaseVO {

    private Long bookmarkId;
    private Long userId;
    private Integer categoryId;
    private Long goalId;
}
