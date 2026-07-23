package org.scoula.security.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.security.account.domain.MemberVO;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInfoDTO {
    Long id;
    String userId;
    String name;
    String phone;
    String status;

    public static UserInfoDTO of(MemberVO member) {
        return new UserInfoDTO(
                member.getId(),
                member.getUserId(),
                member.getName(),
                member.getPhone(),
                member.getStatus()
        );
    }
}
