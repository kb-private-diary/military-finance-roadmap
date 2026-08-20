package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobCategoryVO extends BaseVO {

    private Long categoryId;

    private Long parentId;

    // J01: 취업, J02: 공무원
    private String goalType;

    private String categoryName;

    private String courseMappingCode;

    private String ncsCode;

    // 1: 대분류, 2: 중분류
    private Integer categoryLevel;
}
