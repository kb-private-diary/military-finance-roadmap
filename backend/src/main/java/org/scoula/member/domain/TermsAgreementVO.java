package org.scoula.member.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TermsAgreementVO extends BaseVO {
    private Long agreementId;
    private Long userId;
    private Long termsId;
    private Boolean agreed;
    private LocalDateTime agreedDate;
    private String termsVersion;
}
