package org.scoula.rent.mapper;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;
import org.scoula.rent.domain.HousingProductVO;
import org.scoula.rent.domain.MilitaryServiceVO;

import java.util.List;

/**
 * 주거 금융상품(housing_product) 매퍼 - 월세 매물 조건 기반 추천 조회
 * (테이블 DDL/시딩은 housing_product_schema.sql 로 DB 에서 실행. 여기선 조회만)
 */
public interface HousingProductMapper {

    /**
     * 매물 조건에 맞는 금융상품 조회 (명세 3장 필터)
     * - 지역: region_code 가 NULL(전국)이거나 매물 시도코드(앞 2자리)와 일치
     * - 매물조건: deposit_limit/monthly_limit/area_limit 이 NULL 이면 통과, 있으면 매물값 초과 시 제외
     * - 나이 필터는 하지 않는다 (user 에 생년월일이 없어 나이를 계산할 수 없음)
     * - 정렬: priority 오름차순 (만료 상품 하단 내림은 서비스단에서 처리)
     */
    List<HousingProductVO> findProducts(@Param("regionCode") String regionCode,
                                        @Param("deposit") Long deposit,
                                        @Param("monthlyRent") Long monthlyRent,
                                        @Param("areaSqm") BigDecimal areaSqm);

    /** 사용자 복무기간(입대일·전역예정일) 조회 - 병역 혜택 안내용 */
    MilitaryServiceVO findServicePeriodByUserId(@Param("userId") Long userId);
}
