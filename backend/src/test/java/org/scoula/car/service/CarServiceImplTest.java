package org.scoula.car.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.car.dto.CarAcquisitionTaxResponseDTO;
import org.scoula.car.dto.CarEvSubsidyResponseDTO;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.car.dto.CarGoalResponseDTO;
import org.scoula.car.dto.CarMaintenanceCostResponseDTO;
import org.scoula.car.dto.CarModelSelectRequestDTO;
import org.scoula.car.dto.CarRecommendationResponseDTO;
import org.scoula.car.dto.CarUsedPriceResponseDTO;
import org.scoula.common.exception.BusinessException;
import org.scoula.config.RootConfig;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@ActiveProfiles("dev")
@Transactional
class CarServiceImplTest {

    // 시드데이터: goal_id=1,2는 user_id=1 소유, goal_id=3은 user_id=2 소유
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Autowired
    private CarService service;

    private CarGoalCreateRequestDTO requestDto(Long budget) {
        return CarGoalCreateRequestDTO.builder()
                .budget(budget)
                .isNew(false)
                .targetDate(LocalDate.of(2027, 1, 1))
                .region("서울")
                .build();
    }

    @Test
    void createCarGoal_withValidBudget_returnsGoalId() {
        CarGoalCreateResponseDTO result = this.service.createCarGoal(USER_ID, requestDto(15_000_000L));

        assertNotNull(result.getGoalId());
    }

    @Test
    void createCarGoal_withNullBudget_throws() {
        assertThrows(BusinessException.class, () -> this.service.createCarGoal(USER_ID, requestDto(null)));
    }

    @Test
    void createCarGoal_withZeroBudget_throws() {
        assertThrows(BusinessException.class, () -> this.service.createCarGoal(USER_ID, requestDto(0L)));
    }

    @Test
    void createCarGoal_withNegativeBudget_throws() {
        assertThrows(BusinessException.class, () -> this.service.createCarGoal(USER_ID, requestDto(-1_000_000L)));
    }

    @Test
    void createCarGoal_withExperienceYears_persistsToDetail() {
        CarGoalCreateRequestDTO createDto = CarGoalCreateRequestDTO.builder()
                .budget(15_000_000L)
                .isNew(false)
                .experienceYears(4)
                .targetDate(LocalDate.of(2027, 1, 1))
                .region("서울")
                .build();

        CarGoalCreateResponseDTO created = this.service.createCarGoal(USER_ID, createDto);
        CarGoalResponseDTO detail = this.service.findCarGoalDetail(created.getGoalId(), USER_ID);

        assertEquals(4, detail.getExperienceYears());
    }

    @Test
    void calculateMaintenanceCost_endToEndWithExperienceYearsFromCreation_succeeds() {
        // EV 모델을 선택하면 오피넷 실시간 유가 호출 없이 고정 전기요금으로 계산되어 결정적으로 테스트 가능
        CarGoalCreateRequestDTO createDto = CarGoalCreateRequestDTO.builder()
                .budget(30_000_000L)
                .isNew(true)
                .experienceYears(4) // "3~5년" 구간
                .targetDate(LocalDate.of(2027, 1, 1))
                .region("서울")
                .build();
        CarGoalCreateResponseDTO created = this.service.createCarGoal(USER_ID, createDto);

        // 목표 단계엔 차종이 없고, 선택한 모델(캐스퍼 일렉트릭, car_type_code=1)이 목표의 차종을 결정한다
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder()
                .modelId(16L) // 캐스퍼 일렉트릭(car_type_code=1)
                .build();
        this.service.selectCarModel(created.getGoalId(), USER_ID, selectDto);

        CarMaintenanceCostResponseDTO result =
                this.service.calculateMaintenanceCost(created.getGoalId(), USER_ID);

        assertEquals("전기", result.getFuelType());
        assertEquals(55L, result.getEstimatedFuelCostAnnual());
        assertEquals(40L, result.getInsurancePremiumMin());
        assertEquals(50L, result.getInsurancePremiumMax());
    }

