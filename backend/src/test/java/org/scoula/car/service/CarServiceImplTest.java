package org.scoula.car.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.common.exception.BusinessException;
import org.scoula.config.RootConfig;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
