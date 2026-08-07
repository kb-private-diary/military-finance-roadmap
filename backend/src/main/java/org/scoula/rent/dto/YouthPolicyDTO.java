package org.scoula.rent.dto;

import lombok.Data;

/**
 * 온통청년 OPEN API(getPlcy) 응답 result.youthPolicyList 한 건의 원본 필드 DTO.
 * (연동명세 §2-1 표) — 값은 모두 문자열로 받아 SyncService 에서 매핑·파싱한다.
 * API 응답 필드가 늘어나도 여기만 추가하면 되며, 파싱 규칙은 SyncService 에 둔다.
 */
@Data
public class YouthPolicyDTO {
    private String plcyNo;             // 정책 고유번호 (PK)
    private String plcyNm;             // 정책명 → product_name
    private String plcyExplnCn;        // 정책 설명 → detail 보조
    private String plcySprtCn;         // 지원 내용(금액·한도) → detail
    private String lclsfNm;            // 대분류 (주거)
    private String mclsfNm;            // 중분류 (전월세 및 주거급여 지원 등) - 앱 필터
    private String plcyKywdNm;         // 키워드(콤마 구분) - 보조 필터
    private String pvsnInstGroupCd;    // 0054001 중앙부처 / 0054002 지자체 → product_type
    private String plcyPvsnMthdCd;     // 지원 방식 코드 → support_type
    private String sprvsnInstCdNm;     // 주관기관명 → provider
    private String rgtrHghrkInstCdNm;  // 등록 상위기관(시도명) - 지역 보조
    private String sprtTrgtMinAge;     // 최소 연령 → min_age
    private String sprtTrgtMaxAge;     // 최대 연령 → max_age
    private String sprtTrgtAgeLmtYn;   // 연령 제한 여부 Y/N
    private String earnCndSeCd;        // 0043001 무관 / 0043002 연소득 / 0043003 기타
    private String earnMaxAmt;         // 소득 상한(만원 단위)
    private String earnEtcCn;          // 소득 조건 설명
    private String addAplyQlfcCndCn;   // 추가 자격 요건(보증금·면적) - 파싱 대상
    private String ptcpPrpTrgtCn;      // 참여 제한 대상(중복 수급) - 파싱 대상
    private String aplyPrdSeCd;        // 0057001 특정기간 / 0057002 상시 / 0057003 마감
    private String aplyYmd;            // "20260701 ~ 20261231"
    private String bizPrdBgngYmd;      // 사업기간 시작 (aplyYmd 대체)
    private String bizPrdEndYmd;       // 사업기간 종료
    private String bizPrdEtcCn;        // 기간 설명 → apply_cycle
    private String plcyAplyMthdCn;     // 신청 방법(군복무 규정 포함) - 파싱 대상
    private String aplyUrlAddr;        // 신청 페이지 → external_url
    private String refUrlAddr1;        // 참고 URL → external_url 대체
    private String zipCd;              // 시군구 코드 콤마 구분 → zip_cd
    private String sprtSclCnt;         // 지원 규모(명)
    private String lastMdfcnDt;        // 최종 수정일
}
