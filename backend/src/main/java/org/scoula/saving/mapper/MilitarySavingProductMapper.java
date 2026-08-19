package org.scoula.saving.mapper;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;

import org.scoula.saving.dto.MilitarySavingRateDTO;
import org.scoula.saving.dto.MilitarySavingWithdrawalRateDTO;

// military_saving_product(군적금 상품 금리표) 조회용 매퍼
public interface MilitarySavingProductMapper {
    // bankCode + 총 가입기간(개월) 기준 만기 구간(기본금리·정부매칭비율) 조회. 해당 구간 없으면 null(무이자 구간, 예: 0~1개월)
    MilitarySavingRateDTO findMaturityRate(
            @Param("bankCode") String bankCode,
            @Param("totalMaturityMonths") Integer totalMaturityMonths);

    // bankCode + 회차별 경과개월/경과비율 기준 중도해지 구간 조회.
    // 은행마다 MONTH(경과개월) 또는 RATIO(경과비율) 중 하나만 쓰므로 둘 다 넘기면 해당하는 쪽으로 매칭된다.
    MilitarySavingWithdrawalRateDTO findWithdrawalRate(
            @Param("bankCode") String bankCode,
            @Param("elapsedMonths") Integer elapsedMonths,
            @Param("elapsedRatio") BigDecimal elapsedRatio);

    // bankCode의 월 최소납입한도. 상품 데이터가 없거나 하한이 없으면 null(호출부에서 0으로 처리)
    Long findMinLimit(@Param("bankCode") String bankCode);

    // bankCode의 최대 가입가능 개월수(military_saving_product.max_join_month). 상품 데이터가 없으면 null
    Integer findMaxJoinMonth(@Param("bankCode") String bankCode);
}
