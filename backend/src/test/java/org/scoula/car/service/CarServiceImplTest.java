package org.scoula.car.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.car.dto.CarAcquisitionTaxResponseDTO;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.car.dto.CarUsedPriceResponseDTO;
import org.scoula.common.exception.BusinessException;
import org.scoula.config.RootConfig;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
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
}
