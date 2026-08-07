package org.scoula.rent.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 주거 금융상품 : housing_product 테이블 한 행을 담는 VO (월세 전용)
 * 주택도시기금·국토교통부·KB국민은행·각 지자체 상품을 담는다
 * 감사 5컬럼(created_date 등)이 아니라 created_at/updated_at 만 있어 BaseVO 는 상속하지 않는다
 */
@Data
public class HousingProductVO {
    private Long productId;          // 상품 PK
    private String productName;      // 상품/사업명
    private String productType;      // POLICY(정책) / LOCAL(지자체) / BANK(시중)
    private String supportType;      // GRANT(지원금) / LOAN(대출) / INTEREST(이자지원) / COST(비용지원)
    private String loanType;         // DEPOSIT / MONTHLY / BOTH, GRANT 는 NULL
    private String provider;         // 주택도시기금 / 국토교통부 / KB국민은행 / 서울시 등
    private String isKb;             // KB 상품 우선 노출용 'Y'/'N'
    private String regionCode;       // 법정동 시도코드 2자리. NULL 이면 전국

    private BigDecimal rateMin;      // 최저 금리 %. GRANT 는 NULL
    private BigDecimal rateMax;      // 최고 금리 %
    private String rateSummary;      // 화면 표시용 문자열
    private Integer supportAmount;   // 지원금 월 최대액(원). GRANT/INTEREST 용
    private Integer supportMonths;   // 지원 개월수
    private Long loanLimit;          // 대출 한도(원)

    private Integer minAge;          // 최소 나이
    private Integer maxAge;          // 최대 나이
    private Integer veteranExtend;   // 병역 이행 시 연령 상한 연장(년). 0 이면 없음
    private String veteranNote;      // 병역 혜택 상세 (표시용)
    private Long depositLimit;       // 임차보증금 상한(원). 초과 시 추천 제외
    private Integer monthlyLimit;    // 월세 상한(원). 초과 시 추천 제외
    private BigDecimal areaLimit;    // 전용면적 상한(㎡). 초과 시 추천 제외
    private Long incomeLimit;        // 연소득 상한(원)
    private String joinCondition;    // 가입 조건 요약

    private String exclusiveGroup;   // 동일 그룹 내 택1. NULL 이면 제한 없음(FREE)

    private Integer baseYear;        // 기준 연도
    private LocalDate applyStart;    // 접수 시작일. NULL 이면 상시
    private LocalDate applyEnd;      // 접수 종료일. NULL 이면 상시
    private String applyCycle;       // 접수 주기 안내 (만료 시 표시용)

    private Integer priority;        // 정렬 우선순위. 낮을수록 상단
    private String detail;           // 상세 설명
    private String externalUrl;      // 신청 페이지
    private String deeplink;         // KB스타뱅킹 딥링크 (KB 상품)

    private LocalDateTime createdAt; // 생성 시각
    private LocalDateTime updatedAt; // 수정 시각
}
