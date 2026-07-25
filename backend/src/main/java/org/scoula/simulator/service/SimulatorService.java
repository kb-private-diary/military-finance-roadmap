package org.scoula.simulator.service;

import org.scoula.simulator.dto.SimulatorCalculateResponseDTO;
import org.scoula.simulator.dto.SimulatorConstantCalcRequestDTO;
import org.scoula.simulator.dto.SimulatorVariableCalcRequestDTO;
import org.scoula.simulator.dto.SimulatorSavingDetailsResponseDTO;

public interface SimulatorService {
    SimulatorSavingDetailsResponseDTO findSavingDetails(Long userId);
    
    // SIM-API-02: 만기수령액 1회성 시뮬레이션 (동일 금액)
    SimulatorCalculateResponseDTO calculateConstant(SimulatorConstantCalcRequestDTO request);
    
    // SIM-API-02: 만기수령액 1회성 시뮬레이션 (구간별 금액)
    SimulatorCalculateResponseDTO calculateVariable(SimulatorVariableCalcRequestDTO request);
}
