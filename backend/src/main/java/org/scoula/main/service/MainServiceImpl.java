package org.scoula.main.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.scoula.car.dto.CarGoalResponseDTO;
import org.scoula.car.dto.CarUsedPriceResponseDTO;
import org.scoula.car.service.CarService;
import org.scoula.common.exception.BusinessException;
import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.service.DashboardService;
import org.scoula.main.dto.MainFavoriteRoadmapDTO;
import org.scoula.main.dto.MainSummaryResponseDTO;
import org.scoula.main.dto.MainUpcomingScheduleDTO;
import org.scoula.main.mapper.MainMapper;
import org.scoula.product.dto.SavingProductListResponseDTO;
import org.scoula.product.service.ProductService;
import org.scoula.rent.dto.RentCostResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.service.RentService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    // 관심 로드맵 카테고리 코드 (job_category 등 다른 도메인 코드와 무관한 main 자체 분류)
    // 1=여행, 2=진로는 SQL 서브쿼리로 정확히 계산되어 보정이 필요 없으므로 상수를 두지 않는다.
    private static final int CATEGORY_CAR = 3;
    private static final int CATEGORY_RENT = 4;

    private final DashboardService dashboardService;
    private final ProductService productService;
    private final MainMapper mainMapper;
    private final CarService carService;
    private final RentService rentService;

    @Override
    public MainSummaryResponseDTO findSummary(Long userId) {
        // 전역 D-Day 및 복무 기본 정보 조회
        DashboardBasicResponseDTO basicInfo =
                this.dashboardService.findBasicInfo(userId);

        // 현재 적금 납입액 및 예상 만기 수령액 조회
        DashboardSavingsResponseDTO savings = null;

        try {
            savings = this.dashboardService.findSavingsStatus(userId);
        } catch (BusinessException e) {
            // 군적금 미가입은 메인 전체 조회 실패로 처리하지 않고 빈 상태로 반환
            if (!"DASH_002".equals(e.getCode())) {
                throw e;
            }
        }

        // 사용자가 관심 등록한 카테고리별 로드맵 조회
        List<MainFavoriteRoadmapDTO> favoriteRoadmaps =
                this.mainMapper.findFavoriteRoadmapList(userId);

        // 자동차·자취는 취득세율·관리비 계산이 각 도메인 서비스(Java)에만 있어
        // SQL로 재현할 수 없으므로, 해당 서비스 메서드를 호출해 SQL 기본값을 정확한 금액으로 덮어쓴다.
        // 진로·여행은 저장 시점에 확정된 금액을 SQL이 그대로 합산하므로 별도 보정이 필요 없다.
        for (MainFavoriteRoadmapDTO roadmap : favoriteRoadmaps) {
            this.enrichAmount(roadmap, userId);
        }

        // 여행 출발일과 진로 필기시험일 중 가장 가까운 일정 2개 조회
        List<MainUpcomingScheduleDTO> upcomingSchedules =
                this.mainMapper.findUpcomingScheduleList(userId);

        // 메인 배너에 노출할 KB 적금 상품 최대 3개 조회
        List<SavingProductListResponseDTO> recommendedProducts =
                this.productService.findSavingProductListByCategory("savings")
                        .stream()
                        .limit(3)
                        .toList();

        return new MainSummaryResponseDTO(
                basicInfo,
                savings,
                favoriteRoadmaps,
                upcomingSchedules,
                recommendedProducts
        );
    }

    /*
     * 카테고리별로 SQL 기본값을 정확한 금액으로 보정한다.
     * 상세 계산이 실패해도 홈 화면 전체가 죽으면 안 되므로 예외를 삼키고 SQL 기본값을 유지한다.
     */
    private void enrichAmount(MainFavoriteRoadmapDTO roadmap, Long userId) {
        try {
            if (roadmap.getCategoryId() == null) {
                return;
            }

            int categoryId = roadmap.getCategoryId();

            if (categoryId == CATEGORY_CAR) {
                this.enrichCarAmount(roadmap, userId);
            } else if (categoryId == CATEGORY_RENT) {
                this.enrichRentAmount(roadmap);
            }
        } catch (Exception e) {
            log.warn("관심 로드맵 금액 보정 실패: goalId={}, categoryId={}",
                    roadmap.getGoalId(), roadmap.getCategoryId(), e);
        }
    }

    private void enrichCarAmount(MainFavoriteRoadmapDTO roadmap, Long userId) {
        CarGoalResponseDTO goal = this.carService.findCarGoalDetail(roadmap.getGoalId(), userId);

        if (goal.getSelectedModelId() == null) {
            return; // 차량 미선택이면 SQL 기본값(base_price) 유지
        }

        if (Boolean.TRUE.equals(goal.getIsNew())) {
            // 신차 - SQL의 base_price가 이미 맞는 기준값이므로 취득세만 더한다.
            long acquisitionTax = this.carService
                    .calculateAcquisitionTax(roadmap.getGoalId(), userId)
                    .getAcquisitionTaxAmount();

            long baseAmount = roadmap.getAmount() == null ? 0L : roadmap.getAmount();
            // 취득세액은 만원 단위라 원으로 환산해 더한다 (baseAmount는 SQL 에서 base_price*10000 = 원)
            roadmap.setAmount(baseAmount + acquisitionTax * 10000);
        } else {
            // 중고차 - 연식·키로수로 재산정한 가격이라 SQL 값과 무관하게 통째로 교체한다.
            CarUsedPriceResponseDTO used = this.carService.calculateUsedPrice(roadmap.getGoalId(), userId);
            // totalPrice(시세+취득세)는 만원 단위 → 원으로 환산
            roadmap.setAmount(used.getTotalPrice() * 10000);
        }
    }

    private void enrichRentAmount(MainFavoriteRoadmapDTO roadmap) {
        RentGoalDetailResponseDTO goal = this.rentService.findGoal(roadmap.getGoalId());

        if (goal.getListing() == null) {
            return; // 확정 매물 없으면(DRAFT) 계산 불가, SQL 기본값(null) 유지
        }

        RentCostResponseDTO cost = this.rentService.calculateCost(
                goal.getListing().getListingId(),
                goal.getResidenceMonths()
        );

        roadmap.setAmount(cost.getTotalRequired());
    }
}
