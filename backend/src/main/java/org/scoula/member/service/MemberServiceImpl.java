package org.scoula.member.service;

import java.util.Optional;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.scoula.common.exception.BusinessException;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.account.dto.AuthResultDTO;
import org.scoula.security.account.dto.UserInfoDTO;
import org.scoula.security.account.mapper.UserDetailsMapper;
import org.scoula.security.util.JwtProcessor;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberMapper mapper;
    private final JwtProcessor jwtProcessor;
    private final UserDetailsMapper userDetailsMapper;

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

    @Override
    public Long createMember(MemberJoinRequestDTO dto) {
        if (this.checkDuplicate(dto.getUserId())) {
            throw BusinessException.conflict("이미 사용중인 아이디입니다.", "MEM_002");
        }
        MemberVO member = dto.toVO();
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        this.mapper.insert(member);
        return member.getId();
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
