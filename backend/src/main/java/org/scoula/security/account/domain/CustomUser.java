package org.scoula.security.account.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
@Setter
public class CustomUser extends User {
    private MemberVO member;

    public CustomUser(MemberVO vo) {
        super(vo.getUserId(), vo.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.member = vo;
    }
}
