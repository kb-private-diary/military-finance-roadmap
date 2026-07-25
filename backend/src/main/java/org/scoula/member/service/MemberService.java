package org.scoula.member.service;

import java.util.List;

import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDetailRequestDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.member.dto.TermsDTO;
import org.scoula.security.account.dto.AuthResultDTO;

public interface MemberService {
    boolean checkDuplicate(String userId);
    MemberDTO findMember(String userId);
    void checkJoinBasic(MemberJoinRequestDTO basic);
    Long createMember(MemberJoinDetailRequestDTO member);
    List<TermsDTO> findTerms();
    AuthResultDTO refresh(String refreshToken);
}
