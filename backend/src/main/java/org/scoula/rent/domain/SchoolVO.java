package org.scoula.rent.domain;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.common.domain.BaseVO;

/**
 * 학교 마스터. school 테이블 한 행을 담는 VO.
 * 통학 반경 매물 검색(위경도 기반 거리 계산)에 사용한다.
 * 감사컬럼 5개는 BaseVO 상속.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SchoolVO extends BaseVO {
    private Long schoolId;        // 학교번호 (PK)
    private String schoolName;    // 학교명
    private String schoolType;    // 학교유형 UNIVERSITY / COLLEGE / GRADUATE
    private String address;       // 주소
    private String sigunguCode;   // 시군구코드 (매물 조인용)
    private String regionCode;    // 법정동코드
    private BigDecimal latitude;  // 위도 (DECIMAL, 거리 계산 정확도 위해 BigDecimal)
    private BigDecimal longitude; // 경도
}