    @Test
    void findCarGoals_withSeededUser_returnsNonEmptyList() {
        List<CarGoalResponseDTO> result = this.service.findCarGoals(USER_ID);

        assertFalse(result.isEmpty());
    }

    @Test
    void findCarGoals_withUnknownUser_returnsEmptyList() {
        List<CarGoalResponseDTO> result = this.service.findCarGoals(9_999_999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findCarGoalDetail_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.findCarGoalDetail(9_999_999L, USER_ID));
    }

    @Test
    void findCarGoalDetail_withSeededGoal_includesSelectedModelName() {
        // goal_id=1 시드데이터: selected_model_id=3(캐스퍼)
        CarGoalResponseDTO result = this.service.findCarGoalDetail(1L, USER_ID);

        assertEquals(2000L, result.getBudget());
        assertEquals("캐스퍼", result.getSelectedModelName());
    }

    @Test
    void findCarGoalDetail_withWrongOwner_throws() {
        // goal_id=1은 user_id=1 소유. 다른 사용자가 조회를 시도하면 존재 여부를 숨기고 404로 응답해야 한다.
        assertThrows(BusinessException.class, () -> this.service.findCarGoalDetail(1L, OTHER_USER_ID));
    }

    @Test
    void recommendCars_withSeededUsedGoal_sortsByTotalPriceAscending() {
        // goal_id=2 시드데이터: is_new=false, budget=1500만원
        // 목표 단계엔 차종이 없으므로 전체 차량 모델(경차6/준중형4/SUV9=19종)이 후보이지만,
        // 예산+허용오차(100만원)를 넘는 아이오닉 6은 제외되어 18종이 남는다
        // 연식은 고정 3년이 아니라, 예산 안에서 가장 최신(연차가 가장 적은) 연식을 후보별로 역산한다
        List<CarRecommendationResponseDTO> result = this.service.recommendCars(2L, USER_ID);
        int currentYear = LocalDate.now().getYear();

        assertEquals(18, result.size());
        CarRecommendationResponseDTO cheapest = result.get(0);
        assertEquals("스파크", cheapest.getModelName());
        // 스파크(992만원)는 신차가만으로도 예산(1500) 이내라 감가 없이(0년) 그대로 추천된다
        assertEquals(currentYear, cheapest.getAssumedYear());
        assertEquals(992L, cheapest.getEstimatedPrice());
        assertTrue(cheapest.getWithinBudget());

        // 신차가가 예산을 초과하는 비싼 차량은 연식을 더 낮춰(더 오래된 것으로) 예산에 맞춘다
        CarRecommendationResponseDTO rayEv = result.stream()
                .filter(item -> "레이 EV".equals(item.getModelName()))
                .findFirst()
                .orElseThrow();
        assertTrue(rayEv.getAssumedYear() < currentYear);
        assertTrue(rayEv.getTotalPrice() <= 1_500L);
        assertTrue(rayEv.getWithinBudget());

        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).getTotalPrice() <= result.get(i).getTotalPrice());
        }
    }

    @Test
    void recommendCars_excludesModelsFarOverBudget_butKeepsMarginalOverBudgetOnes() {
        // isNew=true로 신차 기준(연식 역산 없이 기준가 그대로)이라 예상 총액을 손으로 검증하기 쉬움
        CarGoalCreateRequestDTO createDto = CarGoalCreateRequestDTO.builder()
                .budget(1_450L)
                .isNew(true)
                .targetDate(LocalDate.of(2027, 1, 1))
                .region("서울")
                .build();
        CarGoalCreateResponseDTO created = this.service.createCarGoal(USER_ID, createDto);

        List<CarRecommendationResponseDTO> result =
                this.service.recommendCars(created.getGoalId(), USER_ID);

        // 레이(총액 1,456만원)는 예산을 6만원 넘지만 허용 오차(100만원) 이내라 목록에 남고 예산초과로 표시된다
        CarRecommendationResponseDTO ray = result.stream()
                .filter(item -> "레이".equals(item.getModelName()))
                .findFirst()
                .orElseThrow();
        assertEquals(1_456L, ray.getTotalPrice());
        assertFalse(ray.getWithinBudget());

        // 아반떼(총액 2,101만원)는 예산+허용오차(1,550만원)를 크게 넘어서 목록에서 아예 제외된다
        boolean hasAvante = result.stream().anyMatch(item -> "아반떼".equals(item.getModelName()));
        assertFalse(hasAvante);

        // 아이오닉 6(4,995만원)은 훨씬 더 심하게 초과하므로 당연히 제외된다
        boolean hasIoniq6 = result.stream().anyMatch(item -> "아이오닉 6".equals(item.getModelName()));
        assertFalse(hasIoniq6);
    }

    @Test
    void recommendCars_withNewCarGoal_omitsAssumedYear() {
        // goal_id=3 시드데이터: is_new=true, user_id=2
        List<CarRecommendationResponseDTO> result = this.service.recommendCars(3L, OTHER_USER_ID);

        CarRecommendationResponseDTO spark = result.stream()
                .filter(item -> "스파크".equals(item.getModelName()))
                .findFirst()
                .orElseThrow();

        assertEquals(992L, spark.getEstimatedPrice()); // 신차는 기준가 그대로
        assertNull(spark.getAssumedYear());
    }

    @Test
    void recommendCars_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.recommendCars(9_999_999L, USER_ID));
    }

    @Test
    void recommendCars_withWrongOwner_throws() {
        // goal_id=1은 user_id=1 소유
        assertThrows(BusinessException.class, () -> this.service.recommendCars(1L, OTHER_USER_ID));
    }

    @Test
    void selectCarModel_setsGoalCarTypeFromSelectedModel() {
        // goal_id=2 시드데이터: 선택차량 없음(draft), 차종 미정 상태에서 시작
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder()
                .modelId(1L) // 모닝(car_type_code=1)
                .selectedYear(2022)
                .build();

        CarGoalResponseDTO result = this.service.selectCarModel(2L, USER_ID, selectDto);

        assertEquals(1L, result.getSelectedModelId());
        assertEquals("모닝", result.getSelectedModelName());
        assertEquals(1, result.getCarTypeCode());
        assertEquals("SELECTED", result.getStatus());
    }

    @Test
    void selectCarModel_withWrongOwner_throws() {
        // goal_id=2는 user_id=1 소유. 다른 사용자가 남의 목표에 차량을 선택시킬 수 없어야 한다.
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder().modelId(1L).build();

        assertThrows(BusinessException.class, () -> this.service.selectCarModel(2L, OTHER_USER_ID, selectDto));
    }

    @Test
    void selectCarModel_withNullModelId_throws() {
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder().build();

        assertThrows(BusinessException.class, () -> this.service.selectCarModel(2L, USER_ID, selectDto));
    }

    @Test
    void selectCarModel_withUnknownModelId_throws() {
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder().modelId(9_999_999L).build();

        assertThrows(BusinessException.class, () -> this.service.selectCarModel(2L, USER_ID, selectDto));
    }

    @Test
    void selectCarModel_withUnknownGoalId_throws() {
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder().modelId(1L).build();

        assertThrows(BusinessException.class, () -> this.service.selectCarModel(9_999_999L, USER_ID, selectDto));
    }

    @Test
    void calculateMaintenanceCost_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class,
                () -> this.service.calculateMaintenanceCost(9_999_999L, USER_ID));
    }

    @Test
    void calculateMaintenanceCost_withoutSelectedModel_throws() {
        CarGoalCreateResponseDTO created = this.service.createCarGoal(USER_ID, requestDto(15_000_000L));

        assertThrows(BusinessException.class,
                () -> this.service.calculateMaintenanceCost(created.getGoalId(), USER_ID));
    }

    @Test
    void calculateAcquisitionTax_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class,
                () -> this.service.calculateAcquisitionTax(9_999_999L, USER_ID));
    }

    @Test
    void calculateAcquisitionTax_withoutSelectedModel_throws() {
        CarGoalCreateResponseDTO created = this.service.createCarGoal(USER_ID, requestDto(15_000_000L));

        assertThrows(BusinessException.class,
                () -> this.service.calculateAcquisitionTax(created.getGoalId(), USER_ID));
    }

    @Test
    void calculateAcquisitionTax_withSeededGoal_returnsExpectedAmount() {
        // goal_id=1 시드데이터 기준
        CarAcquisitionTaxResponseDTO result = this.service.calculateAcquisitionTax(1L, USER_ID);

        assertEquals(1460L, result.getVehiclePrice());
        assertEquals(58L, result.getAcquisitionTaxAmount());
        assertTrue(result.getBondExempt());
    }

    @Test
    void calculateUsedPrice_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.calculateUsedPrice(9_999_999L, USER_ID));
    }

    @Test
    void calculateUsedPrice_withoutSelectedModel_throws() {
        CarGoalCreateResponseDTO created = this.service.createCarGoal(USER_ID, requestDto(15_000_000L));

        assertThrows(BusinessException.class,
                () -> this.service.calculateUsedPrice(created.getGoalId(), USER_ID));
    }

    @Test
    void calculateUsedPrice_withoutSelectedYear_assumesDefaultAge() {
        // goal_id=3 시드데이터: user_id=2, selected_model_id=4(아반떼, 1964만원), selected_year=NULL -> 3년 가정
        CarUsedPriceResponseDTO result = this.service.calculateUsedPrice(3L, OTHER_USER_ID);

        assertEquals(3, result.getAgeYears());
        assertEquals(1006L, result.getEstimatedUsedPrice());
    }

    @Test
    void calculateUsedPrice_withSeededGoal_returnsExpectedAmount() {
        // goal_id=1 시드데이터: selected_model_id=3(캐스퍼, base_price=1460만원), selected_year=2023
        CarUsedPriceResponseDTO result = this.service.calculateUsedPrice(1L, USER_ID);

        assertEquals(1460L, result.getBaseNewPrice());
        assertEquals(2023, result.getSelectedYear());
        assertEquals(748L, result.getEstimatedUsedPrice());
        assertEquals(30L, result.getAcquisitionTaxAmount());
        assertEquals(778L, result.getTotalPrice());
    }

    @Test
    void calculateEvSubsidy_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.calculateEvSubsidy(9_999_999L, USER_ID));
    }

    @Test
    void calculateEvSubsidy_withoutSelectedModel_throws() {
        CarGoalCreateResponseDTO created = this.service.createCarGoal(USER_ID, requestDto(15_000_000L));

        assertThrows(BusinessException.class,
                () -> this.service.calculateEvSubsidy(created.getGoalId(), USER_ID));
    }

    @Test
    void calculateEvSubsidy_withNonEvModel_throws() {
        // goal_id=1 시드데이터: selected_model_id=3(캐스퍼, 가솔린)
        assertThrows(BusinessException.class, () -> this.service.calculateEvSubsidy(1L, USER_ID));
    }

    @Test
    void calculateEvSubsidy_withSeededEvGoal_returnsExpectedAmount() {
        CarGoalCreateRequestDTO createDto = CarGoalCreateRequestDTO.builder()
                .budget(30_000_000L)
                .isNew(true)
                .targetDate(LocalDate.of(2027, 1, 1))
                .region("서울특별시")
                .build();
        CarGoalCreateResponseDTO created = this.service.createCarGoal(USER_ID, createDto);

        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder()
                .modelId(16L) // 캐스퍼 일렉트릭(car_type_code=1, base_price=2787만원)
                .build();
        this.service.selectCarModel(created.getGoalId(), USER_ID, selectDto);

        CarEvSubsidyResponseDTO result = this.service.calculateEvSubsidy(created.getGoalId(), USER_ID);

        assertEquals("캐스퍼 일렉트릭", result.getModelName());
        assertEquals(2787L, result.getBasePrice());
        assertEquals(580L, result.getNationalSubsidy());
        assertEquals(150L, result.getLocalSubsidy());
        assertEquals(730L, result.getTotalSubsidy());
        assertEquals(2026, result.getBaseYear());
        assertEquals(2057L, result.getFinalPrice());
    }
}
