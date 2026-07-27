package org.scoula.member.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.scoula.security.account.domain.MemberVO;

public interface MemberMapper {
    MemberVO get(String userId);
    MemberVO findByUserId(String userId); // 아이디 중복 체크시 사용
    int countByPhone(@Param("phone") String phone); // 전화번호 중복 체크시 사용
    // 아이디 찾기 - 동명이인 등으로 이름+전화번호 조합이 여러 건일 수 있어 List로 받는다 (단일 객체면 2건 이상일 때 예외 발생)
    List<MemberVO> findByNameAndPhone(@Param("name") String name, @Param("phone") String phone);
    int insert(MemberVO member);
}
