package org.scoula.member.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.scoula.common.exception.BusinessException;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.security.account.domain.MemberVO;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    final PasswordEncoder passwordEncoder;
    final MemberMapper mapper;

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
}
