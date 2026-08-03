package org.scoula.member.mapper;

import java.util.List;

import org.scoula.member.domain.MilitaryUnitVO;

public interface MilitaryUnitMapper {
    List<MilitaryUnitVO> findMilitaryUnitListByTypeId(Integer typeId);
}
