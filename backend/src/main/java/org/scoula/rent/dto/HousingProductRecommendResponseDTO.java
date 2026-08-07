package org.scoula.rent.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * 매물 조건 기반 주거 금융상품 추천 응답 (그룹핑 결과, 명세 2장)
 *
 * 중복수급 제한을 프론트가 UI 로 구분할 수 있도록 exclusive_group 별로 나눠 내려준다
 * - monthlySubsidy(MONTHLY_SUBSIDY): 월세 지원금 계열 - 그룹 내 택1 (라디오 버튼)
 * - depositLoan(DEPOSIT_LOAN): 보증금 대출·이자지원 계열 - 그룹 내 택1 (라디오 버튼)
 * - free(NULL): 중개보수·이사비·KB 상품 - 제한 없음, 자유 조합 (체크박스)
 *
 * 각 그룹 내부는 priority 오름차순 + 만료 상품(open=false) 하단 정렬
 */
@Data
@Builder
public class HousingProductRecommendResponseDTO {
    private Long listingId;      // 대상 매물 번호

    // 병역 안내 (명세 5장) - user 에 생년월일이 없어 나이 필터엔 못 쓰지만 복무 인정 메시지 노출용
    private boolean veteran;      // 복무기간 정보 보유 여부 (입대일·전역예정일 존재)
    private Integer serviceMonths; // 복무기간(개월). 정보 없으면 null
    private Integer serviceYears;  // 복무기간(년, 내림). 정보 없으면 null

    private List<HousingProductResponseDTO> monthlySubsidy; // 월세 지원금 (택1)
    private List<HousingProductResponseDTO> depositLoan;    // 보증금 대출·이자지원 (택1)
    private List<HousingProductResponseDTO> free;           // 추가 지원 (자유 조합)
}
