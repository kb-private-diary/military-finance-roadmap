package org.scoula.member.mapper;

import java.util.List;

import org.scoula.member.domain.TermsAgreementVO;
import org.scoula.member.domain.TermsVO;

public interface TermsMapper {
    List<TermsVO> findAll();
    List<Long> findRequiredIds();
    int insertAgreement(TermsAgreementVO agreement);
}
