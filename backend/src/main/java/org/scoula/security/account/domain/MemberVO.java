package org.scoula.security.account.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scoula.common.domain.BaseVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MemberVO extends BaseVO {
    private Long id;
    private String userId;
    private String password;
    private String name;
    private String phone;
    private Integer typeId;
    private Integer rankId;
    private String unitName;
    private String unitCode;
    private LocalDate enlistDate;
    private LocalDate dischargeDate;
    private String loginProvider;
    private String status;
    private LocalDateTime withdrawnAt;
}
