package org.scoula.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.security.account.domain.MemberVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private Long id;
    private String userId;
    private String name;
    private String phone;
    private Integer typeId;
    private Integer rankId;
    private String unitName;
    private String unitCode;
    private Date enlistDate;
    private Date dischargeDate;
    private String status;

    public static MemberDTO of(MemberVO m) {
        return MemberDTO.builder()
                .id(m.getId())
                .userId(m.getUserId())
                .name(m.getName())
                .phone(m.getPhone())
                .typeId(m.getTypeId())
                .rankId(m.getRankId())
                .unitName(m.getUnitName())
                .unitCode(m.getUnitCode())
                .enlistDate(m.getEnlistDate())
                .dischargeDate(m.getDischargeDate())
                .status(m.getStatus())
                .build();
    }
}
