package org.scoula.rent.mapper;

import org.scoula.rent.domain.RegionCodeVO;
import java.util.List;

public interface RentMapper {
    // 시/도 목록 (중복 제거)
    List<RegionCodeVO> getSidoList();

    // 특정 시/도의 시/군/구 목록
    List<RegionCodeVO> getSigunguListBySido(String sidoName);

    // 특정 시/군/구의 읍/면/동 목록
    List<RegionCodeVO> getUmdListBySigunguCode(String sigunguCode);
}