package org.scoula.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialDistributionItemDTO {
    private Integer categoryId;
    private String categoryName;
    private Long interestCount;
    private Double percentage;
}
