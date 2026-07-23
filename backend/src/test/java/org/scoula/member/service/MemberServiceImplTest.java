package org.scoula.member.service;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.common.exception.BusinessException;
import org.scoula.config.RootConfig;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Transactional
@Log4j2
class MemberServiceImplTest {

    @Autowired
    private MemberService service;

    private MemberJoinRequestDTO joinDto() {
        return MemberJoinRequestDTO.builder()
                .userId("junit.service@kbthink.com")
                .password("pw1234")
                .name("서비스테스트")
                .phone("010-2222-2222")
                .typeId(1)
                .rankId(1)
                .build();
    }

    @Test
    void createMember_succeeds() {
        Long id = this.service.createMember(joinDto());

        assertNotNull(id);
        MemberDTO result = this.service.findMember("junit.service@kbthink.com");
        assertEquals("서비스테스트", result.getName());
    }

    @Test
    void createMember_duplicateUserId_throws() {
        this.service.createMember(joinDto());

        assertThrows(BusinessException.class, () -> this.service.createMember(joinDto()));
    }
}
