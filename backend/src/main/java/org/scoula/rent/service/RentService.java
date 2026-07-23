package org.scoula.rent.service;

import org.scoula.rent.dto.RegionResponseDTO;
import java.util.List;

public interface RentService {
    List<RegionResponseDTO> findRegions(String sido, String sigunguCode);
}