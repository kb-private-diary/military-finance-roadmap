package org.scoula.member.service;

import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.security.account.dto.AuthResultDTO;

public interface MemberService {
    boolean checkDuplicate(String userId);
    MemberDTO findMember(String userId);
    Long createMember(MemberJoinRequestDTO member);
    AuthResultDTO refresh(String refreshToken);
}
