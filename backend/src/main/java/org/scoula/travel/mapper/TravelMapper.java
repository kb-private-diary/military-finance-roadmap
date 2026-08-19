package org.scoula.travel.mapper;

import java.util.List;
import java.time.LocalDate;

import org.apache.ibatis.annotations.Param;

import org.scoula.product.domain.SavingProductVO;
import org.scoula.travel.domain.CityCostVO;
import org.scoula.travel.domain.TravelCostVO;
import org.scoula.travel.domain.TravelGoalVO;
import org.scoula.travel.domain.TravelInsuranceVO;
import org.scoula.travel.domain.TravelPackageVO;
import org.scoula.travel.dto.TravelQuarterCostSearchDTO;
import org.scoula.travel.dto.TravelPackageSearchDTO;
import org.scoula.travel.dto.TravelUserFinanceDTO;
import org.scoula.travel.dto.TravelNotificationTargetDTO;

public interface TravelMapper {

    List<CityCostVO> findCityCostList(@Param("country") String country);

    // 도시명으로 물가 조회. 도착지 검증과 경비 산출에 사용.
    CityCostVO findCityCostByCity(@Param("city") String city);

    String findAirportCodeByCity(@Param("city") final String city);

    // 여행 목표 등록
    int insertGoal(TravelGoalVO vo);

    // 회원의 플랜 작성 상태를 확인한다. 플랜 중복 생성 방지용.
    int countGoalByStatus(@Param("userId") Long userId,
                          @Param("status") String status);

    // 여행 목표 단건 조회. 경비 산출의 입력값을 읽는다.
    TravelGoalVO findGoal(@Param("goalId") Long goalId);

    TravelGoalVO findDraftGoalByUserId(
            @Param("userId") final Long userId);

    int updateGoal(final TravelGoalVO vo);

    int updateGoalStyle(@Param("goalId") final Long goalId,
                        @Param("userId") final Long userId,
                        @Param("style") final String style,
                        @Param("modifiedNm") final String modifiedNm);

    int updateGoalPlaces(@Param("goalId") final Long goalId,
                         @Param("places") final String places,
                         @Param("modifiedNm") final String modifiedNm);

    int insertCost(final TravelCostVO vo);

    int updateCost(final TravelCostVO vo);

    TravelCostVO findCostByGoalId(@Param("goalId") final Long goalId);

    TravelUserFinanceDTO findUserFinanceByUserId(
            @Param("userId") final Long userId);

    Long findFlightCostByQuarter(
            final TravelQuarterCostSearchDTO request);

    Long findHotelCostByQuarter(
            final TravelQuarterCostSearchDTO request);

    TravelPackageVO findPackageByGoodsCode(
            @Param("goodsCode") final String goodsCode);

    TravelPackageVO getPackageById(
            @Param("packageId") final Long packageId);

    List<TravelPackageVO> findPackagesByDestination(
            final TravelPackageSearchDTO request);

    int insertPackage(final TravelPackageVO travelPackage);

    int updatePackage(final TravelPackageVO travelPackage);

    int updateGoalPackage(@Param("goalId") final Long goalId,
                          @Param("packageId") final Long packageId,
                          @Param("modifiedNm") final String modifiedNm);

    List<TravelInsuranceVO> findTravelInsuranceList();

    List<SavingProductVO> findTravelSavingProductList(
            @Param("codePrefix") final String codePrefix);

    int confirmGoal(final TravelGoalVO goal);

    List<TravelNotificationTargetDTO> findUpcomingConfirmedGoalList(
            @Param("fromDate") final LocalDate fromDate,
            @Param("toDate") final LocalDate toDate);

    int countSuccessfulTravelNotification(
            @Param("userId") final Long userId,
            @Param("title") final String title,
            @Param("body") final String body,
            @Param("category") final String category,
            @Param("sentDate") final LocalDate sentDate);

}
