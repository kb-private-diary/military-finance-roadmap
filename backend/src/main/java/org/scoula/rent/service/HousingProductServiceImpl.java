package org.scoula.rent.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.scoula.common.exception.BusinessException;
import org.scoula.rent.domain.HousingProductVO;
import org.scoula.rent.domain.MilitaryServiceVO;
import org.scoula.rent.domain.RentListingVO;
import org.scoula.rent.dto.HousingProductRecommendResponseDTO;
import org.scoula.rent.dto.HousingProductResponseDTO;
import org.scoula.rent.mapper.HousingProductMapper;
import org.scoula.rent.mapper.RentListingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HousingProductServiceImpl implements HousingProductService {

    // 중복수급 그룹 (명세 2장)
    private static final String GROUP_MONTHLY_SUBSIDY = "MONTHLY_SUBSIDY";
    private static final String GROUP_DEPOSIT_LOAN = "DEPOSIT_LOAN";

    // 특수 예외 식별 (명세 3-4, 2-2) - product_id 는 시딩 순서에 의존하므로 상품명/기관으로 식별
    private static final String NAME_MEOMULJARI = "머물자리론";        // 부산 머물자리론
    private static final String PROVIDER_DAEJEON = "대전광역시";        // 대전시 청년 월세지원
    private static final int MEOMULJARI_MIN_MONTHS = 12;               // 임대차 12개월 미만이면 신청 불가

    // 대전 이중배타 안내 (월세지원이 MONTHLY_SUBSIDY 뿐 아니라 DEPOSIT_LOAN 이자지원과도 배타 - 명세 2-2)
    private static final String DAEJEON_CROSS_NOTE =
            "국토부 청년월세 지원뿐 아니라 청년 주택임차보증금 이자지원 사업과도 중복 지급이 불가합니다 (두 그룹 동시 배타)";

    private final HousingProductMapper mapper;
    private final RentListingMapper listingMapper;

    @Override
    @Transactional(readOnly = true)
    public HousingProductRecommendResponseDTO recommend(Long listingId, Long userId, int months) {
        // 1) 매물 조회 (Step3 에서 고른 매물). 없으면 404
        RentListingVO listing = this.listingMapper.findById(listingId);
        if (listing == null) {
            throw BusinessException.notFound("매물을 찾을 수 없습니다.", "RENT_006");
        }
        // 2) 거주개월 검증 (1개월 이상)
        if (months <= 0) {
            throw BusinessException.badRequest("거주 개월수가 올바르지 않습니다.", "RENT_007");
        }

        // 3) 매물 조건으로 후보 상품 조회 (명세 3장 필터: 지역·보증금·월세·면적. 나이 필터 없음)
        //    deposit/monthly 는 NULL 이면 0 으로 (한도 판정에서 항상 통과), area 는 NULL 그대로 전달(매퍼에서 통과 처리)
        Long deposit = listing.getDeposit() == null ? 0L : listing.getDeposit();
        Long monthlyRent = listing.getMonthlyRent() == null ? 0L : listing.getMonthlyRent();
        List<HousingProductVO> candidates = this.mapper.findProducts(
                listing.getRegionCode(), deposit, monthlyRent, listing.getAreaSqm());

        // 4) 사용자 복무기간 계산 (병역 안내용, 명세 5장). 생년월일이 없어 나이 필터엔 쓰지 않는다
        MilitaryServiceVO service = this.mapper.findServicePeriodByUserId(userId);
        Integer serviceMonths = calcServiceMonths(service);
        boolean veteran = serviceMonths != null;
        Integer serviceYears = serviceMonths == null ? null : serviceMonths / 12;

        LocalDate today = LocalDate.now();

        // 5) 특수 예외 제외(명세 3-4) → DTO 변환(공고기간·이중배타 반영) → 정렬(priority, 만료 하단)
        List<HousingProductResponseDTO> products = candidates.stream()
                .filter(vo -> !isExcludedMeomuljari(vo, months))
                .map(vo -> HousingProductResponseDTO.of(vo, isOpen(vo, today), crossExclusiveNote(vo)))
                .sorted(sortComparator())
                .toList();

        // 6) 중복수급 그룹별로 나눠 응답 (명세 2장). 정렬은 위에서 끝났으므로 그룹 내 순서 유지됨
        return HousingProductRecommendResponseDTO.builder()
                .listingId(listingId)
                .veteran(veteran)
                .serviceMonths(serviceMonths)
                .serviceYears(serviceYears)
                .monthlySubsidy(filterByGroup(products, GROUP_MONTHLY_SUBSIDY))
                .depositLoan(filterByGroup(products, GROUP_DEPOSIT_LOAN))
                .free(filterFree(products))
                .build();
    }

    /**
     * 복무기간(개월) 계산 - 입대일~전역예정일. 둘 중 하나라도 없으면 null
     * discharge_date 는 전역예정일이므로 전역 전이라도 계획된 총 복무기간으로 본다
     */
    private Integer calcServiceMonths(MilitaryServiceVO service) {
        if (service == null || service.getEnlistDate() == null || service.getDischargeDate() == null) {
            return null;
        }
        long m = ChronoUnit.MONTHS.between(service.getEnlistDate(), service.getDischargeDate());
        return m < 0 ? 0 : (int) m;
    }

    /**
     * 부산 머물자리론 특수 제외 (명세 3-4)
     * 임대차 계약기간(months)이 12개월 미만이면 신청 불가 - 상품명으로 식별
     */
    private boolean isExcludedMeomuljari(HousingProductVO vo, int months) {
        boolean isMeomuljari = vo.getProductName() != null && vo.getProductName().contains(NAME_MEOMULJARI);
        return isMeomuljari && months < MEOMULJARI_MIN_MONTHS;
    }

    /**
     * 대전시 월세지원 이중배타 안내 (명세 2-2)
     * 현재 스키마(exclusive_group 단일)로는 표현 불가해 별도 안내 문구로 노출. 해당 없으면 null
     */
    private String crossExclusiveNote(HousingProductVO vo) {
        boolean isDaejeonSubsidy = PROVIDER_DAEJEON.equals(vo.getProvider())
                && GROUP_MONTHLY_SUBSIDY.equals(vo.getExclusiveGroup());
        return isDaejeonSubsidy ? DAEJEON_CROSS_NOTE : null;
    }

    /**
     * 공고 접수중 여부 (명세 4장)
     * 시작·종료일이 모두 없으면 상시(true), 있으면 오늘이 기간 내(경계 포함)일 때만 true
     */
    private boolean isOpen(HousingProductVO vo, LocalDate today) {
        LocalDate start = vo.getApplyStart();
        LocalDate end = vo.getApplyEnd();
        if (start == null && end == null) {
            return true; // 상시 접수
        }
        if (start == null || end == null) {
            return true; // 한쪽만 지정된 비정형 데이터는 접수중으로 관대하게 처리
        }
        return !today.isBefore(start) && !today.isAfter(end);
    }

    /** 정렬: 접수중 먼저, 그 안에서 priority 오름차순 (만료 상품은 하단 - 명세 4·6장) */
    private Comparator<HousingProductResponseDTO> sortComparator() {
        return Comparator
                .comparing(HousingProductResponseDTO::isOpen).reversed()
                .thenComparing(dto -> dto.getPriority() == null ? Integer.MAX_VALUE : dto.getPriority());
    }

    /** 특정 중복수급 그룹의 상품만 (정렬 순서 유지) */
    private List<HousingProductResponseDTO> filterByGroup(List<HousingProductResponseDTO> products, String group) {
        return products.stream()
                .filter(dto -> group.equals(dto.getExclusiveGroup()))
                .toList();
    }

    /** 제한 없는(FREE) 상품만 - exclusive_group 이 null (중개보수·이사비·KB 상품) */
    private List<HousingProductResponseDTO> filterFree(List<HousingProductResponseDTO> products) {
        return products.stream()
                .filter(dto -> dto.getExclusiveGroup() == null)
                .toList();
    }
}
