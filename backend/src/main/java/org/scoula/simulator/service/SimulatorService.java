package org.scoula.simulator.service;

import org.scoula.simulator.dto.SimulatorCalculateResponseDTO;
import org.scoula.simulator.dto.SimulatorConstantCalcRequestDTO;
import org.scoula.simulator.dto.SimulatorVariableCalcRequestDTO;
import org.scoula.simulator.dto.SimulatorSavingDetailsResponseDTO;
import org.scoula.simulator.dto.SimulatorSavingLossResponseDTO;

public interface SimulatorService {
    SimulatorSavingDetailsResponseDTO findSavingDetails(Long userId);

    // SIM-API-04: 현재 납입금 기준 중도해지 수령액 및 손실금 조회
    SimulatorSavingLossResponseDTO findSavingLoss(Long userId);

    // SIM-API-02: 만기수령액 1회성 시뮬레이션 (동일 금액)
    SimulatorCalculateResponseDTO calculateConstant(SimulatorConstantCalcRequestDTO request);
    
    // SIM-API-02: 만기수령액 1회성 시뮬레이션 (구간별 금액)
    SimulatorCalculateResponseDTO calculateVariable(SimulatorVariableCalcRequestDTO request);
}
