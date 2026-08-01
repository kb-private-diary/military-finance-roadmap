package org.scoula.member.mapper;

import java.util.List;

import org.scoula.member.domain.MilitaryTypeVO;

public interface MilitaryTypeMapper {
    List<MilitaryTypeVO> findMilitaryTypeList();
}
