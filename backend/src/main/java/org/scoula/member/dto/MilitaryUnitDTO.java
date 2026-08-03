package org.scoula.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.member.domain.MilitaryUnitVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilitaryUnitDTO {
    private String unitCode;
    private String unitName;
    private Integer typeId;

    public static MilitaryUnitDTO of(MilitaryUnitVO vo) {
        return new MilitaryUnitDTO(vo.getUnitCode(), vo.getUnitName(), vo.getTypeId());
    }
}
