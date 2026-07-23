package org.scoula.member.service;

import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;

public interface MemberService {
    boolean checkDuplicate(String userId);
    MemberDTO findMember(String userId);
    Long createMember(MemberJoinRequestDTO member);
}
