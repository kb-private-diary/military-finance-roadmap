package org.scoula.main.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.product.dto.SavingProductListResponseDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainSummaryResponseDTO {

    // 전역 D-Day 및 복무 기본 정보
    private DashboardBasicResponseDTO basicInfo;

    // 현재 적금 납입액 및 예상 만기 수령액
    private DashboardSavingsResponseDTO savings;

    // 카테고리별 관심 등록 목표
    // 각 목표의 amount는 프론트에서 사용 예정 금액 합산에 사용
    private List<MainFavoriteRoadmapDTO> favoriteRoadmaps;

    // 여행 출발일과 진로 자격증 필기시험일 중 가장 가까운 일정 2개
    private List<MainUpcomingScheduleDTO> upcomingSchedules;

    // 메인 배너에 노출할 KB 적금 상품 목록
    private List<SavingProductListResponseDTO> recommendedProducts;
}
