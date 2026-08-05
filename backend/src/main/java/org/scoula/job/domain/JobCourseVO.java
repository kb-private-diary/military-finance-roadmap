package org.scoula.job.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobCourseVO extends BaseVO {

    private Long qualId;
    private Long courseId;
    // C01: 개별강의, C02: 공무원패스, C03: 편입패스
    private String courseType;
    private String providerName;
    private String courseName;

    private Long originalPrice;
    private Long discountPrice;
    private Long militaryPrice;

    private Long selectedCost;

    private String benefitDetail;
    private String detailUrl;
}
