package org.scoula.member.service;

import java.util.List;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.common.exception.BusinessException;
import org.scoula.config.RootConfig;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDetailRequestDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.member.dto.TermsDTO;
import org.scoula.security.account.dto.AuthResultDTO;
import org.scoula.security.config.SecurityConfig;
import org.scoula.security.util.JwtProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
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

    @Autowired
    private JwtProcessor jwtProcessor;

    private MemberJoinDetailRequestDTO joinDetailDto(List<Long> agreedTermsIds) {
        return MemberJoinDetailRequestDTO.builder()
                .userId("junit.service@kbthink.com")
                .password("pw1234")
                .passwordConfirm("pw1234")
                .name("서비스테스트")
                .phone("010-2222-2222")
                .typeId(1)
                .rankId(1)
                .agreedTermsIds(agreedTermsIds)
                .build();
    }

    @Test
    void createMember_withAllRequiredTerms_succeeds() {
        Long id = this.service.createMember(joinDetailDto(List.of(1L, 2L, 3L)));

        assertNotNull(id);
        MemberDTO result = this.service.findMember("junit.service@kbthink.com");
        assertEquals("서비스테스트", result.getName());
    }

    @Test
    void createMember_missingRequiredTerm_throws() {
        assertThrows(BusinessException.class,
                () -> this.service.createMember(joinDetailDto(List.of(1L, 2L)))); // 3번(만 14세 확인) 누락
    }

    @Test
    void createMember_duplicateUserId_throws() {
        this.service.createMember(joinDetailDto(List.of(1L, 2L, 3L)));

        assertThrows(BusinessException.class,
                () -> this.service.createMember(joinDetailDto(List.of(1L, 2L, 3L))));
    }

    @Test
    void createMember_passwordMismatch_throws() {
        MemberJoinDetailRequestDTO dto = MemberJoinDetailRequestDTO.builder()
                .userId("junit.mismatch2@kbthink.com")
                .password("pw1234")
                .passwordConfirm("pw9999")
                .name("불일치")
                .phone("010-6666-6666")
                .typeId(1)
                .rankId(1)
                .agreedTermsIds(List.of(1L, 2L, 3L))
                .build();

        assertThrows(BusinessException.class, () -> this.service.createMember(dto));
    }

    @Test
    void checkJoinBasic_duplicateUserId_throws() {
        this.service.createMember(joinDetailDto(List.of(1L, 2L, 3L)));

        MemberJoinRequestDTO basic = new MemberJoinRequestDTO(
                "junit.service@kbthink.com", "pw", "pw", "다른이름", "010-3333-3333");

        assertThrows(BusinessException.class, () -> this.service.checkJoinBasic(basic));
    }

    @Test
    void checkJoinBasic_newUserId_doesNotThrow() {
        MemberJoinRequestDTO basic = new MemberJoinRequestDTO(
                "junit.newbie@kbthink.com", "pw", "pw", "신규", "010-4444-4444");

        this.service.checkJoinBasic(basic); // 예외 없이 통과해야 함
    }

    @Test
    void checkJoinBasic_passwordMismatch_throws() {
        MemberJoinRequestDTO basic = new MemberJoinRequestDTO(
                "junit.mismatch@kbthink.com", "pw1234", "pw5678", "불일치", "010-5555-5555");

        assertThrows(BusinessException.class, () -> this.service.checkJoinBasic(basic));
    }

    @Test
    void findTerms_returnsSeedTerms() {
        List<TermsDTO> terms = this.service.findTerms();

        assertEquals(5, terms.size());
    }

    @Test
    void refresh_withValidRefreshToken_returnsNewTokenPair() {
        this.service.createMember(joinDetailDto(List.of(1L, 2L, 3L)));
        String refreshToken = this.jwtProcessor.generateRefreshToken("junit.service@kbthink.com");

        AuthResultDTO result = this.service.refresh(refreshToken);

        assertNotNull(result.getToken());
        assertNotNull(result.getRefreshToken());
        assertEquals("junit.service@kbthink.com", result.getUser().getUserId());
    }

    @Test
    void refresh_withAccessTokenInsteadOfRefreshToken_throws() {
        this.service.createMember(joinDetailDto(List.of(1L, 2L, 3L)));
        String accessToken = this.jwtProcessor.generateToken("junit.service@kbthink.com");

        assertThrows(BadCredentialsException.class, () -> this.service.refresh(accessToken));
    }
}
