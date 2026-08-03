package org.scoula.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.member.domain.MilitaryTypeVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilitaryTypeDTO {
    private Integer typeId;
    private String typeName;

    public static MilitaryTypeDTO of(MilitaryTypeVO vo) {
        return new MilitaryTypeDTO(vo.getTypeId(), vo.getTypeName());
    }
}
