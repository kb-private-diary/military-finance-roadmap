package org.scoula.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindPasswordRequestDTO {
    private String userId;
    private String name;
    private String phone;
    private String newPassword;
    private String newPasswordConfirm;
}
