package org.scoula.rent.service;

import org.scoula.rent.dto.RegionResponseDTO;
import java.util.List;

public interface RentService {
    List<RegionResponseDTO> getRegions(String sido, String sigunguCode);
}