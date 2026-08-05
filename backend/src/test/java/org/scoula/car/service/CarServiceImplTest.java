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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@ActiveProfiles("dev")
@Transactional
class CarServiceImplTest {

    @Autowired
    private CarService service;

    private CarGoalCreateRequestDTO requestDto(Long budget) {
        return CarGoalCreateRequestDTO.builder()
                .userId(1L)
                .budget(budget)
                .carTypeCode(3)
                .isNew(false)
                .targetDate(LocalDate.of(2027, 1, 1))
                .region("서울")
                .build();
    }

    @Test
    void createCarGoal_withValidBudget_returnsGoalId() {
        CarGoalCreateResponseDTO result = this.service.createCarGoal(requestDto(15_000_000L));

        assertNotNull(result.getGoalId());
    }

    @Test
    void createCarGoal_withNullBudget_throws() {
        assertThrows(BusinessException.class, () -> this.service.createCarGoal(requestDto(null)));
    }

    @Test
    void createCarGoal_withZeroBudget_throws() {
        assertThrows(BusinessException.class, () -> this.service.createCarGoal(requestDto(0L)));
    }

    @Test
    void createCarGoal_withNegativeBudget_throws() {
        assertThrows(BusinessException.class, () -> this.service.createCarGoal(requestDto(-1_000_000L)));
    }

    @Test
    void createCarGoal_withExperienceYears_persistsToDetail() {
        CarGoalCreateRequestDTO createDto = CarGoalCreateRequestDTO.builder()
                .userId(1L)
                .budget(15_000_000L)
                .carTypeCode(1)
                .isNew(false)
                .experienceYears(4)
                .targetDate(LocalDate.of(2027, 1, 1))
                .region("서울")
                .build();

        CarGoalCreateResponseDTO created = this.service.createCarGoal(createDto);
        CarGoalResponseDTO detail = this.service.findCarGoalDetail(created.getGoalId());

        assertEquals(4, detail.getExperienceYears());
    }

    @Test
    void calculateMaintenanceCost_endToEndWithExperienceYearsFromCreation_succeeds() {
        // EV 모델을 선택하면 오피넷 실시간 유가 호출 없이 고정 전기요금으로 계산되어 결정적으로 테스트 가능
        CarGoalCreateRequestDTO createDto = CarGoalCreateRequestDTO.builder()
                .userId(1L)
                .budget(30_000_000L)
                .carTypeCode(1)
                .isNew(true)
                .experienceYears(4) // "3~5년" 구간
                .targetDate(LocalDate.of(2027, 1, 1))
                .region("서울")
                .build();
        CarGoalCreateResponseDTO created = this.service.createCarGoal(createDto);

        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder()
                .modelId(16L) // 캐스퍼 일렉트릭(car_type_code=1)
                .build();
        this.service.selectCarModel(created.getGoalId(), selectDto);

        CarMaintenanceCostResponseDTO result = this.service.calculateMaintenanceCost(created.getGoalId());

        assertEquals("전기", result.getFuelType());
        assertEquals(55L, result.getEstimatedFuelCostAnnual());
        assertEquals(40L, result.getInsurancePremiumMin());
        assertEquals(50L, result.getInsurancePremiumMax());
    }

    @Test
    void findCarGoals_withSeededUser_returnsNonEmptyList() {
        // 시드데이터 userId=1은 goal_id=1,2,7을 보유
        List<CarGoalResponseDTO> result = this.service.findCarGoals(1L);

        assertFalse(result.isEmpty());
    }

    @Test
    void findCarGoals_withUnknownUser_returnsEmptyList() {
        List<CarGoalResponseDTO> result = this.service.findCarGoals(9_999_999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findCarGoalDetail_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.findCarGoalDetail(9_999_999L));
    }

    @Test
    void findCarGoalDetail_withSeededGoal_includesSelectedModelName() {
        // goal_id=1 시드데이터: selected_model_id=3(캐스퍼)
        CarGoalResponseDTO result = this.service.findCarGoalDetail(1L);

        assertEquals(2000L, result.getBudget());
        assertEquals("캐스퍼", result.getSelectedModelName());
    }

    @Test
    void recommendCars_withSeededUsedGoal_sortsByTotalPriceAscending() {
        // goal_id=2 시드데이터: car_type_code=1(경차), is_new=false, budget=1500만원
        // 중고 추정가 = base_price * 0.8^3, 경차 취득세율 4%
        List<CarRecommendationResponseDTO> result = this.service.recommendCars(2L);

        assertEquals(6, result.size());
        CarRecommendationResponseDTO cheapest = result.get(0);
        assertEquals("스파크", cheapest.getModelName());
        assertEquals(528L, cheapest.getTotalPrice());
        assertTrue(cheapest.getWithinBudget());

        CarRecommendationResponseDTO mostExpensive = result.get(result.size() - 1);
        assertEquals("레이 EV", mostExpensive.getModelName());
        assertEquals(1_518L, mostExpensive.getTotalPrice());
        assertFalse(mostExpensive.getWithinBudget());
    }

