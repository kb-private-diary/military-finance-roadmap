package org.scoula.car.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.scoula.car.domain.CarGoalVO;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.car.mapper.CarMapper;
import org.scoula.common.exception.BusinessException;

@Service
@RequiredArgsConstructor
@Log4j2
public class CarServiceImpl implements CarService {

    private final CarMapper carMapper;

    @Override
    @Transactional
    public CarGoalCreateResponseDTO createCarGoal(CarGoalCreateRequestDTO requestDTO) {
        // 예산은 목표 등록의 핵심 값이라 목표 저장 전에 먼저 검증
        Long budget = requestDTO.getBudget();
        if (budget == null || budget <= 0) {
            throw BusinessException.badRequest("예산은 0보다 커야 합니다", "CAR_001");
        }

        CarGoalVO carGoalVO = new CarGoalVO();
        carGoalVO.setUserId(requestDTO.getUserId());
        carGoalVO.setBudget(requestDTO.getBudget());
        carGoalVO.setCarTypeCode(requestDTO.getCarTypeCode());
        carGoalVO.setIsNew(requestDTO.getIsNew());
        carGoalVO.setTargetDate(requestDTO.getTargetDate());
        carGoalVO.setRegion(requestDTO.getRegion());

        this.carMapper.createCarGoal(carGoalVO);

        return new CarGoalCreateResponseDTO(carGoalVO.getGoalId());
    }
}
