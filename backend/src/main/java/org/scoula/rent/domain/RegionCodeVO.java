package org.scoula.rent.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 법정동 코드 마스터. region_code 테이블 한 행을 담는 VO.
 * 지역 3단계 선택(시도 → 시군구 → 읍면동)과 매물 조회에 사용한다.
 * 감사컬럼 5개는 BaseVO 상속.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegionCodeVO extends BaseVO {
    private String regionCode;   // 법정동코드 (PK)
    private String sidoName;     // 시도명
    private String sigunguName;  // 시군구명
    private String umdName;      // 읍면동명
    private String sigunguCode;  // 시군구코드
    private String isAbolished;  // 폐지여부 Y / N
}
