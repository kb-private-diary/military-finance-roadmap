package org.scoula.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberJoinRequestDTO {
    private String userId;
    private String password;
    private String passwordConfirm;
    private String name;
    private String phone;
}
