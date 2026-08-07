package org.scoula.main.service;

import org.scoula.main.dto.MainSummaryResponseDTO;

public interface MainService {

    MainSummaryResponseDTO findSummary(Long userId);
}
