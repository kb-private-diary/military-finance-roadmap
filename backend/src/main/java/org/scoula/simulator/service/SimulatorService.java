package org.scoula.simulator.service;
import org.scoula.simulator.dto.SimulatorSavingDetailsResponseDTO;
public interface SimulatorService {
    SimulatorSavingDetailsResponseDTO getSavingDetails(Long userId);
}
