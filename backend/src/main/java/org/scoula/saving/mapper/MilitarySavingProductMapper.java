package org.scoula.saving.mapper;

import org.apache.ibatis.annotations.Param;

import org.scoula.saving.dto.MilitarySavingRateDTO;

// military_saving_product(군적금 상품 금리표) 조회용 매퍼
public interface MilitarySavingProductMapper {
    // bankCode + 총 가입기간(개월) 기준 만기 구간(기본금리·정부매칭비율) 조회. 해당 구간 없으면 null(무이자 구간, 예: 0~1개월)
    MilitarySavingRateDTO findMaturityRate(
            @Param("bankCode") String bankCode,
            @Param("totalMaturityMonths") Integer totalMaturityMonths);
}
