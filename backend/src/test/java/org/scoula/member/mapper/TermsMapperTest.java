package org.scoula.member.mapper;

import java.util.List;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.config.RootConfig;
import org.scoula.member.domain.TermsAgreementVO;
import org.scoula.member.domain.TermsVO;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Transactional
@Log4j2
class TermsMapperTest {

    @Autowired
    private TermsMapper mapper;

    @Test
    void findAll_returnsSeedTerms() {
        List<TermsVO> terms = this.mapper.findAll();
        assertEquals(5, terms.size());
    }

    @Test
    void findRequiredIds_matchesSeedData() {
        List<Long> requiredIds = this.mapper.findRequiredIds();
        assertEquals(List.of(1L, 2L, 3L), requiredIds);
    }

    @Test
    void insertAgreement_doesNotThrow() {
        TermsAgreementVO agreement = new TermsAgreementVO();
        agreement.setUserId(1L); // kb-data.sql 시드 유저(id=1)
        agreement.setTermsId(1L);
        agreement.setAgreed(true);
        agreement.setTermsVersion("1.0");
        agreement.setCreatedNm("junit");

        assertDoesNotThrow(() -> this.mapper.insertAgreement(agreement));
    }
}
