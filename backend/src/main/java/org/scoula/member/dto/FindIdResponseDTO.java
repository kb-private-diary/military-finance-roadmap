package org.scoula.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindIdResponseDTO {
    private String maskedUserId; // 개인정보 보호를 위해 일부 마스킹된 아이디
}
