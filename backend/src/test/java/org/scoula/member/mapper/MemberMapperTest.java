package org.scoula.member.mapper;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.config.RootConfig;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Transactional
@Log4j2
class MemberMapperTest {

    @Autowired
    private MemberMapper mapper;

    private MemberVO newMember() {
        MemberVO vo = new MemberVO();
        vo.setUserId("junit.test@kbthink.com");
        vo.setPassword("encoded-pw");
        vo.setName("준잇테스트");
        vo.setPhone("010-0000-0000");
        vo.setTypeId(1);
        vo.setRankId(1);
        vo.setEnlistDate(LocalDate.now());
        vo.setDischargeDate(LocalDate.now());
        vo.setLoginProvider("local");
        vo.setStatus("ACTIVE");
        return vo;
    }

    @Test
    void insertAndGet() {
        MemberVO vo = newMember();
        mapper.insert(vo);
        assertNotNull(vo.getId());

        MemberVO found = mapper.get("junit.test@kbthink.com");
        assertNotNull(found);
        assertEquals("준잇테스트", found.getName());
    }

    @Test
    void findByUserId_duplicateCheck() {
        assertNull(mapper.findByUserId("junit.test@kbthink.com"));

        mapper.insert(newMember());

        assertNotNull(mapper.findByUserId("junit.test@kbthink.com"));
    }
}
