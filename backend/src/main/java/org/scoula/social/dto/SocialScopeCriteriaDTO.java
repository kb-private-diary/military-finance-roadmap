package org.scoula.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialScopeCriteriaDTO {
    private Long userId;
    private String scope;
    private Integer typeId;
    private String unitCode;
    // 모든 비교 범위에 공통으로 얹히는 조건. 같은 계급끼리만 비교한다.
    private Integer rankId;
    private Boolean veteran;
}
