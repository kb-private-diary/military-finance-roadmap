package org.scoula.member.mapper;

import org.scoula.security.account.domain.MemberVO;

public interface MemberMapper {
    MemberVO get(String userId);
    MemberVO findByUserId(String userId); // 아이디 중복 체크시 사용
    int insert(MemberVO member);
}
