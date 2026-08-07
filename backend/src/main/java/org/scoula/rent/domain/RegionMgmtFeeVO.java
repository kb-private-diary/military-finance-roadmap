package org.scoula.rent.domain;

import lombok.Data;

/**
 * 시도별 공용관리비 지역계수 : region_mgmt_fee 테이블 한 행을 담는 VO
 * 매물 법정동코드 앞 2자리(시도)로 조회하여 관리비 계산에 주입
 * 순수 데이터 VO (감사컬럼 없음, BaseVO 미상속)
 */
@Data
public class RegionMgmtFeeVO {
    private String sidoCode;     // 시도코드 2자리
    private String sidoName;     // 시도명
    private double mgmtCoef;     // 공용관리비 지역계수 (전국=1.000)
    private Integer sampleCount; // 표본 단지수
}
