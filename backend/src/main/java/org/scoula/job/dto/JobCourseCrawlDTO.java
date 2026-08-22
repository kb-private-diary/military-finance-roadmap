package org.scoula.job.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class JobCourseCrawlDTO {

    private String courseName;
    private String providerName;
    private Long originalPrice;
    private Long discountPrice;
    private String detailUrl;
}
