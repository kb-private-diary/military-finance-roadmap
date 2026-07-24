package org.scoula.member.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TermsVO extends BaseVO {
    private Long termsId;
    private String name;
    private Boolean required;
    private String content;
    private String version;
}
