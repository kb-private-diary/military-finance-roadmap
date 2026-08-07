package org.scoula.main.service;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.service.DashboardService;
import org.scoula.main.dto.MainFavoriteRoadmapDTO;
import org.scoula.main.dto.MainSummaryResponseDTO;
import org.scoula.main.dto.MainUpcomingScheduleDTO;
import org.scoula.main.mapper.MainMapper;
import org.scoula.product.dto.SavingProductListResponseDTO;
import org.scoula.product.service.ProductService;
import org.scoula.regret.dto.RegretStatsResponseDTO;
import org.scoula.regret.service.RegretService;
//import org.scoula.social.dto.SocialStatsResponseDTO;
//import org.scoula.social.service.SocialService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainServiceImpl implements MainService {

    private final DashboardService dashboardService;
    private final ProductService productService;
    private final RegretService regretService;
    // 소셜 도메인 머지 후 다시 주입
    //private final SocialService socialService;
    private final MainMapper mainMapper;

    @Override
    public MainSummaryResponseDTO findSummary(Long userId) {
        // 전역 D-Day 및 복무 기본 정보 조회
        DashboardBasicResponseDTO basicInfo =
                this.dashboardService.findBasicInfo(userId);

        // 현재 적금 납입액 및 예상 만기 수령액 조회
        DashboardSavingsResponseDTO savings =
                this.dashboardService.findSavingsStatus(userId);

        // 사용자가 관심 등록한 카테고리별 로드맵 조회
        List<MainFavoriteRoadmapDTO> favoriteRoadmaps =
                this.mainMapper.findFavoriteRoadmapList(userId);

        // 여행 출발일과 진로 필기시험일 중 가장 가까운 일정 2개 조회
        List<MainUpcomingScheduleDTO> upcomingSchedules =
                this.mainMapper.findUpcomingScheduleList(userId);

        // 현재 연월 기준 후회소비 통계 조회
        String yearMonth = LocalDate.now().toString().substring(0, 7);
        RegretStatsResponseDTO regret =
                this.regretService.getMonthlyStats(userId, yearMonth);

        // 소셜 도메인 머지 후 전체 사용자 기준 통계 조회
        // SocialStatsResponseDTO social =
        //         this.socialService.findStats(userId, "ALL");

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
                regret,
                recommendedProducts
        );
    }
}