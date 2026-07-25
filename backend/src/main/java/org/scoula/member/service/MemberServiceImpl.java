package org.scoula.member.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.scoula.common.exception.BusinessException;
import org.scoula.member.domain.TermsAgreementVO;
import org.scoula.member.domain.TermsVO;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDetailRequestDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.member.dto.TermsDTO;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.member.mapper.TermsMapper;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.account.dto.AuthResultDTO;
import org.scoula.security.account.dto.UserInfoDTO;
import org.scoula.security.account.mapper.UserDetailsMapper;
import org.scoula.security.util.JwtProcessor;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    // 아이디를 이메일 형식으로 받는다 (로컬파트 + @ + 도메인)
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    // 8자 이상 + 숫자 1개 이상 + 특수문자(영문·숫자 외 문자) 1개 이상
    private static final Pattern PASSWORD_HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern PASSWORD_HAS_SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    private final PasswordEncoder passwordEncoder;
    private final MemberMapper mapper;
    private final TermsMapper termsMapper;
    private final JwtProcessor jwtProcessor;
    private final UserDetailsMapper userDetailsMapper;

    private void validateEmailFormat(String userId) {
        if (userId == null || !EMAIL_PATTERN.matcher(userId).matches()) {
            throw BusinessException.badRequest("사용할 수 없는 아이디입니다.", "MEM_005");
        }
    }

    private void validatePasswordPolicy(String password) {
        boolean valid = password != null
                && password.length() >= 8
                && PASSWORD_HAS_DIGIT.matcher(password).matches()
                && PASSWORD_HAS_SPECIAL.matcher(password).matches();
        if (!valid) {
            throw BusinessException.badRequest(
                    "비밀번호는 8자 이상, 숫자와 특수문자를 포함해야 합니다.", "MEM_006");
        }
    }

    @Override
    public boolean checkDuplicate(String userId) {
        return this.mapper.findByUserId(userId) != null;
    }

    @Override
    public MemberDTO findMember(String userId) {
        MemberVO member = Optional.ofNullable(this.mapper.get(userId))
                .orElseThrow(() -> BusinessException.notFound("일치하는 정보가 없습니다.", "MEM_001"));
        return MemberDTO.of(member);
    }

    // 1단계: 기본정보만 확인하고 계정 생성x, 실제 생성은 createMember(2단계)
    @Override
    public void checkJoinBasic(MemberJoinRequestDTO basic) {
        this.validateEmailFormat(basic.getUserId());
        if (this.checkDuplicate(basic.getUserId())) {
            throw BusinessException.conflict("이미 사용중인 아이디입니다.", "MEM_002");
        }
        this.validatePasswordPolicy(basic.getPassword());
        if (!basic.getPassword().equals(basic.getPasswordConfirm())) {
            throw BusinessException.badRequest("비밀번호가 일치하지 않습니다.", "MEM_004");
        }
    }

    @Transactional
    @Override
    public Long createMember(MemberJoinDetailRequestDTO dto) {
        // 1단계(checkJoinBasic)를 거치지 않고 바로 호출되는 경우까지 대비해 실제 계정 생성 시점에도 재검증한다.
        this.validateEmailFormat(dto.getUserId());
        if (this.checkDuplicate(dto.getUserId())) {
            throw BusinessException.conflict("이미 사용중인 아이디입니다.", "MEM_002");
        }
        this.validatePasswordPolicy(dto.getPassword());
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw BusinessException.badRequest("비밀번호가 일치하지 않습니다.", "MEM_004");
        }

        List<Long> requiredTermsIds = this.termsMapper.findRequiredIds();
        List<Long> agreedTermsIds = dto.getAgreedTermsIds() == null ? List.of() : dto.getAgreedTermsIds();
        if (!agreedTermsIds.containsAll(requiredTermsIds)) {
            throw BusinessException.badRequest("필수 약관에 모두 동의해야 합니다.", "MEM_003");
        }

        MemberVO member = dto.toVO();
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        this.mapper.insert(member);

        if (!agreedTermsIds.isEmpty()) {
            Map<Long, String> versionByTermsId = this.termsMapper.findAll().stream()
                    .collect(Collectors.toMap(TermsVO::getTermsId, TermsVO::getVersion));
            for (Long termsId : agreedTermsIds) {
                TermsAgreementVO agreement = new TermsAgreementVO();
                agreement.setUserId(member.getId());
                agreement.setTermsId(termsId);
                agreement.setAgreed(true);
                agreement.setTermsVersion(versionByTermsId.get(termsId));
                agreement.setCreatedNm(member.getUserId());
                this.termsMapper.insertAgreement(agreement);
            }
        }

        return member.getId();
    }

    @Override
    public List<TermsDTO> findTerms() {
        return this.termsMapper.findAll().stream().map(TermsDTO::of).toList();
    }

    @Override
    public AuthResultDTO refresh(String refreshToken) {
        // 한 번만 파싱해서 얻은 Claims를 재활용 - 유효성 검증 + refresh여부 확인 + username추출을 각각 다시 파싱하지 않는다.
        Claims claims = this.jwtProcessor.parseClaims(refreshToken);
        if (!this.jwtProcessor.isRefreshToken(claims)) {
            throw new BadCredentialsException("refresh token이 아닙니다.");
        }

        String userId = this.jwtProcessor.getUsername(claims);
        MemberVO member = this.userDetailsMapper.get(userId);
        if (member == null) {
            throw new UsernameNotFoundException(userId + "은 없는 id입니다.");
        }

        String newAccessToken = this.jwtProcessor.generateToken(userId);
        String newRefreshToken = this.jwtProcessor.generateRefreshToken(userId);
        return new AuthResultDTO(newAccessToken, newRefreshToken, UserInfoDTO.of(member));
    }
}
