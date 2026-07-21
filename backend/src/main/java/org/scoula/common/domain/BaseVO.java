package org.scoula.common.domain;

import lombok.Data;

import java.util.Date;

/**
 * 모든 테이블 공통 감사(audit) 컬럼을 담는 상위 VO.
 * DB 52개 테이블이 공통으로 가지는 5개 컬럼을 중복 없이 상속받아 사용한다.
 *
 * <p>사용법: 각 도메인 VO가 {@code extends BaseVO} 하면 아래 컬럼이 자동 매핑된다.
 * MyBatis 는 {@code mapUnderscoreToCamelCase=true} 설정으로 snake_case ↔ camelCase 자동 변환.</p>
 *
 * <p>[소프트 삭제 규칙] 물리 삭제하지 않고 {@code delYn = 'Y'} 로 UPDATE 하며,
 * 삭제자·삭제일시는 {@code modifiedNm}·{@code modifiedDate} 에 기록한다.</p>
 */
@Data
public class BaseVO {
    private Date createdDate;    // created_date  생성일시
    private String createdNm;    // created_nm    생성자
    private Date modifiedDate;   // modified_date 수정일시(삭제 시 삭제일시)
    private String modifiedNm;   // modified_nm   수정자(삭제 시 삭제자)
    private String delYn;        // del_yn        삭제여부 'Y'/'N'
}
