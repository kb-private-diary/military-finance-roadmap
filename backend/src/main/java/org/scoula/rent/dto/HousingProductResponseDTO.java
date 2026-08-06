package org.scoula.rent.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.HousingProductVO;

/**
 * 주거 금융상품 요약 응답 DTO (매물 조건 기반 추천 한 건)
 * isOpen(공고 접수중 여부)·crossExclusiveNote(대전 이중배타 안내)는 서비스단에서 계산해 주입한다
 */
@Data
@Builder
public class HousingProductResponseDTO {
    private Long productId;          // 상품 PK
    private String productName;      // 상품/사업명
    private String productType;      // POLICY / LOCAL / BANK
    private String supportType;      // GRANT / LOAN / INTEREST / COST
    private String loanType;         // DEPOSIT / MONTHLY / BOTH (GRANT 는 null)
    private String provider;         // 공급 기관
    private boolean kb;              // KB 상품 여부 (is_kb = 'Y')
    private String regionCode;       // 시도코드 2자리 (전국이면 null)

    private BigDecimal rateMin;      // 최저 금리 %
    private BigDecimal rateMax;      // 최고 금리 %
    private String rateSummary;      // 금리 표시용 문자열
    private Integer supportAmount;   // 지원금 월 최대액(원)
    private Integer supportMonths;   // 지원 개월수
    private Long loanLimit;          // 대출 한도(원)

    private Integer minAge;          // 최소 나이
    private Integer maxAge;          // 최대 나이 (병역 연장 전 기준값)
    private Integer veteranExtend;   // 병역 이행 시 연령 상한 연장(년)
    private String veteranNote;      // 병역 혜택 안내 문구 (표시용)

    private String exclusiveGroup;   // 중복수급 그룹 (MONTHLY_SUBSIDY/DEPOSIT_LOAN/null)
    private LocalDate applyStart;    // 접수 시작일 (null 이면 상시)
    private LocalDate applyEnd;      // 접수 종료일 (null 이면 상시)
    private String applyCycle;       // 접수 주기 안내 (만료 시 표시)
    private boolean open;            // 접수중 여부 (명세 4장). false 면 마감 - 회색·정렬 하단

    private Integer priority;        // 정렬 우선순위 (낮을수록 상단)
    private String detail;           // 상세 설명
    private String externalUrl;      // 신청 페이지
    private String deeplink;         // KB스타뱅킹 딥링크 (KB 상품)

    // 대전시 월세지원 등 현재 스키마로 표현 불가한 이중배타 안내 (명세 2-2). 해당 없으면 null
    private String crossExclusiveNote;

    /**
     * VO → DTO 변환
     * @param vo                금융상품 행
     * @param open              접수중 여부 (서비스단에서 오늘 날짜로 판정)
     * @param crossExclusiveNote 이중배타 안내 문구 (없으면 null)
     */
    public static HousingProductResponseDTO of(HousingProductVO vo, boolean open, String crossExclusiveNote) {
        return HousingProductResponseDTO.builder()
                .productId(vo.getProductId())
                .productName(vo.getProductName())
                .productType(vo.getProductType())
                .supportType(vo.getSupportType())
                .loanType(vo.getLoanType())
                .provider(vo.getProvider())
                .kb("Y".equals(vo.getIsKb()))
                .regionCode(vo.getRegionCode())
                .rateMin(vo.getRateMin())
                .rateMax(vo.getRateMax())
                .rateSummary(vo.getRateSummary())
                .supportAmount(vo.getSupportAmount())
                .supportMonths(vo.getSupportMonths())
                .loanLimit(vo.getLoanLimit())
                .minAge(vo.getMinAge())
                .maxAge(vo.getMaxAge())
                .veteranExtend(vo.getVeteranExtend())
                .veteranNote(vo.getVeteranNote())
                .exclusiveGroup(vo.getExclusiveGroup())
                .applyStart(vo.getApplyStart())
                .applyEnd(vo.getApplyEnd())
                .applyCycle(vo.getApplyCycle())
                .open(open)
                .priority(vo.getPriority())
                .detail(vo.getDetail())
                .externalUrl(vo.getExternalUrl())
                .deeplink(vo.getDeeplink())
                .crossExclusiveNote(crossExclusiveNote)
                .build();
    }
}
