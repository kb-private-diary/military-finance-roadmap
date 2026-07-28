package org.scoula.member.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.security.account.domain.MemberVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberJoinDetailRequestDTO {
    private String userId;
    private String password;
    private String passwordConfirm;
    private String name;
    private String phone;
    private Integer typeId;
    private Integer rankId;
    private String unitName;
    private String unitCode;
    private LocalDate enlistDate;
    private LocalDate dischargeDate;
    private List<Long> agreedTermsIds;

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
