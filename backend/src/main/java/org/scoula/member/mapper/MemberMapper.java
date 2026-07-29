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
    // 비밀번호 찾기(재설정) - 본인확인(아이디+이름+전화번호) 후 비밀번호 변경
    int updatePassword(@Param("id") Long id, @Param("password") String password, @Param("modifiedNm") String modifiedNm);
    // 마이페이지 - 이름/전화번호/부대정보 수정
    int updateProfile(MemberVO member);
    // 마이페이지 - 회원 탈퇴 (소프트 삭제)
    int withdraw(@Param("id") Long id, @Param("modifiedNm") String modifiedNm);
}
