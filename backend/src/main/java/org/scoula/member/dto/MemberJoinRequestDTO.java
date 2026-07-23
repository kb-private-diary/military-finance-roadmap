package org.scoula.member.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.security.account.domain.MemberVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberJoinRequestDTO {
    private String userId;
    private String password;
    private String name;
    private String phone;
    private Integer typeId;
    private Integer rankId;
    private String unitName;
    private String unitCode;
    private Date enlistDate;
    private Date dischargeDate;

    public MemberVO toVO() {
        MemberVO vo = new MemberVO();
        vo.setUserId(this.userId);
        vo.setPassword(this.password);
        vo.setName(this.name);
        vo.setPhone(this.phone);
        vo.setTypeId(this.typeId);
        vo.setRankId(this.rankId);
        vo.setUnitName(this.unitName);
        vo.setUnitCode(this.unitCode);
        vo.setEnlistDate(this.enlistDate);
        vo.setDischargeDate(this.dischargeDate);
        vo.setLoginProvider("local");
        vo.setStatus("ACTIVE");
        return vo;
    }
}