    @Test
    void recommendCars_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.recommendCars(9_999_999L));
    }

    @Test
    void selectCarModel_withMatchingCarType_updatesGoal() {
        // goal_id=2 시드데이터: car_type_code=1(경차), 선택차량 없음(draft)
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder()
                .modelId(1L) // 모닝(car_type_code=1)
                .selectedYear(2022)
                .build();

        CarGoalResponseDTO result = this.service.selectCarModel(2L, selectDto);

        assertEquals(1L, result.getSelectedModelId());
        assertEquals("모닝", result.getSelectedModelName());
        assertEquals("SELECTED", result.getStatus());
    }

    @Test
    void selectCarModel_withMismatchedCarType_throws() {
        // goal_id=2는 car_type_code=1(경차)인데 model_id=4(아반떼)는 car_type_code=2(준중형)
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder().modelId(4L).build();

        assertThrows(BusinessException.class, () -> this.service.selectCarModel(2L, selectDto));
    }

    @Test
    void selectCarModel_withNullModelId_throws() {
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder().build();

        assertThrows(BusinessException.class, () -> this.service.selectCarModel(2L, selectDto));
    }

    @Test
    void selectCarModel_withUnknownModelId_throws() {
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder().modelId(9_999_999L).build();

        assertThrows(BusinessException.class, () -> this.service.selectCarModel(2L, selectDto));
    }

    @Test
    void selectCarModel_withUnknownGoalId_throws() {
        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder().modelId(1L).build();

        assertThrows(BusinessException.class, () -> this.service.selectCarModel(9_999_999L, selectDto));
    }

    @Test
    void calculateMaintenanceCost_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.calculateMaintenanceCost(9_999_999L));
    }

    @Test
    void calculateMaintenanceCost_withoutSelectedModel_throws() {
        CarGoalCreateResponseDTO created = this.service.createCarGoal(requestDto(15_000_000L));

        assertThrows(BusinessException.class,
                () -> this.service.calculateMaintenanceCost(created.getGoalId()));
    }

    @Test
    void calculateAcquisitionTax_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.calculateAcquisitionTax(9_999_999L));
    }

    @Test
    void calculateAcquisitionTax_withoutSelectedModel_throws() {
        CarGoalCreateResponseDTO created = this.service.createCarGoal(requestDto(15_000_000L));

        assertThrows(BusinessException.class,
                () -> this.service.calculateAcquisitionTax(created.getGoalId()));
    }

    @Test
    void calculateAcquisitionTax_withSeededGoal_returnsExpectedAmount() {
        // goal_id=1 시드데이터 기준
        CarAcquisitionTaxResponseDTO result = this.service.calculateAcquisitionTax(1L);

        assertEquals(1460L, result.getVehiclePrice());
        assertEquals(58L, result.getAcquisitionTaxAmount());
        assertTrue(result.getBondExempt());
    }

    @Test
    void calculateUsedPrice_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.calculateUsedPrice(9_999_999L));
    }

    @Test
    void calculateUsedPrice_withoutSelectedModel_throws() {
        CarGoalCreateResponseDTO created = this.service.createCarGoal(requestDto(15_000_000L));

        assertThrows(BusinessException.class,
                () -> this.service.calculateUsedPrice(created.getGoalId()));
    }

    @Test
    void calculateUsedPrice_withoutSelectedYear_assumesDefaultAge() {
        // goal_id=3 시드데이터: selected_model_id=4(아반떼, 1964만원), selected_year=NULL -> 3년 가정
        CarUsedPriceResponseDTO result = this.service.calculateUsedPrice(3L);

        assertEquals(3, result.getAgeYears());
        assertEquals(1006L, result.getEstimatedUsedPrice());
    }

    @Test
    void calculateUsedPrice_withSeededGoal_returnsExpectedAmount() {
        // goal_id=1 시드데이터: selected_model_id=3(캐스퍼, base_price=1460만원), selected_year=2023
        CarUsedPriceResponseDTO result = this.service.calculateUsedPrice(1L);

        assertEquals(1460L, result.getBaseNewPrice());
        assertEquals(2023, result.getSelectedYear());
        assertEquals(748L, result.getEstimatedUsedPrice());
        assertEquals(30L, result.getAcquisitionTaxAmount());
        assertEquals(778L, result.getTotalPrice());
    }

    @Test
    void calculateEvSubsidy_withUnknownGoalId_throws() {
        assertThrows(BusinessException.class, () -> this.service.calculateEvSubsidy(9_999_999L));
    }

    @Test
    void calculateEvSubsidy_withoutSelectedModel_throws() {
        CarGoalCreateResponseDTO created = this.service.createCarGoal(requestDto(15_000_000L));

        assertThrows(BusinessException.class, () -> this.service.calculateEvSubsidy(created.getGoalId()));
    }

    @Test
    void calculateEvSubsidy_withNonEvModel_throws() {
        // goal_id=1 시드데이터: selected_model_id=3(캐스퍼, 가솔린)
        assertThrows(BusinessException.class, () -> this.service.calculateEvSubsidy(1L));
    }

    @Test
    void calculateEvSubsidy_withSeededEvGoal_returnsExpectedAmount() {
        CarGoalCreateRequestDTO createDto = CarGoalCreateRequestDTO.builder()
                .userId(1L)
                .budget(30_000_000L)
                .carTypeCode(1)
                .isNew(true)
                .targetDate(LocalDate.of(2027, 1, 1))
                .region("서울특별시")
                .build();
        CarGoalCreateResponseDTO created = this.service.createCarGoal(createDto);

        CarModelSelectRequestDTO selectDto = CarModelSelectRequestDTO.builder()
                .modelId(16L) // 캐스퍼 일렉트릭(car_type_code=1, base_price=2787만원)
                .build();
        this.service.selectCarModel(created.getGoalId(), selectDto);

        CarEvSubsidyResponseDTO result = this.service.calculateEvSubsidy(created.getGoalId());

        assertEquals("캐스퍼 일렉트릭", result.getModelName());
        assertEquals(2787L, result.getBasePrice());
        assertEquals(580L, result.getNationalSubsidy());
        assertEquals(150L, result.getLocalSubsidy());
        assertEquals(730L, result.getTotalSubsidy());
        assertEquals(2026, result.getBaseYear());
        assertEquals(2057L, result.getFinalPrice());
    }
}
