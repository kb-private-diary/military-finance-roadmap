package org.scoula.simulator.mapper;

import java.util.List;

import org.scoula.simulator.dto.SimulatorSavingAccountDTO;
import org.scoula.simulator.dto.SimulatorSavingHistoryDTO;
import org.scoula.simulator.dto.SimulatorUserDatesDTO;

public interface SimulatorMapper {
    SimulatorUserDatesDTO getUserDates(Long userId);
    
    // TODO: 향후 openbanking 공식 VO 완성 시 반환 타입을 해당 VO로 변경 (SimulatorSavingAccountDTO 삭제)
    List<SimulatorSavingAccountDTO> getAccountsByUserId(Long userId);
    
    // TODO: 향후 openbanking 공식 VO 완성 시 반환 타입을 해당 VO로 변경 (SimulatorSavingHistoryDTO 삭제)
    List<SimulatorSavingHistoryDTO> getHistoryByAccountId(Long accountId);
}
